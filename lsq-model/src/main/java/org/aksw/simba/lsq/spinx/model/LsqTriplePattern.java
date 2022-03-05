package org.aksw.simba.lsq.spinx.model;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;

import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.reprogen.hashid.HashIdCxt;
import org.aksw.simba.lsq.model.util.SpinCoreUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.topbraid.spin.model.impl.TriplePatternImpl;

import com.google.common.hash.HashCode;

@ResourceView
public abstract class LsqTriplePattern
    extends TriplePatternImpl
    implements LsqElement
{
    public LsqTriplePattern(Node node, EnhGraph graph) {
        super(node, graph);
    }

    public Triple toJenaTriple() {
        Triple result = SpinCoreUtils.toJenaTriple(this);
        return result;
    }


    @HashId
    public HashCode getHashId(HashIdCxt cxt)  {
        Triple t = SpinCoreUtils.toJenaTriple(this);
        HashCode result = cxt.getHashFunction().hashString(Objects.toString(t), StandardCharsets.UTF_8);
        return result;
    }


    @Iri(LSQ.Terms.hasExec)
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
