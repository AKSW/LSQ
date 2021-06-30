package org.aksw.simba.lsq.rdf.conversion;

import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public interface RdfConverterFrom {
    RdfConverterTo fromTriples(Stream<Triple> stream);
    RdfConverterTo fromGraphs(Stream<Graph> stream);
    RdfConverterTo fromModels(Stream<Model> stream);

    RdfConverterTo fromQuads(Stream<Quad> stream);
    RdfConverterTo fromDatasetGraphs(Stream<DatasetGraph> stream);
    RdfConverterTo fromDatasets(Stream<Dataset> stream);
}
