package org.aksw.simba.lsq.spinx.model;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;


@ResourceView
public interface SpinQueryEx
    extends Resource
{
    @Iri(LSQ.Strs.hasBGP)
    Set<SpinBgp> getBgps();

    @Iri(LSQ.Strs.bgpCountTotal)
    Integer getTotalBgpCount();
    SpinQueryEx setTotalBgpCount(Integer cnt);

    @Iri(LSQ.Strs.tpInBgpCountMin)
    Integer getMinBgpTriples();
    SpinQueryEx setMinBgpTriples(Integer cnt);

    @Iri(LSQ.Strs.tpInBgpCountMax)
    Integer getMaxBgpTriples();
    SpinQueryEx setMaxBgpTriples(Integer cnt);

    @Iri(LSQ.Strs.tpCountTotal)
    Integer getTriplePatternCount();
    SpinQueryEx setTriplePatternCount(Integer cnt);
}
