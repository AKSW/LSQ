package org.aksw.simba.lsq.rdf.conversion;

import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;

public interface RdfConverterTo {

    Stream<Triple> asTriples();
    Stream<Graph> asGraphs();
    Stream<Model> asModels();

    Stream<Quad> asQuads();
    Stream<Graph> asDatasetGraphs();
    Stream<Model> asDatasets();



//    RDD<Triple> asTriples(boolean dropQuads);
//    RDD<Quad> asQuads();
//    RDD<Dataset> asDataset();
//    RDD<Graph> asGraphs();
//    RDD<Model> asModels();
//    RDD<DatasetGraph> asDatasetGraph();
//    RDD<Dataset> asDatasetGraphs();

}
