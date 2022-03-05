package org.aksw.simba.lsq.spinx.model;

import java.math.BigDecimal;

import org.aksw.commons.util.string.StringUtils;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.annotation.reprogen.StringId;
import org.aksw.jenax.reprogen.hashid.HashIdCxt;
import org.aksw.simba.lsq.model.ElementExec;
import org.aksw.simba.lsq.model.ExperimentRun;
import org.aksw.simba.lsq.model.LocalExecution;
import org.aksw.simba.lsq.vocab.LSQ;


@ResourceView
public interface BgpNodeExec
    extends ElementExec
{
    @Iri(LSQ.Terms.hasExec)
    @Inverse
    @HashId // The id is based on having executed on this node (the link to the queryExec is in the base class)
    BgpNode getBgpNode();
    BgpNodeExec setBgpNode(BgpNode bpgNode);


    @Iri(LSQ.Terms.tpSelJoinVarRestricted)
    BigDecimal getBgpRestrictedSelectivitiy();
    BgpNodeExec setBgpRestrictedSelectivitiy(BigDecimal selectivity);


    @Iri(LSQ.Terms.hasJoinVarExec)
    @Inverse
    BgpExec getBgpExec();
    BgpNodeExec setBgpExec(BgpExec exec);


    @Iri(LSQ.Terms.hasSubBgpExec)
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
        String prefix = StringUtils.toLowerCamelCase(getClass().getSimpleName()); // "bgpNodeExec-"
        String result = prefix + "-" + cxt.getHashAsString(this) + "-" + cxt.getStringId(bmr);
        return result;
    }

//
//    SpinBgpExec getBgpNodeExec();
//    JoinVertexExec setBgpNodeExec(SpinBgp exec);

}
