package org.aksw.simba.lsq.spinx.model;

import java.math.BigDecimal;

import org.aksw.commons.util.strings.StringUtils;
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
public interface TpExec
    extends ElementExec
{
    @Iri(LSQ.Strs.hasExec)
    @Inverse
    LsqTriplePattern getTp();
    TpExec setTp(LsqTriplePattern tp);


    @Iri(LSQ.Strs.hasTpExec)
    @Inverse
    TpInBgpExec getTpInBgpExec();
    TpExec setTpInBgpExec(TpInBgpExec tpInBgpExec);

    @Iri(LSQ.Strs.tpSelBGPRestricted)
    BigDecimal getBgpRestrictedTpSel();
    TpExec setBgpRestrictedTpSel(BigDecimal bgpRestrictedTpSel);

    /**
     * Ratio of resultSetSize(tp) / resultSetSize(dataset)
     *
     * @return
     */
    @Iri(LSQ.Strs.tpToGraphRatio)
    BigDecimal getSelectivity();
    TpInBgpExec setSelectivity(BigDecimal value);

    @StringId
    default String getStringId(HashIdCxt cxt) {
        LocalExecution le = this.getQueryExec().getLocalExecution();
        ExperimentRun bmr = le.getBenchmarkRun();
        String prefix = StringUtils.toLowerCamelCase(getClass().getSimpleName()); // "tpExec"
        String result = prefix + "-" + cxt.getHashAsString(this) + "-" + cxt.getStringId(bmr);
        return result;
    }

}
