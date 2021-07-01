package org.aksw.simba.lsq.spark.cmd.impl;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.base.Stopwatch;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sansa_stack.rdf.spark.io.RddRdfSaver;
import net.sansa_stack.rdf.spark.io.input.api.RdfSource;
import net.sansa_stack.rdf.spark.io.input.api.RdfSourceFactory;
import net.sansa_stack.rdf.spark.io.input.impl.RdfSourceFactoryImpl;

class Cmd {
    public List<String> nonOptionArgs;
    public String outFile;
    public String outFolder;
    public String outFormat;
    public List<String> prefixSources;
    public long deferOutputForUsedPrefixes;
}

public class CmdLsqRehashSparkImpl {
    private static final Logger logger = LoggerFactory.getLogger(CmdLsqRehashSparkImpl.class);



    public static void main(String[] args) throws Exception {

        Stopwatch sw = Stopwatch.createStarted();

        // List<String> sources = Arrays.asList("/home/raven/.dcat/test3/cache/gitlab.com/limbo-project/metadata-catalog/raw/master/catalog.all.ttl/_content/data.nt");
        List<String> sources = Arrays.asList("/home/raven/Datasets/lsq/kegg.merged.lsq.v2.trig.bz2");


        Cmd cmd = new Cmd();
        cmd.nonOptionArgs = sources;
        cmd.outFolder = "/tmp/spark";
        cmd.outFormat = "trig/blocks";
        cmd.deferOutputForUsedPrefixes = 100;
        cmd.prefixSources = Arrays.asList();
        cmd.outFile = "/tmp/result.trig";

        boolean isOutputToConsole = false;

        if (isOutputToConsole) {
            cmd.outFolder = null;
            cmd.outFile = null;
        }

        PrefixMapping prefixes = new PrefixMappingImpl();

        for (String prefixSource : cmd.prefixSources) {
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
            .set("spark.sql.crossJoin.enabled", "true");

        sparkConf.setMaster("local[*]");

        SparkSession sparkSession = SparkSession.builder().config(sparkConf).getOrCreate();

        JavaSparkContext javaSparkContext = JavaSparkContext.fromSparkContext(sparkSession.sparkContext());


        RdfSourceFactory rdfSourceFactory = RdfSourceFactoryImpl.from(sparkSession);

        List<JavaRDD<Dataset>> rdds = cmd.nonOptionArgs.stream()
            .map(rdfSourceFactory::get)
            .map(RdfSource::asDatasets)
            .map(RDD::toJavaRDD)
            .collect(Collectors.toList())
            ;

        @SuppressWarnings("unchecked")
        JavaRDD<Dataset>[] arr = rdds.toArray(new JavaRDD[0]);
        JavaRDD<Dataset> initialRdd = javaSparkContext.union(arr);


        JavaRDD<Dataset> effectiveRdd = initialRdd;


        RddRdfSaver.createForDataset(effectiveRdd.repartition(10))
            .setGlobalPrefixMapping(new PrefixMappingImpl())
            .setOutputFormat(cmd.outFormat)
            .setMapQuadsToTriplesForTripleLangs(true)
            // .setAllowOverwriteFiles(true)
            .setPartitionFolder(cmd.outFolder)
            .setTargetFile(cmd.outFile)
            // .setUseElephas(true)
            .setAllowOverwriteFiles(true)
            .setDeletePartitionFolderAfterMerge(true)
            .run();

        System.err.println("Total process took: " + sw.elapsed(TimeUnit.SECONDS) + " seconds");
    }

}

