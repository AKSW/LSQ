package org.aksw.simba.lsq.parser.csv;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.rx.GraphFactoryEx;
import org.aksw.jenax.sparql.rx.op.FlowOfQuadsOps;
import org.aksw.jenax.sparql.rx.op.QueryFlowOps;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.vocabulary.RDF;

import io.reactivex.rxjava3.core.FlowableTransformer;

public class CsvParser {
    interface BindingToResourceTransform extends FlowableTransformer<Binding, Resource> {}



    public static Map<String, BindingToResourceTransform> loadCsvRegistry(Model model) {
        List<Resource> rs = model.listResourcesWithProperty(RDF.type, LSQ.CsvLogFormat).toList();

        Map<String, BindingToResourceTransform> result = rs.stream()
            .filter(r -> r.hasProperty(LSQ.pattern))
            .collect(Collectors.toMap(
                    r -> r.getLocalName(),
                    r -> create(r.getProperty(LSQ.pattern).getString())
            ));

        return result;
    }

    public static BindingToResourceTransform create(String pattern) {
        Query query = QueryFactory.create(pattern);

        return upstream ->
            upstream
            .compose(QueryFlowOps.createMapperQuads(query))
            .compose(FlowOfQuadsOps.groupConsecutiveQuadsToGraph(Quad::getGraph, Quad::asTriple, GraphFactoryEx::createInsertOrderPreservingGraph))
            .map(e -> {
                Model m = ModelFactory.createModelForGraph(e.getValue());
                return m.asRDFNode(e.getKey());
            })
            // FIXME Apply node transform that replaces the node with a blank node for further re-skolemization
            .map(RDFNode::asResource)
            ;
    }

}
