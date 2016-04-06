package org.aksw.simba.largerdfbench.util;

import java.util.List;
import java.util.function.Consumer;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class ConceptModelUtils {
    public static List<Resource> listResources(Model model, Concept concept) {
        return null;
    }

    public static void collectRdfNodes(Model model, Concept concept, Consumer<RDFNode> consumer) {
        FluentQueryExecutionFactory
            .from(model)
            .create();
    }

}
