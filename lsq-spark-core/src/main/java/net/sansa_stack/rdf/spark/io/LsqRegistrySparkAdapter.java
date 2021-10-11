package net.sansa_stack.rdf.spark.io;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.commons.csv.CSVFormat;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.vocabulary.RDF;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import net.sansa_stack.rdf.spark.io.csv.CsvParserSpark;
import net.sansa_stack.rdf.spark.io.csv.CsvParserSpark.BindingToResourceTransform;
import net.sansa_stack.spark.io.csv.input.CsvDataSources;
import net.sansa_stack.spark.rdd.function.JavaRddFunction;


/** Process the LSQ registry such that it can ingest spark sources */
public class LsqRegistrySparkAdapter {

    public static Map<String, SourceOfRddOfResources> createDefaultLogFmtRegistry(SparkSession ss) {
        return createDefaultLogFmtRegistry(JavaSparkContext.fromSparkContext(ss.sparkContext()));
    }

    public static Map<String, SourceOfRddOfResources> createDefaultLogFmtRegistry(JavaSparkContext sc) {
        Map<String, SourceOfRddOfResources> result = new LinkedHashMap<>();

        Model model = RDFDataMgr.loadModel("default-log-formats.ttl");

        // Load line based log formats
        result.putAll(loadRegistry(sc, model));
        // Add custom RDF based log format(s)
//        result.put("rdf", in -> LsqUtils.createResourceStreamFromRdf(in, Lang.NTRIPLES, "http://example.org/"));

        // Add multi-line sparql format
//        result.put("sparql", in -> LsqUtils.createSparqlStream(in));

        return result;
    }


    public static Map<String, SourceOfRddOfResources> loadRegistry(JavaSparkContext sc, Model model) {
        Map<String, SourceOfRddOfResources> webLogRegistry = loadWebLogRegistry(sc, model);
        Map<String, SourceOfRddOfResources> csvRegistry = loadCsvRegistry(sc, model);

        // Concat both maps; raise  error on duplicate keys
        Map<String, SourceOfRddOfResources> result = Stream.concat(
                webLogRegistry.entrySet().stream(),
                csvRegistry.entrySet().stream())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return result;
    }


    public static Map<String, SourceOfRddOfResources> loadWebLogRegistry(JavaSparkContext sc, Model model) {
        List<Resource> rs = model.listResourcesWithProperty(RDF.type, LSQ.WebAccessLogFormat).toList();

        Map<String, SourceOfRddOfResources> result = rs.stream()
            .filter(r -> r.hasProperty(LSQ.pattern))
            .collect(Collectors.toMap(
                    r -> r.getLocalName(),
                    r -> createWebLogSource(sc, r.getProperty(LSQ.pattern).getString())
            ));

        return result;
    }



    public static Map<String, SourceOfRddOfResources> loadCsvRegistry(JavaSparkContext sc, Model model) {
        List<Resource> rs = model.listResourcesWithProperty(RDF.type, LSQ.CsvLogFormat).toList();

        Map<String, SourceOfRddOfResources> result = rs.stream()
            .filter(r -> r.hasProperty(LSQ.pattern))
            .collect(Collectors.toMap(
                    r -> r.getLocalName(),
                    r -> createCsvSource(sc, r.getProperty(LSQ.pattern).getString())
            ));

        return result;
    }


    public static SourceOfRddOfResources createCsvSource(JavaSparkContext sc, String queryStr) {
        CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.EXCEL).setSkipHeaderRecord(true).build();

        return source -> {
            Query query = QueryFactory.create(queryStr);
            BindingToResourceTransform mapper = CsvParserSpark.newTransformerBindingToResource(query);

            JavaRDD<Binding> rdd = CsvDataSources.createRddOfBindings(sc, source, csvFormat);
            JavaRDD<Resource> r = mapper.apply(rdd);
            return r;
        };
    }


    public static SourceOfRddOfResources createWebLogSource(JavaSparkContext sc, String pattern) {
        return source -> {
            JavaRDD<String> rdd = sc.textFile(source);
            // CSVFormat csvFormat = CSVFormat.Builder.create(CSVFormat.EXCEL).setSkipHeaderRecord(true).build();
            JavaRddFunction<String, Resource> mapper = CsvParserSpark.createWebLogMapper(pattern);


            JavaRDD<Resource> r = mapper.apply(rdd);
            return r;
        };
    }

}
