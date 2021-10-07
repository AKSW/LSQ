package net.sansa_stack.rdf.spark.io.csv;

import java.io.IOException;

import org.aksw.commons.lambda.serializable.SerializableFunction;
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

import net.sansa_stack.query.spark.io.input.csv.CsvSources;
import net.sansa_stack.query.spark.rdd.op.JavaRddOfBindingsOps;
import net.sansa_stack.rdf.spark.rdd.op.JavaRddOfDatasetsOps;
import net.sansa_stack.rdf.spark.rdd.op.JavaRddOfNamedModelsOps;


public class CsvParserSpark {

    @FunctionalInterface
    public interface JavaRddFunction<I, O>
        extends SerializableFunction<JavaRDD<I>, JavaRDD<O>> {

        default <X> JavaRddFunction<I, X> andThen(JavaRddFunction<O, X> next) {
            return rdd -> next.apply(this.apply(rdd));
        }

        default <K, V> ToJavaPairRddFunction<I, K, V> toPairRdd(ToJavaPairRddFunction<O, K, V> next) {
            return rdd -> next.apply(this.apply(rdd));
        }

        public static <I> JavaRddFunction<I, I> identity() {
            return x -> x;
        }
    }

    @FunctionalInterface
    public interface ToJavaPairRddFunction<I, K, V>
        extends SerializableFunction<JavaRDD<I>, JavaPairRDD<K, V>> {

        default <KO, VO> ToJavaPairRddFunction<I, KO, VO> andThen(JavaPairRddFunction<K, V, KO, VO> next) {
            return rdd -> next.apply(this.apply(rdd));
        }

        default <O> JavaRddFunction<I, O> toRdd(ToJavaRddFunction<K, V, O> next) {
            return rdd -> next.apply(this.apply(rdd));
        }
    }

    @FunctionalInterface
    public interface ToJavaRddFunction<K, V, O>
        extends SerializableFunction<JavaPairRDD<K, V>, JavaRDD<O>> {

        default <X> ToJavaRddFunction<K, V, X> andThen(JavaRddFunction<O, X> next) {
            return rdd -> next.apply(this.apply(rdd));
        }

        default <KX, VX> JavaPairRddFunction<K, V, KX, VX> toPairRdd(ToJavaPairRddFunction<O, KX, VX> next) {
            return rdd -> next.apply(this.apply(rdd));
        }
    }

    @FunctionalInterface
    public interface JavaPairRddFunction<KI, VI, KO, VO>
        extends SerializableFunction<JavaPairRDD<KI, VI>, JavaPairRDD<KO, VO>> {

        default <KX, VX> JavaPairRddFunction<KI, VI, KX, VX> andThen(JavaPairRddFunction<KO, VO, KX, VX> next) {
            return rdd -> next.apply(this.apply(rdd));
        }

        default <X> ToJavaRddFunction<KI, VI, X> toRdd(ToJavaRddFunction<KO, VO, X> next) {
            return rdd -> next.apply(this.apply(rdd));
        }

        public static <K, V> JavaPairRddFunction<K, V, K, V> identity() {
            return x -> x;
        }
    }




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

    public static JavaRDD<Resource> loadMappedCsv(JavaSparkContext sc, String path, CSVFormat csvFormat, Query query) throws IOException {
        JavaRDD<Binding> rdd = CsvSources.createRddOfBindings(sc, path, csvFormat);

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
