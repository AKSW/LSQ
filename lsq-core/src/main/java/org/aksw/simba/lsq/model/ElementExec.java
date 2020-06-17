package org.aksw.simba.lsq.model;

import java.math.BigDecimal;
import java.util.Calendar;

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
    @Iri(LSQ.Strs.benchmarkRun)
    ExperimentRun getBenchmarkRun();
    ElementExec setBenchmarkRun(Resource benchmarkRun);

    // We should use seconds (standard SI units)
    @Iri(LSQ.Strs.runTimeMs)
    BigDecimal getRuntimeInMs();
    ElementExec setRuntimeInMs(BigDecimal runtimeInMs);

    @Iri(LSQ.Strs.resultSize)
    Long getResultSetSize();
    ElementExec setResultSetSize(Long resultSetSize);

    @Iri(LSQ.Strs.atTime)
    Calendar getTimestamp();
    RemoteExecution setTimestamp(Calendar calendar);

    // Link to the query having been executed
}
