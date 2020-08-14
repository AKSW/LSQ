package org.aksw.simba.lsq.spinx.model;

import java.math.BigDecimal;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
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
public interface BgpNodeExec
    extends ElementExec
{
    @Iri(LSQ.Strs.hasExec)
    @Inverse
    @HashId // The id is based on having executed on this node (the link to the queryExec is in the base class)
    BgpNode getBgpNode();
    BgpNodeExec setBgpNode(BgpNode bpgNode);


    @Iri(LSQ.Strs.tpSelJoinVarRestricted)
    BigDecimal getBgpRestrictedSelectivitiy();
    BgpNodeExec setBgpRestrictedSelectivitiy(BigDecimal selectivity);


    @Iri(LSQ.Strs.hasJoinVarExec)
    @Inverse
    BgpExec getBgpExec();
    BgpNodeExec setBgpExec(BgpExec exec);


    @Iri(LSQ.Strs.hasSubBgpExec)
    BgpExec getSubBgpExec();
    BgpNodeExec setSubBgpExec(BgpExec exec);


    @StringId
    default String getStringId(HashIdCxt cxt) {

//    	SpinBgpExec bgpPart = getBgpExec(); // .getBgp();
//        BgpNode bgpNode = getBgpNode();
        // TODO Replace the prefix with e.g. cxt.getClassLabel(SpinBgpExec.class)
//        String result = "bgpExec-" + cxt.getHashAsString(bgp) + "-" + getLocalExecution().getBenchmarkRun().getIdentifier();

        //SpinBgpExec bgpExec = getBgpExec();
        //LocalExecution le = bgpExec.getQueryExec().getLocalExecution();
        LocalExecution le = getQueryExec().getLocalExecution();
        ExperimentRun bmr = le.getBenchmarkRun();
        String result = "bgpNodeExec-" + cxt.getHashAsString(this) + "-" + cxt.getStringId(bmr);
        return result;
    }

//
//    SpinBgpExec getBgpNodeExec();
//    JoinVertexExec setBgpNodeExec(SpinBgp exec);

}
