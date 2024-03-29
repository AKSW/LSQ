package org.aksw.simba.lsq.spinx.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

/**
 * Base interface for all resources that denote SPARQL elements that can be related to a query
 * that corresponds to that element's set of solution bindings.
 *
 * The extension query is always a SPARQL SELECT query.
 *
 * @author raven
 *
 */
public interface LsqElement
    extends Resource, Labeled
{
    @Iri(LSQ.Terms.extensionQuery)
    LsqQuery getExtensionQuery();
    LsqElement setExtensionQuery(LsqQuery r);
}
