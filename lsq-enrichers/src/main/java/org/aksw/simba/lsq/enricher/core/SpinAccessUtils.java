package org.aksw.simba.lsq.enricher.core;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.simba.lsq.util.ConceptModelUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;
import org.spinrdf.model.TriplePattern;
import org.spinrdf.vocabulary.SP;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


/** Utils for accessing certain information in a spin model */
public class SpinAccessUtils {

    public static final Fragment1 tpListStarts = Concept.create(
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            + "PREFIX sp: <http://spinrdf.org/sp#>",
            "listStart",
            "?root (<urn:p>|!<urn:p>)* ?listStart . "
            + "?listStart rdf:first [] "
            + "FILTER(NOT EXISTS { [] rdf:rest ?listStart }) "
            + "?listStart (rdf:rest*/rdf:first) [ sp:subject ?s ; sp:predicate ?p ; sp:object ?o ] "
     );


    public static final Fragment1 tpNoList = Concept.create(
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
            + "PREFIX sp: <http://spinrdf.org/sp#>",
            "listStart",
            "?root (<urn:p>|!<urn:p>)* ?listStart . "
            + "FILTER(NOT EXISTS { [] rdf:first ?listStart }) "
            + "?listStart sp:subject ?s ; sp:predicate ?p ; sp:object ?o "
     );


    public static Set<RDFNode> listRDFNodes(org.spinrdf.model.Triple triple) {
        Set<RDFNode> result = new LinkedHashSet<>();
        result.add(triple.getSubject());
        result.add(triple.getPredicate());
        result.add(triple.getObject());
        return result;
    }


    public static Multimap<Resource, org.spinrdf.model.Triple> indexBasicPatterns2(Resource r) {
        Model spinModel = ResourceUtils.reachableClosure(r);
        Multimap<Resource, org.spinrdf.model.Triple> result = indexBasicPatterns2(spinModel);
        return result;
    }

    public static Multimap<Resource, org.spinrdf.model.Triple> indexBasicPatterns2(Model spinModel) {
        Multimap<Resource, org.spinrdf.model.Triple> result = ArrayListMultimap.create();

        {
            Set<Resource> ress = ConceptModelUtils
                    .listResourcesUnchecked(spinModel, tpListStarts, Resource.class)
                    .collect(Collectors.toSet()).blockingGet();

            for(Resource r : ress) {
                RDFList list = r.as(RDFList.class);
                for(RDFNode item : list.asJavaList()) {
                    boolean isSpinTriple = isSpinTriple(item);
                    if(isSpinTriple) {
                        TriplePattern tp = item.as(TriplePattern.class);
                        result.put(r, tp);
                    }
                }
            }
        }

        {
            Set<TriplePattern> ress = ConceptModelUtils
                    .listResourcesUnchecked(spinModel, tpNoList, TriplePattern.class)
                    .collect(Collectors.toSet()).blockingGet();

            for(TriplePattern item : ress) {
                result.put(item, item);
            }
        }

        return result;
    }


    public static boolean isSpinTriple(RDFNode rdfNode) {
        boolean result = false;
        if(rdfNode.isResource()) {
            Resource r = rdfNode.asResource();
            result = r.hasProperty(SP.subject) && r.hasProperty(SP.predicate) && r.hasProperty(SP.object);
        }
        return result;
    }

    public static RDFNode writeNode(Model tgtModel, Node node) {
        RDFNode result = null;
        if(node != null) {
            if(node.isVariable()) {
                String varName = node.getName();
                Resource tmp = tgtModel.createResource();
                org.aksw.jena_sparql_api.rdf.collections.ResourceUtils.setLiteralProperty(
                        tmp, SP.varName, varName);
                result = tmp;
            } else {
                result = tgtModel.asRDFNode(node);
            }
        }

        return result;
    }
}
