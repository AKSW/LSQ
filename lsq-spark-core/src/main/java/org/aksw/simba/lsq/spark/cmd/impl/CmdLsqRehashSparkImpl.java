package org.aksw.simba.lsq.spark.cmd.impl;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.ResourceInDataset;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.simba.lsq.core.LsqUtils;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.ext.com.google.common.base.Stopwatch;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.lang.PipedQuadsStream;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.RiotParsers;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Streams;

import net.sansa_stack.spark.io.rdf.input.api.RdfSource;
import net.sansa_stack.spark.io.rdf.input.api.RdfSourceFactory;
import net.sansa_stack.spark.io.rdf.input.impl.RdfSourceFactoryImpl;
import net.sansa_stack.spark.io.rdf.output.RddRdfSaver;
import net.sansa_stack.spark.rdd.op.rdf.JavaRddOfDatasetsOps;
import net.sansa_stack.spark.rdd.op.rdf.JavaRddOfNamedModelsOps;




public class CmdLsqRehashSparkImpl {
    private static final Logger logger = LoggerFactory.getLogger(CmdLsqRehashSparkImpl.class);



    public static Iterator<Quad> createIteratorQuads(InputStream input, Lang lang, String baseIRI) {
        // Special case N-Quads, because the RIOT reader has a pull interface
        if ( RDFLanguages.sameLang(RDFLanguages.NQUADS, lang) ) {
            return Iter.onCloseIO(
                RiotParsers.createIteratorNQuads(input, null, RiotLib.dftProfile()),
                input);
        }
        // Otherwise, we have to spin up a thread to deal with it
        final PipedRDFIterator<Quad> it = new PipedRDFIterator<>();
        final PipedQuadsStream out = new PipedQuadsStream(it);

        Thread t = new Thread(()->parseFromInputStream(out, input, baseIRI, lang, null));
        t.start();
        return it;
    }

    public static void parseFromInputStream(StreamRDF destination, InputStream in, String baseUri, Lang lang, Context context) {
        RDFParser.create()
            .source(in)
            // Disabling checking does not seem to give a significant performance gain
            // For a 3GB Trig file parsing took ~1:45 min +- 5 seconds either way
            //.checking(false)
            .base(baseUri)
            .lang(lang)
            .context(context)
            .errorHandler(RDFDataMgrRx.dftErrorHandler())
            .labelToNode(RDFDataMgrRx.createLabelToNodeAsGivenOrRandom())
            //.errorHandler(handler)
            .parse(destination);
    }


    public static void main(String[] args) throws Exception {
        Stopwatch sw = Stopwatch.createStarted();

        // String src = "/home/raven/Datasets/lsq/kegg.merged.lsq.v2.trig.bz2";
        String src = "/home/raven/Datasets/lsq/lsq1.ttl.bz2";
        // mainRx(src);
        // mainSpark(src);
        mainJena(src);

        System.err.println("Total process took: " + sw.elapsed(TimeUnit.SECONDS) + " seconds");

    }

    public static void mainJena(String src) throws Exception {

        if (false) {
            try (InputStream in = Files.newInputStream(Paths.get(src))) {
                Iterator<Quad> it = createIteratorQuads(in, Lang.TRIG, null);
                System.out.println("Size: " + Iterators.size(it));
            }

        } else {
            try (InputStream in = new BZip2CompressorInputStream(
                    Files.newInputStream(Paths.get(src)), true)) {
                Iterator<Quad> it = createIteratorQuads(in, Lang.TRIG, null);
                System.out.println("Size: " + Iterators.size(it));
            }
        }
    }

    public static void mainRx(String src) throws Exception {
//    	() -> new BZip2CompressorInputStream(
//                Files.newInputStream(Paths.get(src))
        long count = RDFDataMgrRx.createFlowableQuads(src, Lang.TRIG, null).count().blockingGet();

        System.out.println("Size rx: " + count);
    }

    public static void mainSpark(CmdRdfIo cmd) throws Exception {

        PrefixMapping prefixes = new PrefixMappingImpl();

        Iterable<String> prefixSources = LsqUtils.prependDefaultPrefixSources(cmd.getPrefixSources());

        for (String prefixSource : prefixSources) {
            logger.info("Adding prefixes from " + prefixSource);
            Model tmp = RDFDataMgr.loadModel(prefixSource);
            prefixes.setNsPrefixes(tmp);
        }


        SparkConf sparkConf = new SparkConf()
            .setAppName("Lsq Rehash ( ${cmd.trigFiles} )")
            .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
            .set("spark.kryoserializer.buffer.max", "1000") // MB
            .set("spark.kryo.registrator",
                    String.join(", ", "net.sansa_stack.rdf.spark.io.JenaKryoRegistrator"))
            .set("spark.sql.crossJoin.enabled", "true")
            // .set("spark.hadoop.mapred.max.split.size", "" + 4 * 1024 * 1024)
            //		mapreduce.input.fileinputformat.split.minsize
            ;

        sparkConf.setMaster("local[*]");

        SparkSession sparkSession = SparkSession.builder().config(sparkConf).getOrCreate();

        JavaSparkContext javaSparkContext = JavaSparkContext.fromSparkContext(sparkSession.sparkContext());


        RdfSourceFactory rdfSourceFactory = RdfSourceFactoryImpl.from(sparkSession);


//        if (true) {
//            System.out.println("Size spark: " + rdfSourceFactory.get(src, Lang.TURTLE).asQuads().count());
//            return;
//        }


        List<JavaRDD<Dataset>> rdds = cmd.getNonOptionArgs().stream()
            .map(rdfSourceFactory::get)
            .map(RdfSource::asDatasets)
            .map(RDD::toJavaRDD)
            .collect(Collectors.toList())
            ;

        @SuppressWarnings("unchecked")
        JavaRDD<Dataset>[] arr = rdds.toArray(new JavaRDD[0]);
        JavaRDD<Dataset> initialRdd = javaSparkContext.union(arr);


        JavaRDD<Dataset> effectiveRdd = initialRdd; //.repartition(4);


        JavaRDD<ResourceInDataset> ridRdd =
                JavaRddOfNamedModelsOps.mapToResourceInDataset(
                        JavaRddOfDatasetsOps.flatMapToNamedModels(effectiveRdd));


        Broadcast<PrefixMapping> prefixesBc = javaSparkContext.broadcast(prefixes);
        JavaRDD<Dataset> outRdd = ridRdd
            .mapPartitions(ridIt -> {
                PrefixMapping pm = prefixesBc.getValue();
                SparqlStmtParser sparqlStmtParser = SparqlStmtParserImpl.create(
                        Syntax.syntaxARQ, pm, true);

                return Streams.stream(ridIt)
                    .map(rid -> LsqUtils.rehashQueryHash(rid, sparqlStmtParser))
                    .map(ResourceInDataset::getDataset)
                    .iterator();
            });



        // System.out.println("Size spark: " + effectiveRdd.count());

        RddRdfSaver.createForDataset(outRdd)
            .setGlobalPrefixMapping(new PrefixMappingImpl())
            .setOutputFormat(cmd.getOutFormat())
            .setMapQuadsToTriplesForTripleLangs(true)
            // .setAllowOverwriteFiles(true)
            .setPartitionFolder(cmd.getOutFolder())
            .setTargetFile(cmd.getOutFile())
            // .setUseElephas(true)
            .setAllowOverwriteFiles(true)
            .setDeletePartitionFolderAfterMerge(true)
            .run();
    }


    // List<String> sources = Arrays.asList("/home/raven/.dcat/test3/cache/gitlab.com/limbo-project/metadata-catalog/raw/master/catalog.all.ttl/_content/data.nt");
    // List<String> sources = Arrays.asList(src);


//    CmdRdfIoBase cmd = new CmdRdfIoBase();
//    cmd.nonOptionArgs = sources;
//    cmd.outFolder = "/tmp/spark";
//    cmd.outFormat = "trig/blocks";
//    cmd.deferOutputForUsedPrefixes = 100;
//    cmd.prefixSources = Arrays.asList();
//    cmd.outFile = "/tmp/result.trig";

//    boolean isOutputToConsole = false;
//
//    if (isOutputToConsole) {
//        cmd.setOutFolder(null);
//        cmd.setOutFile(null);
//    }

}

