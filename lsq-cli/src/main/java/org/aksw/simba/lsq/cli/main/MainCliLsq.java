package org.aksw.simba.lsq.cli.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.io.syscall.sort.SysSort;
import org.aksw.commons.io.util.StdIo;
import org.aksw.commons.io.util.UriToPathUtils;
import org.aksw.commons.util.exception.ExceptionUtilsAksw;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.datapod.impl.DataPods;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.rx.dataset.DatasetFlowOps;
import org.aksw.jena_sparql_api.rx.dataset.ResourceInDatasetFlowOps;
import org.aksw.jena_sparql_api.rx.script.SparqlScriptProcessor;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.dataset.impl.ResourceInDatasetImpl;
import org.aksw.jenax.arq.dataset.orderaware.DatasetFactoryEx;
import org.aksw.jenax.arq.dataset.orderaware.DatasetGraphFactoryEx;
import org.aksw.jenax.dataaccess.sparql.connection.reconnect.SparqlQueryConnectionWithReconnect;
import org.aksw.jenax.reprogen.core.MapperProxyUtils;
import org.aksw.jenax.reprogen.hashid.HashIdCxt;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrRx;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.aksw.jenax.sparql.relation.dataset.NodesInDataset;
import org.aksw.jenax.sparql.rx.op.FlowOfRdfNodesInDatasetsOps;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.simba.lsq.cli.cmd.base.CmdLsqAnalyzeBase;
import org.aksw.simba.lsq.cli.cmd.base.CmdLsqMain;
import org.aksw.simba.lsq.cli.cmd.base.CmdLsqRdfizeBase;
import org.aksw.simba.lsq.cli.cmd.rx.api.CmdLsqRxBenchmarkCreate;
import org.aksw.simba.lsq.cli.cmd.rx.api.CmdLsqRxBenchmarkPrepare;
import org.aksw.simba.lsq.cli.cmd.rx.api.CmdLsqRxBenchmarkRun;
import org.aksw.simba.lsq.cli.cmd.rx.api.CmdLsqRxProbe;
import org.aksw.simba.lsq.core.LsqRdfizer;
import org.aksw.simba.lsq.core.ResourceParser;
import org.aksw.simba.lsq.core.io.input.registry.LsqInputFormatRegistry;
import org.aksw.simba.lsq.core.rx.io.input.LsqLogRecordRdfizer;
import org.aksw.simba.lsq.core.rx.io.input.LsqLogRecordRdfizerQueryOnly;
import org.aksw.simba.lsq.core.rx.io.input.LsqProbeUtils;
import org.aksw.simba.lsq.core.rx.io.input.LsqRxIo;
import org.aksw.simba.lsq.enricher.benchmark.core.LsqBenchmarkProcessor;
import org.aksw.simba.lsq.enricher.core.LsqEnricherRegistry;
import org.aksw.simba.lsq.enricher.core.LsqEnricherShell;
import org.aksw.simba.lsq.model.ExperimentConfig;
import org.aksw.simba.lsq.model.ExperimentExec;
import org.aksw.simba.lsq.model.ExperimentRun;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.simba.lsq.vocab.PROV;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spinrdf.vocabulary.SP;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import picocli.CommandLine;



/**
 * This is the main class of Linked Sparql Queries
 *
 * @author raven
 *
 */
// @SpringBootApplication
public class MainCliLsq {

    private static final Logger logger = LoggerFactory.getLogger(MainCliLsq.class);

/*
 * Spring Boot Code - in case we need it at some point; for now it turned out to not bring any benefit
 */

//    public static void main(String[] args) {
//        try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder()
//                .sources(ConfigCliLsq.class)
//            .bannerMode(Banner.Mode.OFF)
//            // If true, Desktop.isDesktopSupported() will return false, meaning we can't
//            // launch a browser
//            .headless(false).web(WebApplicationType.NONE).run(args)) {
//        }
//    }

//    @Configuration
//    @PropertySource("classpath:lsq-core.properties")
//    public static class ConfigCliLsq {
//        @Value("lsq-core.version")
//        protected String lsqCoreVersion;
//
//        @Bean
//        public ApplicationRunner applicationRunner() {
//            return args -> {
//                try {
//                    int exitCode = mainCore(args.getSourceArgs());
//                    System.exit(exitCode);
//
//                } catch(Exception e) {
//                    ExceptionUtils.rethrowIfNotBrokenPipe(e);
//                }
//            };
//        }
//    }

    public static void main(String[] args) {
        int exitCode = mainCore(args);
        System.exit(exitCode);
    }

    public static int mainCore(String[] args) {
        return new CommandLine(new CmdLsqMain())
            .setExecutionExceptionHandler((ex, commandLine, parseResult) -> {
                ExceptionUtilsAksw.rethrowIfNotBrokenPipe(ex);
                return 0;
            })
            .execute(args);
    }

    public static String getOrCreateSalt(CmdLsqRdfizeBase rdfizeCmd) {
        String result = rdfizeCmd.hostSalt;
        if(result == null) {
            result = UUID.randomUUID().toString();
            // TODO Make host hashing a post processing step for the log rdfization
            logger.info("Auto generated host hash salt (only used for non-rdf log input): " + result);
        }
        return result;
    }

    public static Flowable<ResourceInDataset> createLsqRdfFlow(CmdLsqRdfizeBase rdfizeCmd) throws FileNotFoundException, IOException, ParseException {
        String tmpHostHashSalt = getOrCreateSalt(rdfizeCmd);
        return createLsqRdfFlow(rdfizeCmd, tmpHostHashSalt);
    }

    public static Flowable<ResourceInDataset> createLsqRdfFlow(CmdLsqRdfizeBase rdfizeCmd, String hostHashSalt) throws FileNotFoundException, IOException, ParseException {
        String logFormat = rdfizeCmd.inputLogFormat;
        List<String> logSources = rdfizeCmd.nonOptionArgs;
        String baseIri = rdfizeCmd.baseIri;

        String endpointUrl = rdfizeCmd.getEndpointUrl();
// TODO Validate all sources first: For trig files it is ok if no endpoint is specified
//		if(endpointUrl == null) {
//			throw new RuntimeException("Please specify the URL of the endpoint the provided query logs are assigned to.");
//		}


        List<String> rawPrefixSources = rdfizeCmd.prefixSources;
        Iterable<String> prefixSources = LsqRdfizer.prependDefaultPrefixSources(rawPrefixSources);
        Function<String, SparqlStmt> sparqlStmtParser = LsqRdfizer.createSparqlParser(prefixSources);


        Map<String, ResourceParser> logFmtRegistry = LsqInputFormatRegistry.createDefaultLogFmtRegistry();


        // Hash function which is applied after combining host names with salts
        Function<String, String> hostHashFn = str -> BaseEncoding.base64Url().omitPadding().encode(Hashing.sha256()
                .hashString(str, StandardCharsets.UTF_8)
                .asBytes());

        Function<Resource, Resource> rdfizer;
        if (rdfizeCmd.isQueryOnly()) {
            rdfizer = new LsqLogRecordRdfizerQueryOnly(sparqlStmtParser, baseIri, hostHashFn);

        } else {
            rdfizer = new LsqLogRecordRdfizer(
                    sparqlStmtParser,
                    baseIri,
                    hostHashSalt,
                    endpointUrl,
                    hostHashFn
            );
        }

        Flowable<Resource> logRdfEvents = Flowable
            .fromIterable(logSources)
            .flatMap(logSource -> {
                Flowable<Resource> st = LsqRxIo.createReader(
                        logSource,
                        logFormat,
                        logFmtRegistry,
                        rdfizer);
                return st;
            });


        Flowable<ResourceInDataset> legacyLogRdfEvents = logRdfEvents.map(ResourceInDatasetImpl::createFromCopyIntoResourceGraph);

        if(rdfizeCmd.slimMode) {
//            CmdNgsMap cmd = new CmdNgsMap();
//            cmd.mapSpec = new MapSpec();
//            cmd.mapSpec.stmts.add("lsq-slimify.sparql");

            SparqlScriptProcessor sparqlProcessor = SparqlScriptProcessor.createWithEnvSubstitution(null);
            sparqlProcessor.process("lsq-slimify.sparql");
            List<SparqlStmt> sparqlStmts = sparqlProcessor.getSparqlStmts().stream()
                    .map(Entry::getKey).collect(Collectors.toList());


//			cmd.nonOptionArgs.addAll(cmdInvert.nonOptionArgs);

//			JenaSystem.init();
            //RDFDataMgrEx.loadQueries("lsq-slimify.sparql", PrefixMapping.Extended);

//			SparqlStmtUtils.processFile(pm, "lsq-slimify.sparql");
//			MainCliNamedGraphStream.createMapper2(); //map(DefaultPrefixes.prefixes, cmd);
            // PrefixMapping.Extended
            FlowableTransformer<ResourceInDataset, ResourceInDataset> mapper =
                    DatasetFlowOps.createMapperDataset(sparqlStmts,
                            r -> r.getDataset(),
                            (r, ds) -> r.inDataset(ds),
                            DatasetGraphFactoryEx::createInsertOrderPreservingDatasetGraph,
                            cxt -> {});


            legacyLogRdfEvents = legacyLogRdfEvents
                    .compose(mapper);
        }

        if(!rdfizeCmd.noMerge) {
            SysSort sortCmd = new SysSort();
            sortCmd.bufferSize = rdfizeCmd.bufferSize;
            sortCmd.temporaryDirectory = rdfizeCmd.temporaryDirectory;

            FlowableTransformer<NodesInDataset, NodesInDataset> sorter = ResourceInDatasetFlowOps.createSystemSorter(sortCmd, null);
            legacyLogRdfEvents = legacyLogRdfEvents
                    .compose(FlowOfRdfNodesInDatasetsOps.groupedResourceInDataset())
                    .compose(sorter)
                    .compose(FlowOfRdfNodesInDatasetsOps::mergeConsecutiveResourceInDatasets)
                    .flatMap(FlowOfRdfNodesInDatasetsOps::ungrouperResourceInDataset);
        }

        return legacyLogRdfEvents;
    }

    public static void rdfize(CmdLsqRdfizeBase cmdRdfize) throws Exception {
        Flowable<ResourceInDataset> logRdfEvents = createLsqRdfFlow(cmdRdfize);
        try {
            RDFDataMgrRx.writeResources(logRdfEvents, StdIo.openStdOutWithCloseShield(), RDFFormat.TRIG_BLOCKS);
            logger.info("RDFization completed successfully");
        } catch(Exception e) {
            ExceptionUtilsAksw.rethrowIfNotBrokenPipe(e);
        }
    }

    public static void probe(CmdLsqRxProbe cmdProbe) {
        List<String> nonOptionArgs = cmdProbe.nonOptionArgs;
        if(nonOptionArgs.isEmpty()) {
            logger.error("No arguments provided.");
            logger.error("Argument must be one or more log files which will be probed against all registered LSQ log formats");
        }

        for(int i = 0; i < nonOptionArgs.size(); ++i) {
            String filename = nonOptionArgs.get(i);

            List<Entry<String, Number>> bestCands = LsqProbeUtils.probeLogFormat(filename);

            System.out.println(filename + "\t" + bestCands);
        }
    }

    public static <T> List<T> effectiveList(List<T> input, boolean isWhiteList, List<T> available) {
        List<T> result;
        if (isWhiteList) {
            // Validate that all inputs are available
            Set<T> availableSet = new HashSet<>(available);

            Set<T> missingSet = new HashSet<>(input);
            missingSet.removeAll(availableSet);

            if (!missingSet.isEmpty()) {
                throw new NoSuchElementException("The following requested items are missing: " + missingSet);
            }

            result = input;
        } else {
            Set<T> blacklist = new HashSet<>(input);
            result = available.stream().filter(x -> !blacklist.contains(x)).toList();
        }
        return result;
    }

//    public static Function<Resource, Resource> createEnricherFactory(String baseIri, List<String> enrichers, boolean isWhitelist, Supplier<LsqEnricherRegistry> registrySupplier) {
//        return () -> {
//            LsqEnricherRegistry registry = registrySupplier.get();
//            List<String> effectiveList = effectiveList(enrichers, isWhitelist, new ArrayList<>(registry.getKeys()));
//
//            return in -> {
//                // TODO Implement support for blacklisting
//
//                 LsqQuery q = in.as(LsqQuery.class);
//                 // TODO Parse query here only once
//                 // Store it in a context object that gets passed to the enrichers?
//
//                 if (q.getParseError() == null) {
//                     for (String name : enrichers) {
//                         LsqEnricherFactory f = registry.getOrThrow(name);
//                         LsqEnricher enricher = f.get();
//                         safeEnricher(enricher).apply(q);
//                     }
//
//                     // TODO Given enrichers a name
//                     // TODO Track failed enrichments in the output? qualify error with enricher name?
//                     // TODO Create a registry for enrichers
////                     safeEnricher(LsqEnrichments::enrichWithFullSpinModelCore).apply(q);
////                     safeEnricher(LsqEnrichments::enrichWithStaticAnalysis).apply(q);
////
////                     safeEnricher(LsqEnrichments::enrichWithBBox).apply(q);
//                 }
//
//                 // TODO createLsqRdfFlow already performs skolemize; duplicated effort
//                 Resource out = Skolemize.skolemize(in, baseIri, LsqQuery.class, null);
//                 return out.as(LsqQuery.class);
//            };
//        };
//    }

//    public static SerializableFunction<Resource, Resource> createEnricher(String baseIri) {
//        return in -> {
//            LsqQuery q = in.as(LsqQuery.class);
//            // TODO Parse query here only once
//            // Store it in a context object that gets passed to the enrichers?
//
//            if (q.getParseError() == null) {
//
//                // TODO Given enrichers a name
//                // TODO Track failed enrichments in the output? qualify error with enricher name?
//                // TODO Create a registry for enrichers
//                safeEnricher(LsqEnrichments::enrichWithFullSpinModelCore).apply(q);
//                safeEnricher(LsqEnrichments::enrichWithStaticAnalysis).apply(q);
//
//                safeEnricher(LsqEnrichments::enrichWithBBox).apply(q);
//            }
//
//            // TODO createLsqRdfFlow already performs skolemize; duplicated effort
//            Resource out = Skolemize.skolemize(in, baseIri, LsqQuery.class, null);
//            return out;
//        };
//    }


    public static void analyze(CmdLsqAnalyzeBase analyzeCmd) throws Exception {
        CmdLsqRdfizeBase rdfizeCmd = new CmdLsqRdfizeBase();
        rdfizeCmd.nonOptionArgs = analyzeCmd.nonOptionArgs;
        rdfizeCmd.noMerge = true;

        String baseIri = rdfizeCmd.baseIri;
        // TODO How to obtain the baseIRI? A simple hack would be to 'grep'
        // for the id part before lsqQuery
        // The only 'clean' option would be to make the baseIri an attribute of every lsqQuery resource
        // which might be somewhat overkill

        LsqEnricherShell enricherFactory = new LsqEnricherShell(baseIri, analyzeCmd.enricherSpec.getEffectiveList(), LsqEnricherRegistry::get);
        Function<Resource, Resource> enricher = enricherFactory.get();

        Flowable<ResourceInDataset> flow = createLsqRdfFlow(rdfizeCmd);

        Flowable<Dataset> dsFlow = flow.map(rid -> {
            // TODO The enricher may in general rename the input resource due to skolemization - handle this case
            enricher.apply(rid);
            return rid;
        })
//        .map(ResourceInDatasetImpl::createFromCopyIntoResourceGraph)
        .map(ResourceInDataset::getDataset);

        RDFDataMgrRx.writeDatasets(dsFlow, StdIo.openStdOutWithCloseShield(), RDFFormat.TRIG_BLOCKS);
    }



    /*
    public static DatasetGraph replace(DatasetGraph datasetGraph, String target, String replacement) {
        DatasetGraph result;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            StreamRDF streamRDF = StreamRDFWriterEx.getWriterStream(
                    baos,
                    RDFFormat.NTRIPLES);

            StreamRDFOps.sendDatasetToStream(datasetGraph, streamRDF);

            String ntriples = new String(baos.toByteArray());
            ntriples.replace(target, replacement);

            result = DatasetGraphFactoryEx.createInsertOrderPreservingDatasetGraph();
            try (InputStream in = new ByteArrayInputStream(ntriples.getBytes(StandardCharsets.UTF_8))) {
                RDFDataMgr.read(result, in, Lang.NTRIPLES);
            }
        }
        return result;
    }
    */


/* Use the spark version instead
    public static void rehash(CmdLsqRehash rehashCmd) throws Exception {
        CmdLsqRdfize rdfizeCmd = new CmdLsqRdfize();
        rdfizeCmd.nonOptionArgs = rehashCmd.nonOptionArgs;
        rdfizeCmd.noMerge = true;
        // TODO How to obtain the baseIRI? A simple hack would be to 'grep'
        // for the id part before lsqQuery
        // The only 'clean' option would be to make the baseIri an attribute of every lsqQuery resource
        // which might be somewhat overkill

//        PrefixMapping pm = new PrefixMappingImpl();
//        addLsqPrefixes(pm);

        // Flowable<ResourceInDataset> flow = createLsqRdfFlow(rdfizeCmd);
        Flowable<Dataset> flow = NamedGraphStreamCliUtils.createNamedGraphStreamFromArgs(
                rehashCmd.nonOptionArgs, null, null, RDFDataMgrEx.DEFAULT_PROBE_LANGS);


        Flowable<Dataset> dsFlow = flow
                .flatMap(ResourceInDatasetFlowOps::naturalResources)
                .map(rid -> LsqUtils::rehashQueryHash)
                .map(ResourceInDataset::getDataset);


        RDFDataMgrRx.writeDatasets(dsFlow, StdIo.openStdOutWithCloseShield(), RDFFormat.TRIG_BLOCKS);
    }
*/

    public static PrefixMapping addLsqPrefixes(PrefixMapping prefixMapping) {
        return prefixMapping
            .setNsPrefix("lsqo", LSQ.NS)
            .setNsPrefix("dct", DCTerms.NS)
            .setNsPrefix("rdf", RDF.uri)
            .setNsPrefix("rdfs", RDFS.uri)
            .setNsPrefix("xsd", XSD.NS)
            .setNsPrefix("lsqr", "http://lsq.aksw.org/")
            .setNsPrefix("sp", SP.NS)
            .setNsPrefix("prov", PROV.NS);
    }


    public static String createExperimentId(String datasetLabel) {
        // experimentId = distributionId + "_" + timestamp

        Instant now = Instant.now();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(now, ZoneId.systemDefault());
        // Calendar nowCal = GregorianCalendar.from(zdt);
        //String timestamp = now.toString();
        String timestamp = DateTimeFormatter.ISO_LOCAL_DATE.format(zdt);

        String expId = "xc-" + datasetLabel + "_" + timestamp;
        return expId;
    }

    /**
     * Creates a model with the configuration of the benchmark experiment
     * which serves as the base for running the benchmark
     *
     *
     * @param benchmarkCreateCmd
     * @throws Exception
     */
    public static void benchmarkCreate(CmdLsqRxBenchmarkCreate benchmarkCreateCmd) throws Exception {

        String baseIri = benchmarkCreateCmd.baseIri;
        String endpointUrl = benchmarkCreateCmd.endpoint;

        // We could have datasetId or a datasetIri
        String datasetId = benchmarkCreateCmd.dataset;

        String datasetIri = null;
        if(datasetId != null && datasetId.matches("^[\\w]+://.*")) {
            datasetIri = datasetId;
        }

//        // experimentId = distributionId + "_" + timestamp
//        String datasetLabel = UriToPathUtils.resolvePath(datasetId).toString()
//                .replace('/', '-');
//
//
//        Instant now = Instant.now();
//        ZonedDateTime zdt = ZonedDateTime.ofInstant(now, ZoneId.systemDefault());
//        // Calendar nowCal = GregorianCalendar.from(zdt);
//        //String timestamp = now.toString();
//        String timestamp = DateTimeFormatter.ISO_LOCAL_DATE.format(zdt);
//
//        String expId = "xc-" + datasetLabel + "_" + timestamp;
        String datasetLabel = UriToPathUtils.resolvePath(datasetId).toString()
                .replace('/', '-');

        String expId = createExperimentId(datasetId);

        // Not used if stdout flag is set
        String outFilename = sanitizeFilename(expId) + ".conf.ttl";


        String expIri = baseIri + expId;

        Long qt = benchmarkCreateCmd.queryTimeoutInMs;
        Long ct = benchmarkCreateCmd.connectionTimeoutInMs;

        Long datasetSize = benchmarkCreateCmd.datasetSize;

        if(datasetSize == null) {
//            logger.info("Dataset size not set. Attempting to query its size.");
//            logger.info("If successful, restart lsq with -s <obtainedDatasetSize>");

            // TODO inject default graphs
            String countQueryStr = "SELECT (COUNT(*) AS ?c) { ?s ?p ?o }";
            logger.info("Attempting to count number of triples using query " + countQueryStr);
            try(RDFConnection conn = RDFConnectionRemote.create()
                .destination(endpointUrl)
                .acceptHeaderSelectQuery(WebContent.contentTypeResultsXML)
                .build()) {

                Object raw = SparqlRx.execSelect(conn, countQueryStr)
                    .blockingFirst()
                    .get("c").asNode().getLiteralValue();
                datasetSize = ((Number)raw).longValue();
            }
        }

        Model model = DatasetFactoryEx.createInsertOrderPreservingDataset().getDefaultModel();
        addLsqPrefixes(model);

        RdfDataRefSparqlEndpoint dataRef = model.createResource().as(RdfDataRefSparqlEndpoint.class)
                .setServiceUrl(endpointUrl)
                .mutateDefaultGraphs(dgs -> dgs.addAll(benchmarkCreateCmd.defaultGraphs));

        ExperimentConfig cfg = model.createResource(expIri).as(ExperimentConfig.class);
        cfg
            .setIdentifier(expId)
            // .setCreationDate(nowCal)
            .setDataRef(dataRef)
            .setExecutionTimeoutForRetrieval(qt == null ? new BigDecimal(300) : new BigDecimal(qt).divide(new BigDecimal(1000)))
            .setConnectionTimeoutForRetrieval(ct == null ? new BigDecimal(60) : new BigDecimal(ct).divide(new BigDecimal(1000)))
            .setMaxResultCountForCounting(1000000l) // 1M
            .setMaxByteSizeForCounting(-1l) // limit only by count
            .setMaxResultCountForSerialization(-1l) // limit by byte size
            .setMaxByteSizeForSerialization(1000000l) // 1MB
            .setExecutionTimeoutForCounting(qt == null ? new BigDecimal(300) : new BigDecimal(qt).divide(new BigDecimal(1000)))
            .setConnectionTimeoutForCounting(ct == null ? new BigDecimal(60) : new BigDecimal(ct).divide(new BigDecimal(1000)))
            .setMaxCount(1000000000l)
            .setMaxCountAffectsTp(false)
            .setUserAgent(benchmarkCreateCmd.userAgent)
            .benchmarkSecondaryQueries(true)
            .setDatasetSize(datasetSize)
            .setDatasetLabel(datasetLabel)
            .setDatasetIri(datasetIri)
            .setBaseIri(baseIri)
            ;

        HashIdCxt tmp = MapperProxyUtils.getHashId(dataRef);
        ResourceUtils.renameResource(dataRef, baseIri + tmp.getStringId(dataRef));


        Path outPath = null;
        if (!benchmarkCreateCmd.stdout) {
            outPath = Paths.get(outFilename).toAbsolutePath().normalize();
            logger.info("Writing config to " + outPath);
        }

        try(OutputStream out = outPath == null
                ? StdIo.openStdOutWithCloseShield()
                : Files.newOutputStream(outPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            RDFDataMgr.write(out, model, RDFFormat.TURTLE_BLOCKS);
        }

        if(outPath != null) {
            System.out.println(outPath);
        }
    }

    /**
     * Replaces any non-word character (with the exception of '-') with an underescore.
     * Only suitable for ASCII names; otherwise the result will be mostly composed of underscores!
     *
     * @param inputName
     * @return
     */
    public static String sanitizeFilename(String inputName) {
        String result = inputName.replaceAll("[^\\w-]", "_");
        return result;
    }

    public static void benchmarkExecute(CmdLsqRxBenchmarkRun benchmarkExecuteCmd) throws Exception {
        // TTODO Find a nicer way to pass config options around; reusing the command object is far from optimal
        CmdLsqRdfizeBase rdfizeCmd = new CmdLsqRdfizeBase();
        rdfizeCmd.nonOptionArgs = benchmarkExecuteCmd.logSources;
        rdfizeCmd.noMerge = true;

        Flowable<LsqQuery> queryFlow = createLsqRdfFlow(rdfizeCmd)
//        		.map(r -> {
//        			Resource x = ModelFactory.createDefaultModel().createResource();
//        			 x.getModel().add(r.getModel());
//        			 return x;
//        		})
                .map(r -> r.as(LsqQuery.class));


        String configSrc = benchmarkExecuteCmd.config;

        //List<String> logSources = benchmarkCmd.logSources;
        // Load the benchmark config and create a benchmark run for it

        ExperimentExec expExec = tryLoadExec(configSrc)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not detect a resource with " + LSQ.Terms.config + " property in " + configSrc));

        ExperimentConfig expConfig = expExec.getConfig();


        ExperimentRun expRun = expExec.getModel().createResource().as(ExperimentRun.class)
                .setExec(expExec)
                .setRunId(0)
                // .setTimestamp(null); // xsddt
                ;


        String lsqBaseIri = Objects.requireNonNull(expConfig.getBaseIri(), "Base IRI (e.g. http://lsq.aksw.org/) not provided");
        //LsqBenchmeclipse-javadoc:%E2%98%82=jena-sparql-api-conjure/src%5C/main%5C/java%3Corg.aksw.jena_sparql_apiarkProcessor.createProcessor()


        String runId = expConfig.getIdentifier();
        Objects.requireNonNull(runId, "Experiment run identifier must not be null");

        // Create a folder with the database for the run
        String fsSafeId = sanitizeFilename(runId);

        Path tdb2BasePath = benchmarkExecuteCmd.tdb2BasePath;
        Path tdb2FullPath = tdb2BasePath.resolve("lsq-" + fsSafeId);
        Files.createDirectories(tdb2FullPath);
        String fullPathStr = tdb2FullPath.toString();

        LsqEnricherShell enricherFactory = new LsqEnricherShell(lsqBaseIri, benchmarkExecuteCmd.enricherSpec.getEffectiveList(), LsqEnricherRegistry::get);
        Function<Resource, Resource> enricher = enricherFactory.get();

        logger.info("TDB2 benchmark db location: " + tdb2FullPath);

        Dataset dataset = TDB2Factory.connectDataset(fullPathStr);
        try (OutputStream outStream = StdIo.openStdOutWithCloseShield();
            RDFConnection indexConn = RDFConnection.connect(dataset)) {
            StreamRDF out = StreamRDFWriter.getWriterStream(outStream, RDFFormat.TRIG_BLOCKS);
            out.start();
            RdfDataRefSparqlEndpoint dataRef = expConfig.getDataRef();
            try (RdfDataPod dataPod = DataPods.fromDataRef(dataRef)) {
                try (SparqlQueryConnection benchmarkConn =
                        SparqlQueryConnectionWithReconnect.create(() -> dataPod.getConnection())) {
                    LsqBenchmarkProcessor.process(out, queryFlow, lsqBaseIri, expConfig, expExec, expRun, enricher, benchmarkConn, indexConn);
                }
            }
            out.finish();
        } finally {
            dataset.close();
        }

        logger.info("Benchmark run " + runId + " completed successfully");
    }

    public static <T extends RDFNode> Optional<T> tryLoadResourceWithProperty(String src, Property p, Class<T> clazz) {
        Model configModel = RDFDataMgr.loadModel(src);

        Optional<T> result = configModel.listResourcesWithProperty(p)
                .nextOptional()
                .map(x -> x.as(clazz));

        return result;
    }

    public static Optional<ExperimentConfig> tryLoadConfig(String src) {
        return tryLoadResourceWithProperty(src, LSQ.endpoint, ExperimentConfig.class);
    }

    public static Optional<ExperimentExec> tryLoadExec(String src) {
        return tryLoadResourceWithProperty(src, LSQ.config, ExperimentExec.class);
    }


    public static void benchmarkPrepare(CmdLsqRxBenchmarkPrepare benchmarkCmd) throws Exception {
//        CmdLsqRdfize rdfizeCmd = new CmdLsqRdfize();
//        rdfizeCmd.nonOptionArgs = benchmarkCmd.logSources;
//        rdfizeCmd.noMerge = true;

        String configSrc = benchmarkCmd.config;

        //List<String> logSources = benchmarkCmd.logSources;
        // Load the benchmark config and create a benchmark run for it

        // The config model is first loaded with the config file
        // and subsequently enriched with a benchmark run resource

        Model configModel = DatasetFactoryEx.createInsertOrderPreservingDataset().getDefaultModel();
        RDFDataMgr.read(configModel, configSrc);

        ExperimentConfig config = configModel.listResourcesWithProperty(LSQ.endpoint)
                .nextOptional()
                .map(x -> x.as(ExperimentConfig.class))
                .orElse(null);

        // Create an instance of the config at the current time
        Instant benchmarkRunStartTimestamp = Instant.now(); //;Instant.ofEpochMilli(0);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(benchmarkRunStartTimestamp, ZoneId.systemDefault());
        Calendar cal = GregorianCalendar.from(zdt);
        XSDDateTime xsddt = new XSDDateTime(cal);
        String timestampStr = DateTimeFormatter.ISO_INSTANT.format(zdt);

        String configId = config.getIdentifier();
        String runId = configId + "_" + timestampStr;

        // Not used if stdout flag is set
        String outFilename = sanitizeFilename(runId) + ".run.ttl";

        ExperimentExec expRun = configModel
                .createResource()
                .as(ExperimentExec.class)
                .setConfig(config)
                .setTimestamp(xsddt)
                ;
                // .setIdentifier(runId);


        String runIri = config.getBaseIri() + runId;
        ResourceUtils.renameResource(expRun, runIri);


        Path outPath = null;
        if (!benchmarkCmd.stdout) {
            outPath = Paths.get(outFilename).toAbsolutePath().normalize();
            logger.info("Writing run information to " + outPath);
        }

        try (OutputStream out = outPath == null
                ? StdIo.openStdOutWithCloseShield()
                : Files.newOutputStream(outPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            RDFDataMgr.write(out, configModel, RDFFormat.TURTLE_BLOCKS);
        }

        if(outPath != null) {
            System.out.println(outPath);
        }
    }

}
