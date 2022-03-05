package org.aksw.simba.lsq.spinx.model;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface BgpInfo
    extends Resource
{
    @Iri(LSQ.Terms.hasBgp)
    Set<Bgp> getBgps();
}
