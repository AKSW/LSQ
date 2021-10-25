package net.sansa_stack.rdf.spark.io;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.aksw.commons.lambda.serializable.SerializableFunction;
import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.aksw.commons.rx.function.RxFunction;
import org.aksw.jena_sparql_api.rx.script.SparqlScriptProcessor;
import org.aksw.jenax.arq.dataset.api.DatasetOneNg;
import org.aksw.jenax.sparql.rx.op.FlowOfDatasetOps;
import org.aksw.jenax.sparql.rx.op.FlowOfResourcesOps;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParser;
import org.aksw.simba.lsq.core.LsqRdfizeSpec;
import org.aksw.simba.lsq.core.LsqRdfizer;
import org.aksw.simba.lsq.core.rx.io.input.LsqProbeUtils;
import org.aksw.simba.lsq.model.RemoteExecution;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import net.sansa_stack.spark.io.rdf.input.impl.RdfSourceFactoryImpl;
import net.sansa_stack.spark.rdd.op.rdf.JavaRddOfDatasetsOps;
import net.sansa_stack.spark.rdd.op.rx.JavaRddRxOps;


public class LsqSparkIo {

    private static final Logger logger = LoggerFactory.getLogger(LsqSparkIo.class);

    /**
     * Method that creates a reader for a specific inputResource under the given config.
     * The config's inputResources are ignored.
     *
     * @param config
     * @param inputResource
     * @return
     * @throws Exception
     */
    public static JavaRDD<DatasetOneNg> createReader(
            SparkSession sparkSession,
            String logSource,
            SerializableSupplier<SparqlStmtParser> sparqlStmtParserSupp,
            String logFormat,
            Map<String, SourceOfRddOfResources> logFmtRegistry,
            String baseIri,
            String hostHashSalt,
            String serviceUrl,
            SerializableFunction<String, String> hashFn
            ) throws Exception {

        Lang lang = logFormat == null
                ? null
                : RDFLanguages.nameToLang(logFormat);

        if(lang == null) {
            lang = RDFDataMgr.determineLang(logSource, null, null);
        }

        JavaRDD<DatasetOneNg> result = null;

        // Check if we are dealing with RDF
        if(lang != null) {
            // If quad based, use streaming
            // otherwise partition by lsq.text property
            if(RDFLanguages.isQuads(lang)) {

                // TODO Stream as datasets first, then select any resource with LSQ.text
                logger.info("Quad-based format detected - assuming RDFized log as input");
                result = RdfSourceFactoryImpl.from(sparkSession).get(logSource).asDatasets().toJavaRDD();
//                        .flatMap(ds -> DatasetUtils.listResourcesWithProperty(ds, LSQ.text).toList().iterator());

                // throw new RuntimeException("Quad based format not implemented");

            } else if(RDFLanguages.isTriples(lang)){
                logger.info("Triple-based format detected - assuming RDFized log as input");
                // Model model = RDFDataMgr.loadModel(logSource, lang);
                // result = null;
                throw new RuntimeException("Triple based format not implemented");
                //result = Flowable.fromIterable(() -> model.listSubjectsWithProperty(LSQ.text))
            }
//            else {
//                throw new RuntimeException("Unknown RDF input format; neither triples nor quads");
//            }

        }

        String effectiveLogFormat = null;

        // If the result is still null, probe for log formats
        if(result == null) {
            JavaRDD<Resource> tmpRdd = null;


            // Probe for known RDF or know log format
            logger.info("Processing log source " + logSource);

            if(Strings.isNullOrEmpty(logFormat)) {
                List<Entry<String, Number>> formats = probeLogFormat(logFmtRegistry, logSource);
                if(formats.isEmpty()) {
                    throw new RuntimeException("Could not auto-detect a log format for " + logSource);
                }

//    				if(formats.size() != 1) {
//    					throw new RuntimeException("Expected probe to return exactly 1 log format for source " + logSource + ", got: " + formats);
//    				}
                effectiveLogFormat = formats.get(0).getKey();
                logger.info("Auto-selected format [" + effectiveLogFormat + "] among auto-detected candidates " + formats);
            } else {
                effectiveLogFormat = logFormat;
            }

            SourceOfRddOfResources rddFactory = logFmtRegistry.get(effectiveLogFormat);

            //Mapper webLogParser = config.getLogFmtRegistry().get(logFormat);
            if(rddFactory == null) {
                throw new RuntimeException("No log format parser found for '" + logFormat + "'");
            }

            tmpRdd = rddFactory.load(logSource);


            // The webLogParser yields resources (blank nodes) for the log entry
            // First add a sequence id attribute
            // Then invert the entry:

            // SerializableFlowableTransformer<Upstream, Downstream>

            JavaRDD<Resource> zippedRdd = tmpRdd
                .zipWithIndex()
                .map(e -> {
                    Resource r = e._1;
                    Long idx = e._2;

                    // Temporarily add the sequence id. For records without timestamp
                    // the sequence id is used as a fallback
                    RemoteExecution re = r.as(RemoteExecution.class);
                    re.setSequenceId(idx);

                    return r;
                });

            RxFunction<Resource, DatasetOneNg> rxFn = RxFunction.<Resource>identity()
                // Add the zipped index to the resource
                .andThen(upstream -> {
                    SparqlStmtParser sparqlStmtParser = sparqlStmtParserSupp.get();

                    return upstream.flatMapIterable(record -> {
                        Optional<Resource> r;
                        try {
                            r = LsqRdfizer.rdfizeLogRecord(sparqlStmtParser, baseIri, hostHashSalt, serviceUrl, hashFn, record);
                        } catch (Exception e) {
                            logger.warn("Internal error; trying to continue", e);
                            r = Optional.empty();
                        }

                        return r.map(Collections::singleton).orElse(Collections.emptySet());
                    });
                })
                .andThen(FlowOfResourcesOps::mapToDatasets);
//                .andThenMap(x -> {
//                    System.out.println("got dataset: " + x.getGraphName());
//                    RDFDataMgr.write(System.out, x, RDFFormat.TRIG_PRETTY);
//                    return x;
//                });

            result = JavaRddRxOps.mapPartitions(zippedRdd, rxFn);

//            result = tmpRdd.mapPartitionsToPair(it ->
//                Streams.stream(it).map(r -> new Tuple2<>(r.getURI(), r.getModel())).iterator());
        }


        return result;
    }

//    public static NodesInModel fromRDFNode(RDFNode rdfNode) {
//        return new NodesInModel(
//                rdfNode.getModel(),
//                new NodeSelectorFromUnaryRelation(Concept.createNodes(rdfNode.asNode())));
//    }

    public static List<Entry<String, Number>> probeLogFormat(Map<String, SourceOfRddOfResources> registry, String resource) {

        Multimap<? extends Number, String> report = probeLogFormatCore(registry, resource);

        List<Entry<String, Number>> result = report.entries().stream()
            .filter(e -> e.getKey().doubleValue() != 0)
            .map(e -> Maps.immutableEntry(e.getValue(), (Number)e.getKey()))
//			.limit(2)
            //.map(Entry::getValue)
            //.flatMap(Collection::stream)
            .collect(Collectors.toList());

        return result;
    }


    /**
     * Return formats sorted by weight
     * Errorneous records must not be skipped but represented as resources having the lsq:processingError property.
     * Higher weight = better format; more properties could be parsed with that format
     *
     * @param registry
     * @param loader
     * @param filename
     * @return
     */
    public static Multimap<Double, String> probeLogFormatCore(Map<String, SourceOfRddOfResources> registry, String filename) {
        Multimap<Double, String> result = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());


        int sampleSize = 1000;
        for(Entry<String, SourceOfRddOfResources> entry : registry.entrySet()) {
            String formatName = entry.getKey();

            SourceOfRddOfResources fn = entry.getValue();

            // Try-catch block because fn.parse may throw an exception before the flowable is created
            // For example, a format may attempt ot read the input stream into a buffer
            List<Resource> baseItems;
            try {
                baseItems = fn.load(filename).take(sampleSize);
            } catch(Exception e) {
                baseItems = Collections.emptyList();
                logger.debug("Probing against format " + formatName + " raised exception", e);
            }

            double weight = LsqProbeUtils.analyzeInformationRatio(baseItems, r -> r.hasProperty(LSQ.processingError));

            result.put(weight, formatName);
        }

        return result;
    }



    public static JavaRDD<DatasetOneNg> createLsqRdfFlow(
            // JavaSparkContext sc,
            SparkSession sparkSession,
            LsqRdfizeSpec rdfizeCmd)  {
        String logFormat = rdfizeCmd.getInputLogFormat();
        List<String> logSources = rdfizeCmd.getNonOptionArgs();
        String baseIri = rdfizeCmd.getBaseIri();

        String tmpHostHashSalt = rdfizeCmd.getHostSalt();
        if(tmpHostHashSalt == null) {
            tmpHostHashSalt = UUID.randomUUID().toString();
            // TODO Make host hashing a post processing step for the log rdfization
            logger.info("Auto generated host hash salt (only used for non-rdf log input): " + tmpHostHashSalt);
        }

        String hostHashSalt = tmpHostHashSalt;

        String endpointUrl = rdfizeCmd.getEndpointUrl();
// TODO Validate all sources first: For trig files it is ok if no endpoint is specified
//		if(endpointUrl == null) {
//			throw new RuntimeException("Please specify the URL of the endpoint the provided query logs are assigned to.");
//		}


        List<String> rawPrefixSources = rdfizeCmd.getPrefixSources();
        Iterable<String> prefixSources = Lists.newArrayList(LsqRdfizer.prependDefaultPrefixSources(rawPrefixSources));

        // FIXME The prefixes need to be serialized in the driver!
        SerializableSupplier<SparqlStmtParser> sparqlStmtParserSupp = () -> LsqRdfizer.createSparqlParser(prefixSources);


        Map<String, SourceOfRddOfResources> logFmtRegistry = LsqRegistrySparkAdapter.createDefaultLogFmtRegistry(sparkSession);


        // Hash function which is applied after combining host names with salts
        SerializableFunction<String, String> hashFn = str -> BaseEncoding.base64Url().omitPadding().encode(Hashing.sha256()
                .hashString(str, StandardCharsets.UTF_8)
                .asBytes());

        List<JavaRDD<DatasetOneNg>> rdds = logSources.stream()
            .map(logSource -> {
                JavaRDD<DatasetOneNg> r;
                try {
                    r = LsqSparkIo.createReader(
                        sparkSession,
                        logSource,
                        sparqlStmtParserSupp,
                        logFormat,
                        logFmtRegistry,
                        baseIri,
                        hostHashSalt,
                        endpointUrl,
                        hashFn);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return r;
            })
            .collect(Collectors.toList());

        JavaSparkContext sc = JavaSparkContext.fromSparkContext(sparkSession.sparkContext());

        @SuppressWarnings("unchecked")
        JavaRDD<DatasetOneNg> result = rdds.size() == 1
            ? rdds.iterator().next()
            : sc.union(rdds.toArray(new JavaRDD[0]));


        if(rdfizeCmd.isSlimMode()) {

            SparqlScriptProcessor sparqlProcessor = SparqlScriptProcessor.createWithEnvSubstitution(null);
            sparqlProcessor.process("lsq-slimify.sparql");
            List<SparqlStmt> sparqlStmts = sparqlProcessor.getSparqlStmts().stream()
                    .map(Entry::getKey).collect(Collectors.toList());

            result = JavaRddRxOps.mapPartitions(result,
                    RxFunction.<DatasetOneNg>identity()
                    .andThenMap(x -> (Dataset)x)
                    .andThen(FlowOfDatasetOps.mapWithSparql(sparqlStmts))
                    .andThen(FlowOfDatasetOps.flatMapNamedGraphs()));

            // result = flatMapResources(sparqlStmts).apply(result);
        }

        if(!rdfizeCmd.isNoMerge()) {
            result = JavaRddOfDatasetsOps.groupNamedGraphsByGraphIri(result, true, true, 0);
//            result = JavaPairRddFunction.<String, Model>identity()
//                // .toPairRdd(JavaRddOfResourcesOps::mapToNamedModels)
//                .andThen(rdd -> JavaRddOfNamedModelsOps.groupNamedModels(rdd, true, true, 0))
//                .toRdd(JavaRddOfNamedModelsOps::mapToResources)
//                .apply(result);

//            SysSort sortCmd = new SysSort();
//            sortCmd.bufferSize = rdfizeCmd.bufferSize;
//            sortCmd.temporaryDirectory = rdfizeCmd.temporaryDirectory;
//
//            FlowableTransformer<GroupedResourceInDataset, GroupedResourceInDataset> sorter = ResourceInDatasetFlowOps.createSystemSorter(sortCmd, null);
//            logRdfEvents = logRdfEvents
//                    .compose(ResourceInDatasetFlowOps.groupedResourceInDataset())
//                    .compose(sorter)
//                    .compose(ResourceInDatasetFlowOps::mergeConsecutiveResourceInDatasets)
//                    .flatMap(ResourceInDatasetFlowOps::ungrouperResourceInDataset);
        }

        return result;
    }


    /**
     * Run a sequence of sparql statements against a model in the values
     *
     * @param <K>
     * @param <V>
     * @param getModel
     * @param baseStmts
     * @return
     */
//    public static <K, V> JavaPairRddFunction<K, V, K, V> runOnEachModel(
//            SerializableFunction<V, Model> modelGetter,
//            Collection<? extends SparqlStmt> baseStmts) {
//        // Turn statements to strings for serialization
//        List<String> stmtStrs = baseStmts.stream().map(Object::toString).collect(Collectors.toList());
//
//        return inRdd -> inRdd.mapPartitionsToPair(it -> {
//            return Streams.stream(it)
//                    .map(t -> {
//                        Model model = modelGetter.apply(t._2());
//                        Dataset dataset = DatasetFactory.wrap(model);
//
//                        SparqlStmtParser parser = SparqlStmtParserImpl.createAsGiven();
//                        List<SparqlStmt> stmts = stmtStrs.stream().map(parser).collect(Collectors.toList());
//
//                        FlowableTransformer<Dataset, Dataset> transformer = DatasetFlowOps.createMapperDataset(stmts, DatasetGraphFactory::create, cxt -> {});
//
//                        transformer.apply(null)
//
//
//                        // return RxOps.transform(it, transformer);
//
//                        return t;
//                    }).iterator();
//        });
//    }

//    public static JavaRddFunction<Resource, Resource> flatMapResources(Collection<? extends SparqlStmt> baseStmts) {
//        // Turn statements to strings for serialization
//        List<String> stmtStrs = baseStmts.stream().map(Object::toString).collect(Collectors.toList());
//
//        return JavaRddFunction.<Resource>identity()
//            .toPairRdd(JavaRddOfResourcesOps::mapToNamedModels)
//            .toRdd(JavaRddOfNamedModelsOps::mapToDatasets)
//            .andThen(rdd -> rdd.mapPartitions(it -> {
//                    SparqlStmtParser parser = SparqlStmtParserImpl.createAsGiven();
//                    List<SparqlStmt> stmts = stmtStrs.stream().map(parser).collect(Collectors.toList());
//
//                    FlowableTransformer<Dataset, Dataset> transformer = FlowOfDatasetOps.createMapperDataset(stmts, DatasetGraphFactory::create, cxt -> {});
//
//                    return RxOps.transform(it, transformer);
//             }))
//            .toPairRdd(JavaRddOfDatasetsOps::flatMapToNamedModels)
//            .toRdd(JavaRddOfNamedModelsOps::mapToResources);
//    }


//    public static JavaRddFunction<Dataset, Dataset> flatMapDataset(Collection<? extends SparqlStmt> baseStmts) {
//
//        // Turn statements to strings for serialization
//        List<String> stmtStrs = baseStmts.stream().map(Object::toString).collect(Collectors.toList());
//
//        return rdd -> {
//            return rdd.mapPartitions(it -> {
//                SparqlStmtParser parser = SparqlStmtParserImpl.createAsGiven();
//                List<SparqlStmt> stmts = stmtStrs.stream().map(parser).collect(Collectors.toList());
//
//                FlowableTransformer<Dataset, Dataset> transformer = FlowOfDatasetOps.createMapperDataset(stmts, DatasetGraphFactory::create, cxt -> {});
//
//                return RxOps.transform(it, transformer);
//            });
//        };
//    }

}
