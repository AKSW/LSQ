package org.aksw.simba.lsq.model;

import java.math.BigDecimal;

import org.aksw.commons.util.string.StringUtils;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.annotation.reprogen.StringId;
import org.aksw.jenax.reprogen.hashid.HashIdCxt;
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
    @Iri(LSQ.Terms.hasQueryExec)
    @Inverse
    LocalExecution getLocalExecution();
    BgpExec setLocalExecution(LocalExecution le);

//    @Iri(LSQ.Strs.benchmarkRun)
//    ExperimentRun getBenchmarkRun();
//    ElementExec setBenchmarkRun(Resource benchmarkRun);

    @Iri(LSQ.Terms.retrievalDuration)
    BigDecimal getRetrievalDuration();
    QueryExec setRetrievalDuration(BigDecimal runtimeInMs);

    @Iri(LSQ.Terms.countingDuration)
    BigDecimal getCountDuration();
    QueryExec setCountDuration(BigDecimal runtimeInMs);

    /**
     * The total amount of time spent benchmarking which may have involved trying out multiple
     * strategies
     *
     * @return
     */
    @Iri(LSQ.Terms.evalDuration)
    BigDecimal getEvalDuration();
    QueryExec setEvalDuration(BigDecimal duration);


    @Iri(LSQ.Terms.resultCount)
    Long getResultSetSize();
    QueryExec setResultSetSize(Long resultSetSize);

    /** The value returned by SELECT COUNT() queries */
    @Iri(LSQ.Terms.countValue)
    Long getCountValue();
    QueryExec setCountValue(Long countValue);

    @Iri(LSQ.Terms.exceededMaxByteSizeForCounting)
    Boolean getExceededMaxByteSizeForCounting();
    QueryExec setExceededMaxByteSizeForCounting(Boolean offOrOn);

    @Iri(LSQ.Terms.exceededMaxResultCountForCounting)
    Boolean getExceededMaxResultCountForCounting();
    QueryExec setExceededMaxResultCountForCounting(Boolean offOrOn);

    @Iri(LSQ.Terms.exceededMaxByteSizeForSerialization)
    Boolean getExceededMaxByteSizeForSerialization();
    QueryExec setExceededMaxByteSizeForSerialization(Boolean offOrOn);

    @Iri(LSQ.Terms.exceededMaxResultCountForSerialization)
    Boolean getExceededMaxResultCountForSerialization();
    QueryExec setExceededMaxResultCountForSerialization(Boolean offOrOn);

    @Iri(LSQ.Terms.serializedResult)
    String getSerializedResult();
    QueryExec setSerializedResult(String serializedResult);

    @Iri(LSQ.Terms.retrievalError)
    String getRetrievalError();
    QueryExec setRetrievalError(String msg);

    @Iri(LSQ.Terms.countingError)
    String getCountingError();
    QueryExec setCountingError(String msg);

    @Iri(LSQ.Terms.atTime)
    XSDDateTime getTimestamp();
    QueryExec setTimestamp(XSDDateTime calendar);

    @StringId
    default String getStringId(HashIdCxt cxt) {
        LocalExecution le = getLocalExecution();
        ExperimentRun bmr = le.getBenchmarkRun();
        String prefix = StringUtils.toLowerCamelCase(QueryExec.class.getSimpleName()); // "queryExec"

        String result = prefix + "-" + cxt.getHashAsString(this) + "-" + cxt.getStringId(bmr);
        return result;
    }

}
