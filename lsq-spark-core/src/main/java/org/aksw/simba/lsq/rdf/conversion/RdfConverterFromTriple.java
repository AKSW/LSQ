package org.aksw.simba.lsq.rdf.conversion;

import java.util.stream.Stream;

import org.apache.jena.graph.Graph;

public interface RdfConverterFromTriple
    extends RdfConverterTo
{
    Stream<Graph> asGraphsGroupedBySubject();

}
