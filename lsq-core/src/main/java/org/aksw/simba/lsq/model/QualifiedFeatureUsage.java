package org.aksw.simba.lsq.model;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface QualifiedFeatureUsage
    extends Resource
{
    @Iri(LSQ.Strs.feature)
    String getFeature();
    QualifiedFeatureUsage setFeature(String host);

    @Iri(LSQ.Strs.count)
    Integer getCount();
    QualifiedFeatureUsage setCount(Integer count);
}
