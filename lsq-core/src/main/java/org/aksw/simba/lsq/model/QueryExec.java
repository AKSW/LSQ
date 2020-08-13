package org.aksw.simba.lsq.model;

import java.math.BigDecimal;

import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Inverse;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.jena_sparql_api.mapper.annotation.StringId;
import org.aksw.jena_sparql_api.mapper.hashid.HashIdCxt;
import org.aksw.simba.lsq.spinx.model.BgpExec;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.datatypes.xsd.XSDDateTime;
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
    BgpExec setLocalExecution(LocalExecution le);

//    @Iri(LSQ.Strs.benchmarkRun)
//    ExperimentRun getBenchmarkRun();
//    ElementExec setBenchmarkRun(Resource benchmarkRun);

    @Iri(LSQ.Strs.retrievalDuration)
    BigDecimal getRetrievalDuration();
    QueryExec setRetrievalDuration(BigDecimal runtimeInMs);

    @Iri(LSQ.Strs.countingDuration)
    BigDecimal getCountDuration();
    QueryExec setCountDuration(BigDecimal runtimeInMs);

    /**
     * The total amount of time spent benchmarking which may have involved trying out multiple
     * strategies
     *
     * @return
     */
    @Iri(LSQ.Strs.evalDuration)
    BigDecimal getEvalDuration();
    QueryExec setEvalDuration(BigDecimal duration);


    @Iri(LSQ.Strs.resultCount)
    Long getResultSetSize();
    QueryExec setResultSetSize(Long resultSetSize);

    @Iri(LSQ.Strs.exceededMaxByteSizeForCounting)
    Boolean getExceededMaxByteSizeForCounting();
    QueryExec setExceededMaxByteSizeForCounting(Boolean offOrOn);

    @Iri(LSQ.Strs.exceededMaxResultCountForCounting)
    Boolean getExceededMaxItemCountForCounting();
    QueryExec setExceededMaxItemCountForCounting(Boolean offOrOn);

    @Iri(LSQ.Strs.exceededMaxByteSizeForSerialization)
    Boolean getExceededMaxByteSizeForSerialization();
    QueryExec setExceededMaxByteSizeForSerialization(Boolean offOrOn);

    @Iri(LSQ.Strs.exceededMaxResultCountForSerialization)
    Boolean getExceededMaxItemCountForSerialization();
    QueryExec setExceededMaxItemCountForSerialization(Boolean offOrOn);

    @Iri(LSQ.Strs.serializedResult)
    String getSerializedResult();
    QueryExec setSerializedResult(String serializedResult);

    @Iri(LSQ.Strs.retrievalError)
    String getRetrievalError();
    QueryExec setRetrievalError(String msg);

    @Iri(LSQ.Strs.countingError)
    String getCountingError();
    QueryExec setCountingError(String msg);

    @Iri(LSQ.Strs.atTime)
    XSDDateTime getTimestamp();
    QueryExec setTimestamp(XSDDateTime calendar);

    @StringId
    default String getStringId(HashIdCxt cxt) {
        LocalExecution le = getLocalExecution();
        ExperimentRun bmr = le.getBenchmarkRun();
        String result = "queryExec-" + cxt.getHashAsString(this) + "-" + cxt.getString(bmr);
        return result;
    }

}
