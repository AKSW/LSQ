package org.aksw.simba.lsq.model;

import java.util.Objects;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.mapper.annotation.StringId;
import org.aksw.jena_sparql_api.mapper.hashid.HashIdCxt;
import org.aksw.simba.lsq.spinx.model.BgpExec;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface LocalExecution
    extends Resource
{
    @HashId
    @Iri(LSQ.Strs.benchmarkRun)
    ExperimentRun getBenchmarkRun();
    ElementExec setBenchmarkRun(Resource benchmarkRun);

    @HashId
    @Iri(LSQ.Strs.hasLocalExec)
    @Inverse
    LsqQuery getLsqQuery();

    @Iri(LSQ.Strs.hasQueryExec)
    QueryExec getQueryExec();
    LocalExecution setQueryExec(QueryExec queryExec);


    // TODO Maybe return a list of (bgp, exec) pairs - where setValue updates the exec?
    default BgpExec findBgpExec(Resource bgp) {
        Resource expRun = getBenchmarkRun();
        Objects.requireNonNull(expRun, "benchmark run resource not set");

        Set<BgpExec> cands = getBgpExecs();
        BgpExec result = null;
        for(BgpExec cand : cands) {
            if(Objects.equals(cand.getBgp(), bgp) && Objects.equals(cand.getQueryExec().getLocalExecution().getBenchmarkRun(), expRun)) {
                result = cand;
                break;
            }
        }

        return result;
    }

    @Iri(LSQ.Strs.hasBgpExec)
    Set<BgpExec> getBgpExecs();


    @StringId
    default String getStringId(HashIdCxt cxt) {
        ExperimentRun bmr = getBenchmarkRun();
        String result = "localExec-" + cxt.getHashAsString(this) + "-" + cxt.getString(bmr);
        return result;
    }

//    default Map<Resource, SpinBgpExec> indexBgpExecs() {
//        // TODO benchmark run to exec
//        return null;
//    }

    //Set<S> getTpExecs();
//    @Iri(LSQ.Strs.hasJoinVarExec)
//    Set<JoinVertexExec> getBgpNodeExecs();


//    @Iri(LSQ.Strs.hasTpInBgpExec)
//    Set<TpInBgpExec> getTpInBgpExec();


    // TODO Maybe return a list of (bgp, exec) pairs - where setValue updates the exec?
//    default JoinVertexExec findBgpNodeExec(Resource bgp) {
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
