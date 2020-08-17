package org.aksw.simba.lsq.spinx.model;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;

public interface Labeled
{
    @Iri("rdfs:label")
    Labeled setLabel(String label);
    String getLabel();
}
