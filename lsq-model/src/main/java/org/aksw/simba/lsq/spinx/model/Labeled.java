package org.aksw.simba.lsq.spinx.model;

import org.aksw.jenax.annotation.reprogen.Iri;

public interface Labeled
{
    @Iri("rdfs:label")
    Labeled setLabel(String label);
    String getLabel();
}
