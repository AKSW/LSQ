package org.aksw.simba.lsq.spinx.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.util.SpinUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.BasicPattern;
import org.topbraid.spin.model.Triple;


@ResourceView
public interface SpinBgp
    extends LsqElement
{
    @HashId
    @Iri(LSQ.Strs.hasTP)
    List<LsqTriplePattern> getTriplePatterns();

//    default List<LsqTriplePattern> getTriplePatterns() {
//        List<TpInBgp> list = getTpInBgp();
//        List<LsqTriplePattern> result = list.stream().map(item -> item.getTriplePattern()).collect(Collectors.toList());
//        return result;
//    }

    @Iri(LSQ.Strs.hasTpInBgp)
    Set<TpInBgp> getTpInBgp();


//    @Iri(LSQ.Strs.joinVertex)
//    Set<JoinVertex> getJoinVertices();

    @Iri(LSQ.Strs.hasExec)
    Set<SpinBgpExec> getSpinBgpExecs();

    @Iri(LSQ.Strs.joinVertices)
    Integer getJoinVertexCount();
    SpinQueryEx setJoinVertexCount(Integer cnt);

    @Iri(LSQ.Strs.meanJoinVertexDegree)
    Integer getAvgJoinVertexDegree();
    SpinQueryEx setAvgJoinVertexDegree(Integer cnt);

    @Iri(LSQ.Strs.medianJoinVertexsDegree)
    Integer getMedianJoinVertexDegree();
    SpinQueryEx setMedianJoinVertexDegree(Integer cnt);

    @Iri(LSQ.Strs.hasBGPNode)
    Set<SpinBgpNode> getBgpNodes();

    @Iri(LSQ.Strs.hasEdge)
    Set<DirectedHyperEdge> getEdges();

    // TODO Eventually replace with a proper map view
    default Map<Node, SpinBgpNode> indexBgpNodes() {
        Map<Node, SpinBgpNode> result = new LinkedHashMap<>();
        for(SpinBgpNode v : getBgpNodes()) {
            Node k = v.toJenaNode();
            result.put(k, v);
        }
        return result;

// If key is a variable, check the SP.varName property...
//
//        new MapFromKeyConverter<>(
//        		new MapFromResourceUnmanaged(this, LSQ.hasBGPNode, LSQ.proxyFor),
//				new ConverterFromNodeMapperAndModel<>(
//						model,
//						RDFNode.class, new ConverterFromNodeMapper<>(NodeMappers.uriString)));
//
//
//        return null;
    }

    default BasicPattern toBasicPattern() {
        BasicPattern result = new BasicPattern();

        List<LsqTriplePattern> tps = getTriplePatterns();
        for(Triple tp : tps) {
            org.apache.jena.graph.Triple t = SpinUtils.toJenaTriple(tp);
            result.add(t);
        }

        return result;
    }
}
