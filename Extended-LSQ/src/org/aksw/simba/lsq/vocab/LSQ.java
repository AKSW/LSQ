package org.aksw.simba.lsq.vocab;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * LSQ vocabulary
 *
 * @author Claus Stadler
 *
 */
public class LSQ {
    public static final String ns = "http://lsq.aksw.org/vocab#";

    public static Resource resource(String local) { return ResourceFactory.createResource(ns + local); }
    public static Property property(String local) { return ResourceFactory.createProperty(ns + local); }

    // Used internally for the hypergraph representation - not part of the public vocab
    public static final Resource Vertex = resource(ns + "Vertex");
    public static final Resource Edge = resource(ns + "Vertex");

    public static final Property in = property("in");
    public static final Property out = property("out");

    public static final Resource Star = resource(ns + "Star");
    public static final Resource Sink = resource(ns + "Sink");
    public static final Resource Path = resource(ns + "Path");
    public static final Resource Hybrid = resource(ns + "Hybrid");

    public static final Property joinVertex = property("joinVertex");
    public static final Property joinVertexType = property("joinVertexType");
    public static final Property joinVertexDegree = property("joinVertexDegree");


    public static final Property text = property("text");
    public static final Property resultSize = property("resultSize");
    public static final Property structuralFeatures = property("structuralFeatures");
    public static final Property hasTriplePattern = property("hasTriplePattern");
    public static final Property triplePatternText = property("triplePatternText");
    public static final Property triplePatternSelectivity = property("triplePatternSelectivity");
    public static final Property triplePatternExtensionSize = property("triplePatternExtensionSize");
    public static final Property meanTriplePatternSelectivity = property("meanTriplePatternSelectivity");
    public static final Property runtimeError = property("runtimeError");
    public static final Property runTimeMs = property("runTimeMs");

    public static final Property hasLocalExecution = property("hasLocalExecution");
    public static final Property hasRemoteExecution = property("hasRemoteExecution");

    public static final Property usesFeature = property("usesFeature");

    public static final Property engine = property("engine");
    public static final Property vendor = property("vendor");
    public static final Property version = property("version");
    public static final Property processor = property("processor");
    public static final Property ram = property("ram");

    public static final Property dataset = property("dataset");

    public static final String defaultLsqrNs = "http://lsq.aksw.org/res/";

    //public static final Property

    //lsqv:resultSize
    //lsqv:runTimeMs
    //lsqv:hasLocalExecution
    //lsqv:structuralFeatures
    //lsqv:runtimeError
    //lsqv:resultSize
    //lsqv:meanTriplePatternSelectivity
    //lsqv:joinVertexDegree 2 ; lsqv:joinVertexType lsqv:Star
    //lsqv:hasRemoteExecution
    //lsqv:endpoint --> maybe supersede by dataset distribution vocab (i think dcat has something)
    //

}
