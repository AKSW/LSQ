package org.aksw.simba.lsq.rdf.conversion;

import java.util.stream.Stream;

import org.aksw.commons.collector.core.AggBuilder;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;


public interface RdfConverter {

    static Quad toQuad(Triple triple) {
        return new Quad(Quad.defaultGraphNodeGenerated, triple);
    }


    static RdfConverterFromTriple fromTriples(Stream<Triple> stream) {
        return new RdfConverterFromTriple() {

            @Override
            public Stream<Triple> asTriples() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Stream<Quad> asQuads() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Stream<Model> asModels() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Stream<Graph> asGraphs() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Stream<Model> asDatasets() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Stream<Graph> asDatasetGraphs() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Stream<Graph> asGraphsGroupedBySubject() {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }


    RdfConverterTo fromGraphs(Stream<Graph> stream);
    RdfConverterTo fromModels(Stream<Model> stream);

    RdfConverterTo fromQuads(Stream<Quad> stream);
    RdfConverterTo fromDatasetGraphs(Stream<DatasetGraph> stream);
    RdfConverterTo fromDatasets(Stream<Dataset> stream);

}
