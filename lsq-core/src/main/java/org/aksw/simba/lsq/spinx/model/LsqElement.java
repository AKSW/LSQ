package org.aksw.simba.lsq.spinx.model;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
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
    extends Resource
{
    @Iri(LSQ.Strs.extensionQuery)
    LsqQuery getExtensionQuery();
    LsqElement setExtensionQuery(LsqQuery r);
}
