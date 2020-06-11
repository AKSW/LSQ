package org.aksw.simba.lsq.spinx.model;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.util.SpinUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;


@ResourceView
public interface SpinBgpNode
    extends Resource
{
//    @Iri(LSQ.Strs.hasBGPNode)
//    RDFNode getNode();
//    SpinBgpNode setNode(RDFNode node);

    /**
     * The set of RDFNodes mentioned in a BGP of a SPIN model that denote the same RDF term.
     *
     * @return
     */
    @Iri(LSQ.Strs.proxyFor)
    Set<RDFNode> getProxyFor();


    // TODO These two methods should by mapped by DirectedHyperEdge
    @Iri(LSQ.Strs.in)
    Set<DirectedHyperEdge> getInEdges();

    @Iri(LSQ.Strs.out)
    Set<DirectedHyperEdge> getOutEdges();

    public default Node toJenaNode() {
        Set<RDFNode> set = getProxyFor();
        if(set.isEmpty()) {
            throw new RuntimeException("toJenaNode() requires non-empty set of refernced RDF terms");
        }

        RDFNode node = set.iterator().next();
        return SpinUtils.readNode(node);
    }
}
