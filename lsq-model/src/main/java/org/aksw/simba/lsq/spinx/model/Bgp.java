package org.aksw.simba.lsq.spinx.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.simba.lsq.model.util.SpinCoreUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.BasicPattern;
import org.spinrdf.model.Triple;


@ResourceView
//@IdPrefix("bgp-")
public interface Bgp
    extends LsqElement
{
    /**
     * The ID of a bgp is defined by the <b>list</b> of triple patterns.
     * Note, that the ID of a TpInBgp depends on the IDs of a bgp and the tp.
     * So even a tp's are eventually made accessible via getTpInBgp,
     * the direct link to triple patterns is necessary for ID assignment.
     *
     * @return
     */
    @HashId
    @Iri(LSQ.Terms.hasTp)
    List<LsqTriplePattern> getTriplePatterns();


//    default List<LsqTriplePattern> getTriplePatterns() {
//        List<TpInBgp> list = getTpInBgp();
//        List<LsqTriplePattern> result = list.stream().map(item -> item.getTriplePattern()).collect(Collectors.toList());
//        return result;
//    }

    @Iri(LSQ.Terms.hasTpInBgp)
    Set<TpInBgp> getTpInBgp();


//    @Iri(LSQ.Strs.joinVertex)
//    Set<JoinVertex> getJoinVertices();

    @Iri(LSQ.Terms.hasExec)
    Set<BgpExec> getSpinBgpExecs();

    @Iri(LSQ.Terms.joinVertexCount)
    Integer getJoinVertexCount();
    SpinQueryEx setJoinVertexCount(Integer cnt);

    @Iri(LSQ.Terms.joinVertexDegreeMean)
    Integer getAvgJoinVertexDegree();
    SpinQueryEx setAvgJoinVertexDegree(Integer cnt);

    @Iri(LSQ.Terms.joinVertexDegreeMedian)
    Integer getMedianJoinVertexDegree();
    SpinQueryEx setMedianJoinVertexDegree(Integer cnt);

    @Iri(LSQ.Terms.hasBgpNode)
    Set<BgpNode> getBgpNodes();

    @Iri(LSQ.Terms.hasEdge)
    Set<DirectedHyperEdge> getEdges();


    /**
     * Creates a mapping from tp (triple pattern) to tpInBgp.
     * Most useful for checking whether for a given tp the corresponding tpInBgp resource
     * has already been created.
     *
     *
     * @return
     */
    default Map<LsqTriplePattern, TpInBgp> indexTps() {
        Map<LsqTriplePattern, TpInBgp> result = new LinkedHashMap<>();
        Set<TpInBgp> tpInBgps = getTpInBgp();
        for(TpInBgp tpInBgp : tpInBgps) {
            LsqTriplePattern tp = tpInBgp.getTriplePattern();
            result.put(tp, tpInBgp);
        }
        return result;

    }

    // TODO Eventually replace with a proper map view
    default Map<Node, BgpNode> indexBgpNodes() {
        Map<Node, BgpNode> result = new LinkedHashMap<>();
        Set<BgpNode> bgpNodes = getBgpNodes();
        for(BgpNode v : bgpNodes) {
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


    default BgpExec findBgpExec(RDFNode expRun) {
//        Resource expRun = getBenchmarkRun();
        Objects.requireNonNull(expRun, "benchmark run resource not set");

        Set<BgpExec> cands = getSpinBgpExecs();
        BgpExec result = null;
        for(BgpExec cand : cands) {
            //if(Objects.equals(cand.getBgp(), bgp) && Objects.equals(cand.getQueryExec().getLocalExecution().getBenchmarkRun(), expRun)) {
            if(cand.getQueryExec().getLocalExecution().getBenchmarkRun().equals(expRun)) {
                result = cand;
                break;
            }
        }

        return result;
    }


    default BasicPattern toBasicPattern() {
        BasicPattern result = new BasicPattern();

        List<LsqTriplePattern> tps = getTriplePatterns();
        for(Triple tp : tps) {
            org.apache.jena.graph.Triple t = SpinCoreUtils.toJenaTriple(tp);
            result.add(t);
        }

        return result;
    }
}
