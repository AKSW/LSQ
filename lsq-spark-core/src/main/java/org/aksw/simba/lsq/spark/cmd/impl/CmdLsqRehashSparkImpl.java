package org.aksw.simba.lsq.spark.cmd.impl;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.commons.io.util.StdIo;
import org.aksw.jena_sparql_api.rx.RDFLanguagesEx;
import org.aksw.jena_sparql_api.utils.io.StreamRDFDeferred;
import org.aksw.jena_sparql_api.utils.io.WriterStreamRDFBaseUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.riot.system.SyntaxLabels;
import org.apache.jena.riot.writer.WriterStreamRDFBase;
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

        Cmd cmd = new Cmd();
        cmd.nonOptionArgs = Arrays.asList("/home/raven/Datasets/lsq/kegg.merged.lsq.v2.trig.bz2");
        cmd.outFolder = "/tmp/spark";
        cmd.outFormat = "trig/blocks";
        cmd.deferOutputForUsedPrefixes = 100;
        cmd.prefixSources = Arrays.asList();
        cmd.outFile = "/tmp/result.trig";


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

        Configuration hadoopConf = sparkSession.sparkContext().hadoopConfiguration();


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

        if (cmd.outFolder == null && cmd.outFile == null) {

            OutputStream out = StdIo.openStdOutWithCloseShield();

            // val out = Files.newOutputStream(Paths.get("output.trig"),
            // StandardOpenOption.WRITE, StandardOpenOption.CREATE)
            // System.out
            RDFFormat outRdfFormat = RDFLanguagesEx.findRdfFormat(cmd.outFormat);
            StreamRDF coreWriter = StreamRDFWriter.getWriterStream(out, outRdfFormat, null);

            if (coreWriter instanceof WriterStreamRDFBase) {
                WriterStreamRDFBaseUtils.setNodeToLabel((WriterStreamRDFBase) coreWriter,
                        SyntaxLabels.createNodeToLabelAsGiven());
            }

            StreamRDF writer = new StreamRDFDeferred(coreWriter, true, prefixes, cmd.deferOutputForUsedPrefixes,
                    Long.MAX_VALUE, null);

            writer.start();
            StreamRDFOps.sendPrefixesToStream(prefixes, writer);

            // val it = effectiveRdd.collect
            Iterator<Dataset> it = effectiveRdd.toLocalIterator();
            it.forEachRemaining(ds -> StreamRDFOps.sendDatasetToStream(ds.asDatasetGraph(), writer));
            writer.finish();
            out.flush();
        } else {
            RddRdfSaver.createForDataset(effectiveRdd)
                .setGlobalPrefixMapping(new PrefixMappingImpl())
                .setOutputFormat(cmd.outFormat)
                .setMapQuadsToTriplesForTripleLangs(true)
                // .setAllowOverwriteFiles(true)
                .setPartitionFolder(cmd.outFolder)
                .setTargetFile(cmd.outFile)
                // .setUseElephas(true)
                .setDeletePartitionFolderAfterMerge(true)
                .run();
          }
    }

}

