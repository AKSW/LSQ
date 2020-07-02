package org.aksw.simba.lsq.spinx.model;

import java.math.BigDecimal;

import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.model.ExperimentRun;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface TpInBgpExec
    extends Resource
{
    BigDecimal getSelectivity();
    TpInBgpExec setSelectivity(BigDecimal value);

    TpInBgp getTpInBgp();
    TpInBgpExec setTpInBgp(Resource tpInBgp);

    ExperimentRun getBenchmarkRun();
    TpInBgpExec setBenchmarkRun(ExperimentRun exp);


    // TODO The following attributes should be mapped as they refer to computations from which the selectivity was derived
    SpinBgpExec getBgpExec();
    TpInBgpExec setBgpExec(Resource bgpExec);

    TpExec getTpExec();
    TpInBgpExec setTpExec(TpExec tpExec);

    JoinVertexExec getJoinVarExec();
    TpInBgpExec setJoinVarExec(Resource joinVar);
}
