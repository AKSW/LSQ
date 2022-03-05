package org.aksw.simba.lsq.spinx.model;

import java.math.BigDecimal;

import org.aksw.commons.util.string.StringUtils;
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
public interface TpExec
    extends ElementExec
{
    @Iri(LSQ.Terms.hasExec)
    @Inverse
    LsqTriplePattern getTp();
    TpExec setTp(LsqTriplePattern tp);


    @Iri(LSQ.Terms.hasTpExec)
    @Inverse
    TpInBgpExec getTpInBgpExec();
    TpExec setTpInBgpExec(TpInBgpExec tpInBgpExec);

    @Iri(LSQ.Terms.tpSelBGPRestricted)
    BigDecimal getBgpRestrictedTpSel();
    TpExec setBgpRestrictedTpSel(BigDecimal bgpRestrictedTpSel);

    /**
     * Ratio of resultSetSize(tp) / resultSetSize(dataset)
     *
     * @return
     */
    @Iri(LSQ.Terms.tpToGraphRatio)
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
