package net.sansa_stack.rdf.spark.io.csv;

import java.io.IOException;

import org.aksw.simba.lsq.parser.Mapper;
import org.aksw.simba.lsq.parser.WebLogParser;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.commons.csv.CSVFormat;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import com.google.common.collect.Streams;

import net.sansa_stack.hadoop.format.univocity.conf.UnivocityHadoopConf;
import net.sansa_stack.spark.io.csv.input.CsvDataSources;
import net.sansa_stack.spark.rdd.function.JavaRddFunction;
import net.sansa_stack.spark.rdd.op.rdf.JavaRddOfBindingsOps;
import net.sansa_stack.spark.rdd.op.rdf.JavaRddOfDatasetsOps;
import net.sansa_stack.spark.rdd.op.rdf.JavaRddOfNamedModelsOps;


public class CsvParserSpark {




    public interface BindingToResourceTransform
        extends JavaRddFunction<Binding, Resource> {}

    public static BindingToResourceTransform newTransformerBindingToResource(Query query) {
        // Convert to string for lambda serialization
        String queryStr = query.toString();

        return rdd -> {
            JavaRDD<Dataset> datasetRdd = JavaRddOfBindingsOps.tarqlDatasets(rdd, QueryFactory.create(queryStr)); //   RxOps.map(rdd, QueryFlowOps.createMapperQuads(query)::apply);
            JavaPairRDD<String, Model> namedModelRdd = JavaRddOfDatasetsOps.flatMapToNamedModels(datasetRdd);
            JavaRDD<Resource> resourceRdd = JavaRddOfNamedModelsOps.mapToResources(namedModelRdd);
            return resourceRdd;
        };
    }

    public static JavaRDD<Resource> loadMappedCsv(JavaSparkContext sc, String path, UnivocityHadoopConf csvConf, Query query) throws IOException {
        JavaRDD<Binding> rdd = CsvDataSources.createRddOfBindings(sc, path, csvConf);

        return newTransformerBindingToResource(query).apply(rdd);
    }


    public static JavaRddFunction<String, Resource> createWebLogMapper(String logPattern) throws IOException {

        // Attempt to create a mapper for validation - we are not using the instance
        // because we only reference the serializable string pattern in the mapper
        Mapper validation = WebLogParser.create(logPattern);


        return rdd -> rdd.mapPartitions(lineIt -> {
            Mapper mapper = WebLogParser.create(logPattern);

            return Streams.stream(lineIt).map(line -> {
                Resource r = processLogLine(mapper, line);
                return r;
            }).iterator();
        });
    }

    public static Resource processLogLine(Mapper mapper, String line) {
        Resource r = ModelFactory.createDefaultModel().createResource();

        r.addLiteral(LSQ.logRecord, line);

        boolean parsed;
        try {
            parsed = mapper.parse(r, line) != 0;
            if(!parsed) {
                r.addLiteral(LSQ.processingError, "Failed to parse log line (no detailed information available)");
            }
        } catch(Exception e) {
            parsed = false;
            r.addLiteral(LSQ.processingError, "Failed to parse log line: " + e);
            // logger.warn("Parser error", e);
        }

        return r;
    }

}
