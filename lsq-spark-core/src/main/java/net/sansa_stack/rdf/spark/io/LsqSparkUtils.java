package net.sansa_stack.rdf.spark.io;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

public class LsqSparkUtils {

    public static JavaSparkContext createSparkContext() {
        SparkConf sparkConf = new SparkConf()
            .setAppName("Lsq")
            .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
            .set("spark.kryoserializer.buffer.max", "1000") // MB
            .set("spark.kryo.registrator",
                    String.join(", ", "net.sansa_stack.spark.io.rdf.kryo.JenaKryoRegistrator"))
            .set("spark.sql.crossJoin.enabled", "true")
            // .set("spark.hadoop.mapred.max.split.size", "" + 4 * 1024 * 1024)
            //		mapreduce.input.fileinputformat.split.minsize
            ;

        sparkConf.setMaster("local[*]");

        SparkSession ss = SparkSession.builder().config(sparkConf).getOrCreate();

        JavaSparkContext sc = JavaSparkContext.fromSparkContext(ss.sparkContext());

        return sc;
    }

}
