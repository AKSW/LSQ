package org.aksw.simba.lsq.spark.cmd.impl;

import java.util.function.Supplier;

import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.aksw.commons.model.csvw.domain.impl.DialectMutableImpl;
import org.aksw.commons.model.csvw.univocity.UnivocityCsvwConf;
import org.aksw.jenax.arq.picocli.CmdMixinArq;
import org.aksw.jenax.arq.util.exec.query.ExecutionContextUtils;
import org.aksw.jenax.arq.util.security.ArqSecurity;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import net.sansa_stack.spark.io.csv.input.CsvDataSources;
import net.sansa_stack.spark.io.rdf.output.RddRdfWriter;
import net.sansa_stack.spark.rdd.op.rdf.JavaRddOfBindingsOps;

public class LsqTarqlTest {

    public static void main(String[] args) throws Exception {

        String str = "PREFIX lsq: <http://lsq.aksw.org/vocab#>\n"
                + "PREFIX prov: <http://www.w3.org/ns/prov#>\n"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
                + "CONSTRUCT {\n"
                + "  GRAPH ?s {\n"
                + "    ?s\n"
                + "      lsq:query ?query ;\n"
                + "      lsq:host ?domain ;\n"
                + "      lsq:headers [ <http://example.org/header#User-agent> ?agent ] ;\n"
                + "      prov:atTime ?t\n"
                + "  }\n"
                + "} {\n"
                + "  # query,domain,agent,timestamp\n"
                + "  BIND(IRI(CONCAT('urn:lsq:', MD5(CONCAT(?query, '-', ?domain, '-', ?timestamp)))) AS ?s)\n"
                + "  BIND(STRDT(?timestamp, xsd:dateTime) AS ?t)\n"
                + "}";

        System.out.println(str);

        Query query = QueryFactory.create(str);


//        PrefixMapping prefixes = new PrefixMappingImpl();
//
//        Iterable<String> prefixSources = LsqUtils.prependDefaultPrefixSources(cmd.getPrefixSources());
//
//        for (String prefixSource : prefixSources) {
//            logger.info("Adding prefixes from " + prefixSource);
//            Model tmp = RDFDataMgr.loadModel(prefixSource);
//            prefixes.setNsPrefixes(tmp);
//        }


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

        SparkSession ss = SparkSession.builder().config(sparkConf).getOrCreate();

        JavaSparkContext sc = JavaSparkContext.fromSparkContext(ss.sparkContext());

        // CSVFormat baseCsvFormat = CSVFormat.Builder.create(CSVFormat.EXCEL).setSkipHeaderRecord(true).build();
        UnivocityCsvwConf csvConf = new UnivocityCsvwConf(new DialectMutableImpl().setHeader(true), null);


        String path = "/home/raven/Datasets/bio2rdf_sparql_logs_processed_01-2019_to_07-2021.csv";

        JavaRDD<Binding> bindingRdd = CsvDataSources.createRddOfBindings(sc, path, csvConf);

        // CmdMixinArq.configureCxt(ARQ.getContext(), arqConfig);
        CmdMixinArq arqConfig = new CmdMixinArq();
        Supplier<ExecutionContext> execCxtSupplier = createExecCxtSupplier(arqConfig);


        JavaRDD<Quad> quadRdd = JavaRddOfBindingsOps.tarqlQuads(bindingRdd, query, execCxtSupplier);
        //JavaRDD<Dataset> outRdd = null;
//
//        List<JavaRDD<Dataset>> rdds = cmd.getNonOptionArgs().stream()
//            .map(rdfSourceFactory::get)
//            .map(RdfSource::asDatasets)
//            .map(RDD::toJavaRDD)
//            .collect(Collectors.toList())
//            ;

//        @SuppressWarnings("unchecked")
//        JavaRDD<Dataset>[] arr = rdds.toArray(new JavaRDD[0]);
//        JavaRDD<Dataset> initialRdd = javaSparkContext.union(arr);
//
//
//        JavaRDD<Dataset> effectiveRdd = initialRdd; //.repartition(4);
//
//
//        JavaRDD<ResourceInDataset> ridRdd =
//                NamedModelOpsRddJava.mapToResourceInDataset(
//                        DatasetOpsRddJava.toNamedModels(effectiveRdd));
//
//
//        Broadcast<PrefixMapping> prefixesBc = javaSparkContext.broadcast(prefixes);
//        JavaRDD<Dataset> outRdd = ridRdd
//            .mapPartitions(ridIt -> {
//                PrefixMapping pm = prefixesBc.getValue();
//                SparqlStmtParser sparqlStmtParser = SparqlStmtParserImpl.create(
//                        Syntax.syntaxARQ, pm, true);
//
//                return Streams.stream(ridIt)
//                    .map(rid -> LsqUtils.rehashQueryHash(rid, sparqlStmtParser))
//                    .map(ResourceInDataset::getDataset)
//                    .iterator();
//            });



        // System.out.println("Size spark: " + effectiveRdd.count());

        RddRdfWriter.createForQuad()
            .setRdd(quadRdd)
            .setGlobalPrefixMapping(new PrefixMappingImpl())
            .setOutputFormat(RDFFormat.TRIG_BLOCKS)
            // .setOutputFormat(cmd.getOutFormat())
            .setMapQuadsToTriplesForTripleLangs(true)
            // .setAllowOverwriteFiles(true)
            /// .setPartitionFolder(cmd.getOutFolder())
            .setTargetFile("/tmp/result.trig")
            // .setUseElephas(true)
            .setAllowOverwriteFiles(true)
            .setDeletePartitionFolderAfterMerge(true)
            .run();
    }

    public static Supplier<ExecutionContext> createExecCxtSupplier(CmdMixinArq arqConfig) {
        SerializableSupplier<ExecutionContext> execCxtSupplier = () -> {
            Context baseCxt = ARQ.getContext().copy();
            CmdMixinArq.configureCxt(baseCxt, arqConfig);
            baseCxt.set(ArqSecurity.symAllowFileAccess, true);

            // Scripting can only be set via system property
            // baseCxt.setTrue(ARQ.systemPropertyScripting)

            ExecutionContext execCxt = ExecutionContextUtils.createExecCxtEmptyDsg(baseCxt);
            return execCxt;
        };
        return execCxtSupplier;
    }
}
