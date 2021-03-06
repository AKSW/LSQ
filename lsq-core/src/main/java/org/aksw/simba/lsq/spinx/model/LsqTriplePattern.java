package org.aksw.simba.lsq.spinx.model;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.mapper.hashid.HashIdCxt;
import org.aksw.simba.lsq.util.SpinUtils;
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
        Triple result = SpinUtils.toJenaTriple(this);
        return result;
    }


    @HashId
    public HashCode getHashId(HashIdCxt cxt)  {
        Triple t = SpinUtils.toJenaTriple(this);
        HashCode result = cxt.getHashFunction().hashString(Objects.toString(t), StandardCharsets.UTF_8);
        return result;
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
