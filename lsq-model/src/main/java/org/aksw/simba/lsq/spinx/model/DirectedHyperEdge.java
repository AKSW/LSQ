package org.aksw.simba.lsq.spinx.model;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface DirectedHyperEdge
    extends Resource
{
    @Iri(LSQ.Terms.in)
    @HashId
    Set<BgpNode> getInNodes();

    @Iri(LSQ.Terms.out)
    @HashId
    Set<BgpNode> getOutNodes();
}
