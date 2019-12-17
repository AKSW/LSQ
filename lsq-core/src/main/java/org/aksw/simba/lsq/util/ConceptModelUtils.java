package org.aksw.simba.lsq.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;

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
//        QueryExecutionFactory qef = FluentQueryExecutionFactory
//            .from(model)
//            .create();
    	try(RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.wrap(model))) {
	    	
	        List<Node> nodes = ServiceUtils.fetchList(conn, concept);
	
	        nodes.stream()
	            .map(node -> model.asRDFNode(node))
	            .forEach(consumer);
    	}
    }

}
