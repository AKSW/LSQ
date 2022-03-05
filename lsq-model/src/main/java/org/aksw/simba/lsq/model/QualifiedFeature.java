package org.aksw.simba.lsq.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface QualifiedFeature
    extends Resource
{
    @Iri(LSQ.Terms.feature)
    String getFeature();
    QualifiedFeature setFeature(String host);

    @Iri(LSQ.Terms.count)
    Integer getCount();
    QualifiedFeature setCount(Integer count);

    //
    default int incrementAndGet() {
        Integer count = getCount();
        int nextCount = count == null ? 1 : ++count;
        setCount(nextCount);
        return count;
    }
}
