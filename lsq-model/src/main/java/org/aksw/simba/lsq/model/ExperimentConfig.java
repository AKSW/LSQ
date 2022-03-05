package org.aksw.simba.lsq.model;

import java.math.BigDecimal;
import java.util.Calendar;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefSparqlEndpoint;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

// FIXME Add config option to benchmark only master queries and not secondary ones
@ResourceView
public interface ExperimentConfig
    extends Resource
{
    @Iri("dct:identifier")
    @HashId
    String getIdentifier();
    ExperimentConfig setIdentifier(String id);

//    @Iri(LSQ.Strs.endpoint)
//    @IriType
//    String getEndpoint();
//    ExperimentConfig setEndpoint(String url);

    @Iri("http://purl.org/dc/terms/created")
    Calendar getCreationDate();
    ExperimentConfig setCreationDate(Calendar cal);

    @Iri(LSQ.Terms.endpoint)
    ExperimentConfig setDataRef(DataRefSparqlEndpoint dataRef);
    DataRefSparqlEndpoint getDataRef();

    @Iri(LSQ.Terms.requestDelay)
    BigDecimal getRequestDelay();
    ExperimentConfig setRequestDelay(BigDecimal requestDelay);


//    @Iri(LSQ.Strs.resultSetSizeThreshold)
//    Long getResultSetSizeThreshold();
//    ExperimentConfig setResultSetSizeThreshold(Long resultSetSizeThreshold);

    @Iri(LSQ.Terms.userAgent)
    String getUserAgent();
    ExperimentConfig setUserAgent(String userAgent);

    @Iri(LSQ.Terms.datasetSize)
    Long getDatasetSize();
    ExperimentConfig setDatasetSize(Long datasetSize);

    @Iri(LSQ.Terms.datasetLabel)
    String getDatasetLabel();
    ExperimentConfig setDatasetLabel(String datasetLabel);

    @Iri(LSQ.Terms.datasetIri)
    @IriType
    String getDatasetIri();
    ExperimentConfig setDatasetIri(String datasetIri);

    @Iri(LSQ.Terms.baseIri)
    @IriType
    String getBaseIri();
    ExperimentConfig setBaseIri(String baseIri);

    /*
     * Benchmark options
     */

    @Iri(LSQ.Terms.connectionTimeoutForRetrieval)
    BigDecimal getConnectionTimeoutForRetrieval();
    ExperimentConfig setConnectionTimeoutForRetrieval(BigDecimal duration);

    @Iri(LSQ.Terms.executionTimeoutForRetrieval)
    BigDecimal getExecutionTimeoutForRetrieval();
    ExperimentConfig setExecutionTimeoutForRetrieval(BigDecimal duration);

    @Iri(LSQ.Terms.connectionTimeoutForCounting)
    BigDecimal getConnectionTimeoutForCounting();
    ExperimentConfig setConnectionTimeoutForCounting(BigDecimal duration);

    @Iri(LSQ.Terms.executionTimeoutForCounting)
    BigDecimal getExecutionTimeoutForCounting();
    ExperimentConfig setExecutionTimeoutForCounting(BigDecimal duration);

    @Iri(LSQ.Terms.maxResultCountForRetrieval)
    Long getMaxResultCountForCounting();
    ExperimentConfig setMaxResultCountForCounting(Long maxItemCountForCounting);

    @Iri(LSQ.Terms.maxByteSizeForRetrieval)
    Long getMaxByteSizeForCounting();
    ExperimentConfig setMaxByteSizeForCounting(Long maxByteSizeForCounting);

    @Iri(LSQ.Terms.maxResultCountForSerialization)
    Long getMaxResultCountForSerialization();
    ExperimentConfig setMaxResultCountForSerialization(Long maxItemCountForSerialization);

    @Iri(LSQ.Terms.maxByteSizeForSerialization)
    Long getMaxByteSizeForSerialization();
    ExperimentConfig setMaxByteSizeForSerialization(Long maxByteSizeForSerialization);

    @Iri(LSQ.Terms.maxCount)
    Long getMaxCount();
    ExperimentConfig setMaxCount(Long maxItemCountForCounting);

    @Iri(LSQ.Terms.maxCountAffectsTp)
    Boolean getMaxCountAffectsTp();
    ExperimentConfig setMaxCountAffectsTp(Boolean offOrOn);

    @Iri(LSQ.Terms.benchmarkSecondaryQueries)
    Boolean benchmarkSecondaryQueries();
    ExperimentConfig benchmarkSecondaryQueries(Boolean offOrOn);

}
