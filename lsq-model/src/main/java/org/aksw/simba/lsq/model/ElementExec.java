package org.aksw.simba.lsq.model;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

/**
 * This effectively represents an observation of the metrics of a query having
 * been executed on some endpoint in some benchmark experiment
 *
 * @author raven
 *
 */
public interface ElementExec
    extends Resource
{
//    @Iri(LSQ.Strs.benchmarkRun)
//    ExperimentRun getBenchmarkRun();
//    ElementExec setBenchmarkRun(Resource benchmarkRun);

    @HashId
    @Iri(LSQ.Terms.hasElementExec)
    QueryExec getQueryExec();
    ElementExec setQueryExec(QueryExec queryExec);
    // We should use seconds (standard SI units)

    // Link to the element whole extension query was executed
    // Use element.getExtensionQuery to get the the query itself
//    LsqElement getElement();
}
