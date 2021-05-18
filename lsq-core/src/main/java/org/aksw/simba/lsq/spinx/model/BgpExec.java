package org.aksw.simba.lsq.spinx.model;

import java.util.Objects;
import java.util.Set;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.mapper.annotation.StringId;
import org.aksw.jena_sparql_api.mapper.hashid.HashIdCxt;
import org.aksw.simba.lsq.model.ElementExec;
import org.aksw.simba.lsq.model.LocalExecution;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;


/**
 * Execution result of a BGP
 *
 * @author raven
 *
 */
@ResourceView
//@IdPrefix("bgpexec-")
public interface BgpExec
    extends ElementExec
{
//    @Iri(LSQ.Strs.hasBgpExec)
//    @Inverse
//    LocalExecution getLocalExecution();
//    SpinBgpExec setLocalExecution(LocalExecution le);

    // Link from the BGP to this exec
    @Iri(LSQ.Strs.hasExec)
    @HashId
    @Inverse
    Bgp getBgp();
    BgpExec setBgp(Bgp bgp);


    // NOTE Calling the method getId is not recognized by the annotation processor
    // because there is a method Resource.setId which has incompatible types
    @StringId
    default String getStringId(HashIdCxt cxt) {
        Bgp bgp = getBgp();
        // TODO Replace the prefix with e.g. cxt.getClassLabel(SpinBgpExec.class)
//        String result = "bgpExec-" + cxt.getHashAsString(bgp) + "-" + getLocalExecution().getBenchmarkRun().getIdentifier();
        String prefix = StringUtils.toLowerCamelCase(getClass().getSimpleName()); // "bgpExec"

        String result = prefix + "-" + cxt.getHashAsString(bgp) + "-" + cxt.getStringId(getQueryExec().getLocalExecution().getBenchmarkRun());
        return result;
    }


    // Link from this exec to the benchmark result of the BGP's extension query
//    @Iri(LSQ.Strs.hasExec)
//    @Inverse
//    LocalExecution getBgpQueryExec();
//    SpinBgpExec setBgpQueryExec(LocalExecution exec);


    @Iri(LSQ.Strs.hasTpInBgpExec)
    Set<TpInBgpExec> getTpInBgpExecs();

    @Iri(LSQ.Strs.hasJoinVarExec)
    Set<BgpNodeExec> getBgpNodeExecs();


    default TpInBgpExec findTpInBgpExec(Resource tpInBgp) {
        Resource expRun = getQueryExec().getLocalExecution().getBenchmarkRun();
        Objects.requireNonNull(expRun);

        Set<TpInBgpExec> cands = getTpInBgpExecs();
        TpInBgpExec result = null;
        for(TpInBgpExec cand : cands) {
            if(Objects.equals(cand.getTpInBgp(), tpInBgp) && Objects.equals(cand.getBgpExec().getQueryExec().getLocalExecution().getBenchmarkRun(), expRun)) {
                result = cand;
                break;
            }
        }

        return result;
    }


    default BgpNodeExec findBgpNodeExec(Resource bpgNode) {
        Resource expRun = getQueryExec().getLocalExecution().getBenchmarkRun();
        Objects.requireNonNull(expRun);

        Set<BgpNodeExec> cands = getBgpNodeExecs();
        BgpNodeExec result = null;
        for(BgpNodeExec cand : cands) {
            if(Objects.equals(cand.getBgpNode(), bpgNode) && Objects.equals(cand.getBgpExec().getQueryExec().getLocalExecution().getBenchmarkRun(), expRun)) {
                result = cand;
                break;
            }
        }

        return result;
    }


//    default TpExec findTpExec(LsqTriplePattern tp) {
//        Resource expRun = getLocalExecution().getBenchmarkRun();
//        Objects.requireNonNull(expRun);
//
//        Set<TpExec> cands = getT();
//        JoinVertexExec result = null;
//        for(JoinVertexExec cand : cands) {
//            if(Objects.equals(cand.getBgpNode(), bpgNode) && Objects.equals(cand.getBgpExec().getQueryExec().getBenchmarkRun(), expRun)) {
//                result = cand;
//                break;
//            }
//        }
//
//        return result;
//    }

    // TODO Add reverse link to SpinBGP
}
