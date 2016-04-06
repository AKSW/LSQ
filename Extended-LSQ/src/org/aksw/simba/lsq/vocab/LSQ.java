package org.aksw.simba.lsq.vocab;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * LSQ vocabulary
 *
 * @author Claus Stadler
 *
 */
public class LSQ {
    public static final String ns = "http://lsq.aksw.org/vocab#";

    public static Property property(String local) {
        return ResourceFactory.createProperty(ns + local);
    }

    public static final Property resultSize = property("resultSize");
    public static final Property structuralFeatures = property("structuralFeatures");
    public static final Property hasTriplePattern = property("hasTriplePattern");
    public static final Property triplePatternText = property("triplePatternText");
    public static final Property triplePatternSelectivity = property("triplePatternSelectivity");
    public static final Property triplePatternExtensionSize = property("triplePatternExtensionSize");
    public static final Property meanTriplePatternSelectivity = property("meanTriplePatternSelectivity");
    public static final Property runtimeError = property("runtimeError");



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
