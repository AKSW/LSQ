package org.aksw.simba.largerdfbench.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class ConceptModelUtils {
    public static List<Resource> listResources(Model model, Concept concept) {
        List<Resource> result = new ArrayList<>();
        collectRdfNodes(model, concept, item -> {
            if(item.isResource()) {
                result.add(item.asResource());
            }
        });

        return result;
    }

    public static void collectRdfNodes(Model model, Concept concept, Consumer<RDFNode> consumer) {
        QueryExecutionFactory qef = FluentQueryExecutionFactory
            .from(model)
            .create();

        List<Node> nodes = ServiceUtils.fetchList(qef, concept);

        nodes.stream()
            .map(node -> model.asRDFNode(node))
            .forEach(consumer);
    }

}
