package org.aksw.simba.lsq.spinx.model;

import java.util.Objects;
import java.util.Set;

import org.aksw.commons.util.string.StringUtils;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.annotation.reprogen.StringId;
import org.aksw.jenax.reprogen.hashid.HashIdCxt;
import org.aksw.simba.lsq.model.ElementExec;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;


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
    @Iri(LSQ.Terms.hasExec)
    @HashId
    @Inverse
    Bgp getBgp();
    BgpExec setBgp(Bgp bgp);

    @Iri(LSQ.Terms.hasTpInBgpExec)
    Set<TpInBgpExec> getTpInBgpExecs();

    @Iri(LSQ.Terms.hasJoinVarExec)
    Set<BgpNodeExec> getBgpNodeExecs();

    // NOTE Calling the method getId is not recognized by the annotation processor
    // because there is a method Resource.setId which has incompatible types
    @StringId
    default String getStringId(HashIdCxt cxt) {
        Bgp bgp = getBgp();
        // TODO Replace the prefix with e.g. cxt.getClassLabel(SpinBgpExec.class)
//        String result = "bgpExec-" + cxt.getHashAsString(bgp) + "-" + getLocalExecution().getBenchmarkRun().getIdentifier();
        String prefix = StringUtils.toLowerCamelCase(BgpExec.class.getSimpleName()); // "bgpExec"

        String bgpHash = cxt.getHashAsString(bgp);

//      RDFDataMgr.write(System.out, this.getModel(), RDFFormat.NTRIPLES);
//        if (bgpHash == null) {
//            System.err.println("Null BGP hash encountered");
//        }

        String result = prefix + "-" + bgpHash + "-" + cxt.getStringId(getQueryExec().getLocalExecution().getBenchmarkRun());
        return result;
    }

// Link from this exec to the benchmark result of the BGP's extension query
//    @Iri(LSQ.Strs.hasExec)
//    @Inverse
//    LocalExecution getBgpQueryExec();
//    SpinBgpExec setBgpQueryExec(LocalExecution exec);

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
