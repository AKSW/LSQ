package org.aksw.simba.lsq.spark.cmd.impl;

import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.io.util.StdIo;
import org.aksw.jena_sparql_api.rx.RDFLanguagesEx;
import org.aksw.jena_sparql_api.utils.io.StreamRDFDeferred;
import org.aksw.jena_sparql_api.utils.io.WriterStreamRDFBaseUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
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
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sansa_stack.hadoop.jena.rdf.trig.FileInputFormatTrigDataset;
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

        Cmd cmd = null;


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
                    String.join(", ", "net.sansa_stack.rdf.spark.io.JenaKryoRegistrator",
                            "net.sansa_stack.query.spark.ontop.OntopKryoRegistrator"))
            .set("spark.sql.crossJoin.enabled", "true");

        SparkSession sparkSession = SparkSession.builder().config(sparkConf).getOrCreate();

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkSession.sparkContext().getConf());

        Configuration hadoopConf = sparkSession.sparkContext().hadoopConfiguration();


        List<Path> paths = cmd.nonOptionArgs.stream().flatMap(pathStr -> {
            Collection<Path> r;
            try {
                URI uri = new URI(pathStr);
                // TODO Use try-with-resources for the filesystem?
                FileSystem fs = FileSystem.get(uri, hadoopConf);
                Path path = new Path(pathStr);
                path = fs.resolvePath(path);

                r = fs.isFile(path) ? Collections.singleton(path) : Collections.emptySet();

            } catch (Exception e) {
                logger.error(ExceptionUtils.getRootCauseMessage(e));
                r = Collections.emptySet();
            }
            return r.stream();
        }).collect(Collectors.toList());



        // TODO Consider validtion of paths
        /*
        Set<Path> pathSet = new LinkedHashSet<>(paths);
        val validPaths = paths
          .filter(_.getFileSystem(hadoopConf).get)
          .filter(!fileSystem.isFile(_))
          .toSet
        Set<Path> validPathSet = new LinkedHashSet<>(paths);
        Set<Path> invalidPaths = Sets.difference(pathSet, validPathSet);
        */

        Set<Path> validPathSet = new LinkedHashSet<>(paths);
        Set<Path> invalidPaths = Collections.emptySet();
        if (!invalidPaths.isEmpty()) {
            throw new IllegalArgumentException("The following paths are invalid (do not exist or are not a (readable) file): " + invalidPaths);
        }

        List<JavaRDD<Dataset>> rdds = validPathSet.stream()
                .map(path -> createRddOfDatasetFromTrig(sparkSession, path.toString()))
                        .collect(Collectors.toList());

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

    public static JavaRDD<Dataset> createRddOfDatasetFromTrig(SparkSession sparkSession, String path) {
        Configuration confHadoop = sparkSession.sparkContext().hadoopConfiguration();

        JavaRDD<Dataset> result = sparkSession.sparkContext()
            .newAPIHadoopFile(
                path,
                FileInputFormatTrigDataset.class,
                LongWritable.class,
                Dataset.class,
                confHadoop)
            .toJavaRDD()
            .map(t -> t._2());

        return result;
    }

}

/**
 * Called from the Java class [[CmdSansaTrigMerge]]
 */
 /*
class CmdSansaTrigMergeImpl
object CmdSansaTrigMergeImpl {
  private val logger = LoggerFactory.getLogger(getClass)
  // JenaSystem.init()

  def run(cmd: CmdSansaTrigMerge): Integer = {

    import collection.JavaConverters._

    val stopwatch = StopWatch.createStarted()

    val prefixes: PrefixMapping = new PrefixMappingImpl()


    for (prefixSource <- cmd.outPrefixes.asScala) {
      logger.info("Adding prefixes from " + prefixSource)
      val tmp = RDFDataMgr.loadModel(prefixSource)
      prefixes.setNsPrefixes(tmp)
    }

    // val resultSetFormats = RDFLanguagesEx.getResultSetFormats
    // val outLang = RDFLanguagesEx.findLang(cmd.outFormat, resultSetFormats)

//    if (outLang == null) {
//      throw new IllegalArgumentException("No result set format found for " + cmd.outFormat)
//    }

//    logger.info("Detected registered result set format: " + outLang)



    val spark = SparkSession.builder
      .appName(s"Trig Merge ( ${cmd.trigFiles} )")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.kryoserializer.buffer.max", "1000") // MB
      .config("spark.kryo.registrator", String.join(
        ", ",
        "net.sansa_stack.rdf.spark.io.JenaKryoRegistrator",
        "net.sansa_stack.query.spark.ontop.OntopKryoRegistrator"))
      .config("spark.sql.crossJoin.enabled", true)
      .getOrCreate()

    // StreamManager.get().addLocator(new LocatorHdfs(fileSystem))

    val hadoopConf = spark.sparkContext.hadoopConfiguration

    val paths = cmd.trigFiles.asScala
      .flatMap(pathStr => {
        var r: Iterator[(FileSystem, Path)] = Iterator()
        try {
          val uri = new URI(pathStr)
          // TODO Use try-with-resources for the filesystem?
          val fs = FileSystem.get(uri, hadoopConf)
          val path = new Path(pathStr)
          fs.resolvePath(path)
          r = Iterator((fs, path))
        } catch {
          case e: Throwable => logger.error(ExceptionUtils.getRootCauseMessage(e))
        }
        r
      })
      .filter { case (fs, file) => fs.isFile(file) }
      .map(_._2)
      .toList

    /*
    val validPaths = paths
      .filter(_.getFileSystem(hadoopConf).get)
      .filter(!fileSystem.isFile(_))
      .toSet
* /

val initialRdd: RDD[Dataset] = spark.sparkContext.union(
      validPathSet
        .map(path => spark.datasets(Lang.TRIG)(path.toString)).toSeq)


    val effectiveRdd = RddOfDatasetOps.groupNamedGraphsByGraphIri(initialRdd, cmd.sort, cmd.distinct, cmd.numPartitions)

    val outRdfFormat = RDFLanguagesEx.findRdfFormat(cmd.outFormat)
      // RDFFormat.TRIG_BLOCKS

    if (cmd.outFolder == null && cmd.outFile == null) {

      val out = StdIo.openStdOutWithCloseShield

      // val out = Files.newOutputStream(Paths.get("output.trig"), StandardOpenOption.WRITE, StandardOpenOption.CREATE)
      // System.out
      val coreWriter = StreamRDFWriter.getWriterStream(out, outRdfFormat, null)

      if (coreWriter.isInstanceOf[WriterStreamRDFBase]) {
        WriterStreamRDFBaseUtils.setNodeToLabel(coreWriter.asInstanceOf[WriterStreamRDFBase], SyntaxLabels.createNodeToLabelAsGiven())
      }

      val writer = new StreamRDFDeferred(coreWriter, true,
        prefixes, cmd.deferOutputForUsedPrefixes, Long.MaxValue, null)

      writer.start
      StreamRDFOps.sendPrefixesToStream(prefixes, writer)

      // val it = effectiveRdd.collect
      val it = effectiveRdd.toLocalIterator
      for (dataset <- it) {
        StreamRDFOps.sendDatasetToStream(dataset.asDatasetGraph, writer)
      }
      writer.finish
      out.flush
    } else {
      effectiveRdd.configureSave()
        .setGlobalPrefixMapping(new PrefixMappingImpl())
        .setOutputFormat(outRdfFormat)
        .setMapQuadsToTriplesForTripleLangs(true)
        // .setAllowOverwriteFiles(true)
        .setPartitionFolder(cmd.outFolder)
        .setTargetFile(cmd.outFile)
        // .setUseElephas(true)
        .setDeletePartitionFolderAfterMerge(true)
        .run()

//      if (cmd.outFile != null) {
//        // effectiveRdd.flatMapToQuads.save(cmd.outFile)
//        effectiveRdd.saveToFile(cmd.outFile, new PrefixMappingImpl(), outRdfFormat, cmd.outFolder)
//      } else { // if (cmd.outFolder != null) {
//        effectiveRdd.saveToFolder(cmd.outFolder, new PrefixMappingImpl(), outRdfFormat)
//      }
    }

    // effectiveRdd.saveAsFile("outfile.trig.bz2", prefixes, RDFFormat.TRIG_BLOCKS)

    // effectiveRdd.coalesce(1)


    // ResultSetMgr.write(System.out, resultSetSpark.collectToTable().toResultSet, outLang)

    logger.info("Processing time: " + stopwatch.getTime(TimeUnit.SECONDS) + " seconds")

    0 // exit code
  }
}
*/
