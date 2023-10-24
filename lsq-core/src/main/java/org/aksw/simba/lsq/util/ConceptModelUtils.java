package org.aksw.simba.lsq.util;

import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.ConceptUtils;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;

import io.reactivex.rxjava3.core.Flowable;

public class ConceptModelUtils {
    public static <T extends Resource> Flowable<T> listResources(Model model, Fragment1 concept, Class<? extends T> clazz) {
        return listRdfNodes(model, concept)
//                .filter(rdfNode -> rdfNode.canAs(clazz))
                .filter(rdfNode -> {
                    boolean r = rdfNode.canAs(clazz);
                    return r;
                })
                .map(rdfNode -> {
                    T r = rdfNode.as(clazz);
                    return r;
                });
    }

    /**
     * Obtain the set of RDFNodes from a concept over a model.
     * Unchecked means that Resource.canAs() is not checked.
     *
     * For example, SPIN2ARQ does not produce ?x a sp:TriplePattern triples, but canAs
     * on the Spin Resource Implementations returns false if the type is not present.
     *
     *
     * @param <T>
     * @param model
     * @param concept
     * @param clazz
     * @return
     */
    public static <T extends Resource> Flowable<T> listResourcesUnchecked(
            Model model,
            Fragment1 concept,
            Class<? extends T> clazz) {
        return listRdfNodes(model, concept)
                .map(rdfNode -> {
                    T r = rdfNode.as(clazz);
                    return r;
                });
    }

//    public static List<Resource> listResources(Model model, Concept concept) {
//        List<Resource> result = new ArrayList<>();
//        collectRdfNodes(model, concept, item -> {
//            if(item.isResource()) {
//                result.add(item.asResource());
//            }
//        });
//
//        return result;
//    }

    public static Flowable<RDFNode> listRdfNodes(Model model, Fragment1 concept) {
        Var var = concept.getVar();
        Query query = ConceptUtils.createQueryList(concept);

        return SparqlRx.execConcept(() -> QueryExecutionFactory.create(query, model), var);
    }

}
