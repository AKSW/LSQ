package org.aksw.simba.lsq.spinx.model;

import java.math.BigDecimal;

import org.aksw.jena_sparql_api.mapper.annotation.IdPrefix;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.mapper.annotation.StringId;
import org.aksw.jena_sparql_api.mapper.hashid.HashIdCxt;
import org.aksw.simba.lsq.model.ElementExec;
import org.aksw.simba.lsq.model.ExperimentRun;
import org.aksw.simba.lsq.model.LocalExecution;
import org.aksw.simba.lsq.vocab.LSQ;


@ResourceView
@IdPrefix("bgpnodeexec-")
public interface JoinVertexExec
    extends ElementExec
{
    @Iri(LSQ.Strs.hasExec)
    @Inverse
    SpinBgpNode getBgpNode();
    JoinVertexExec setBgpNode(SpinBgpNode bpgNode);


    @Iri(LSQ.Strs.tpSelJoinVarRestricted)
    BigDecimal getBgpRestrictedSelectivitiy();
    JoinVertexExec setBgpRestrictedSelectivitiy(BigDecimal selectivity);


    @Iri(LSQ.Strs.hasJoinVarExec)
    @Inverse
    SpinBgpExec getBgpExec();
    JoinVertexExec setBgpExec(SpinBgpExec exec);


    @Iri(LSQ.Strs.hasSubBgpExec)
    SpinBgpExec getSubBgpExec();
    JoinVertexExec setSubBgpExec(SpinBgpExec exec);


    @StringId
    default String getStringId(HashIdCxt cxt) {

//    	SpinBgpExec bgpPart = getBgpExec(); // .getBgp();
//        BgpNode bgpNode = getBgpNode();
        // TODO Replace the prefix with e.g. cxt.getClassLabel(SpinBgpExec.class)
//        String result = "bgpExec-" + cxt.getHashAsString(bgp) + "-" + getLocalExecution().getBenchmarkRun().getIdentifier();
        SpinBgpExec bgpExec = getBgpExec();
        LocalExecution le = bgpExec.getQueryExec().getLocalExecution();
        ExperimentRun bmr = le.getBenchmarkRun();
        String result = "bgpNodeExec-" + cxt.getHashAsString(this) + "-" + cxt.getString(bmr);
        return result;
    }

//
//    SpinBgpExec getBgpNodeExec();
//    JoinVertexExec setBgpNodeExec(SpinBgp exec);

}
