package org.aksw.simba.lsq.spark.cmd.impl;

import org.apache.jena.query.Dataset;
import org.apache.spark.rdd.RDD;


public interface RdfSource {
//    RDD<Triple> asTriples(boolean dropQuads);
//    RDD<Quad> asQuads();
//    RDD<Dataset> asDataset();
//    RDD<Graph> asGraphs();
//    RDD<Model> asModels();
//    RDD<DatasetGraph> asDatasetGraph();
    RDD<Dataset> asDatasets();
}
