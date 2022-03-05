package org.aksw.simba.lsq.spinx.model;

import java.math.BigDecimal;

import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface TpInBgpExec
    extends Resource
{
//    ExperimentRun getBenchmarkRun();
//    TpInBgpExec setBenchmarkRun(ExperimentRun exp);


    // TODO The following attributes should be mapped as they refer to computations from which the selectivity was derived
    @HashId
    @Iri(LSQ.Terms.hasTpInBgpExec)
    @Inverse
    BgpExec getBgpExec();
    TpInBgpExec setBgpExec(Resource bgpExec);

    @HashId
    @Iri(LSQ.Terms.hasTpExec)
    TpExec getTpExec();
    TpInBgpExec setTpExec(TpExec tpExec);

    @Iri(LSQ.Terms.hasTp)
    @Inverse
    TpInBgp getTpInBgp();
    TpInBgpExec setTpInBgp(Resource tpInBgp);

    @Iri(LSQ.Terms.tpSelBGPRestricted)
    BigDecimal getSelectivity();
    TpInBgpExec setSelectivity(BigDecimal value);

    @Iri(LSQ.Terms.tpToBgpRatio)
    BigDecimal getTpToBgpRatio();
    TpInBgpExec setTpToBgpRatio(BigDecimal value);

    @Iri(LSQ.Terms.hasJoinVarExec)
    BgpNodeExec getJoinVarExec();
    TpInBgpExec setJoinVarExec(Resource joinVar);
}
