package net.sansa_stack.rdf.spark.io;

import org.apache.jena.rdf.model.Resource;
import org.apache.spark.api.java.JavaRDD;

public interface SourceOfRddOfResources {
    JavaRDD<Resource> load(String source) throws Exception;
}
