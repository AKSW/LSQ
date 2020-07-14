package org.aksw.simba.lsq.model;

import java.math.BigDecimal;
import java.util.Calendar;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.spinx.model.SpinBgpExec;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

@ResourceView
@HashId // Include the type into the hash value
public interface QueryExec extends Resource {
//    @Iri(LSQ.Strs.hasExec)
//    @Inverse
//    Resource getSpinQuery();
    @HashId
    @Iri(LSQ.Strs.hasQueryExec)
    @Inverse
    LocalExecution getLocalExecution();
    SpinBgpExec setLocalExecution(LocalExecution le);

//    @Iri(LSQ.Strs.benchmarkRun)
//    ExperimentRun getBenchmarkRun();
//    ElementExec setBenchmarkRun(Resource benchmarkRun);

    @Iri(LSQ.Strs.runTimeMs)
    BigDecimal getRuntimeInMs();
    QueryExec setRuntimeInMs(BigDecimal runtimeInMs);

    @Iri(LSQ.Strs.resultSize)
    Long getResultSetSize();
    QueryExec setResultSetSize(Long resultSetSize);

    @Iri(LSQ.Strs.atTime)
    Calendar getTimestamp();
    RemoteExecution setTimestamp(Calendar calendar);
}
