package org.aksw.simba.lsq.spinx.model;

import java.util.Set;

import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.jena_sparql_api.conjure.algebra.common.ResourceTreeUtils;
import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.util.SpinUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.topbraid.spin.model.impl.TriplePatternImpl;

@ResourceView
public abstract class LsqTriplePattern
    extends TriplePatternImpl
    implements LsqElement
{
    public LsqTriplePattern(Node node, EnhGraph graph) {
        super(node, graph);
    }

    public Triple toJenaTriple() {
        Triple result = SpinUtils.toJenaTriple(this);
        return result;
    }


    @HashId
    public String getHashId()  {
        Triple t = SpinUtils.toJenaTriple(this);
        return "" + t;
    }


    @Iri(LSQ.Strs.hasExec)
    public abstract Set<TpExec> getTpExecs();

//    public TpExec findTpExec(Resource expRun) {
//        return null;
//    }
//    default JoinVertexExec findTpExec(Resource expRun) {
//        Resource expRun = getBenchmarkRun();
//        Objects.requireNonNull(expRun);
//
//        Set<JoinVertexExec> cands = getBgpNodeExecs();
//        JoinVertexExec result = null;
//        for(JoinVertexExec cand : cands) {
//            if(Objects.equals(cand.getBgpNode(), bgp) && Objects.equals(cand.getQueryExec().getBenchmarkRun(), expRun)) {
//                result = cand;
//                break;
//            }
//        }
//
//        return result;
//    }
}
