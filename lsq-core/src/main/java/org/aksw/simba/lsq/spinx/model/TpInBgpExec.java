package org.aksw.simba.lsq.spinx.model;

import java.math.BigDecimal;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
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
    @Iri(LSQ.Strs.hasTpInBgpExec)
    @Inverse
    SpinBgpExec getBgpExec();
    TpInBgpExec setBgpExec(Resource bgpExec);

    @HashId
    @Iri(LSQ.Strs.hasTpExec)
    TpExec getTpExec();
    TpInBgpExec setTpExec(TpExec tpExec);

    @Iri(LSQ.Strs.hasTp)
    @Inverse
    TpInBgp getTpInBgp();
    TpInBgpExec setTpInBgp(Resource tpInBgp);

    @Iri(LSQ.Strs.tpSelBGPRestricted)
    BigDecimal getSelectivity();
    TpInBgpExec setSelectivity(BigDecimal value);

    @Iri(LSQ.Strs.tpToBgpRatio)
    BigDecimal getTpToBgpRatio();
    TpInBgpExec setTpToBgpRatio(BigDecimal value);

    @Iri(LSQ.Strs.hasJoinVarExec)
    JoinVertexExec getJoinVarExec();
    TpInBgpExec setJoinVarExec(Resource joinVar);
}
