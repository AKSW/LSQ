package org.aksw.simba.lsq.model;

import java.math.BigDecimal;
import java.util.Calendar;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
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

    @Iri(LSQ.Strs.endpoint)
    ExperimentConfig setDataRef(DataRefSparqlEndpoint dataRef);
    DataRefSparqlEndpoint getDataRef();

    @Iri(LSQ.Strs.requestDelay)
    BigDecimal getRequestDelay();
    ExperimentConfig setRequestDelay(BigDecimal requestDelay);


//    @Iri(LSQ.Strs.resultSetSizeThreshold)
//    Long getResultSetSizeThreshold();
//    ExperimentConfig setResultSetSizeThreshold(Long resultSetSizeThreshold);

    @Iri(LSQ.Strs.userAgent)
    String getUserAgent();
    ExperimentConfig setUserAgent(String userAgent);

    @Iri(LSQ.Strs.datasetSize)
    Long getDatasetSize();
    ExperimentConfig setDatasetSize(Long datasetSize);

    @Iri(LSQ.Strs.datasetLabel)
    String getDatasetLabel();
    ExperimentConfig setDatasetLabel(String datasetLabel);

    @Iri(LSQ.Strs.datasetIri)
    @IriType
    String getDatasetIri();
    ExperimentConfig setDatasetIri(String datasetIri);

    @Iri(LSQ.Strs.baseIri)
    @IriType
    String getBaseIri();
    ExperimentConfig setBaseIri(String baseIri);

    /*
     * Benchmark options
     */

    @Iri(LSQ.Strs.connectionTimeoutForRetrieval)
    BigDecimal getConnectionTimeoutForRetrieval();
    ExperimentConfig setConnectionTimeoutForRetrieval(BigDecimal duration);

    @Iri(LSQ.Strs.executionTimeoutForRetrieval)
    BigDecimal getExecutionTimeoutForRetrieval();
    ExperimentConfig setExecutionTimeoutForRetrieval(BigDecimal duration);

    @Iri(LSQ.Strs.connectionTimeoutForCounting)
    BigDecimal getConnectionTimeoutForCounting();
    ExperimentConfig setConnectionTimeoutForCounting(BigDecimal duration);

    @Iri(LSQ.Strs.executionTimeoutForCounting)
    BigDecimal getExecutionTimeoutForCounting();
    ExperimentConfig setExecutionTimeoutForCounting(BigDecimal duration);

    @Iri(LSQ.Strs.maxResultCountForRetrieval)
    Long getMaxResultCountForCounting();
    ExperimentConfig setMaxResultCountForCounting(Long maxItemCountForCounting);

    @Iri(LSQ.Strs.maxByteSizeForRetrieval)
    Long getMaxByteSizeForCounting();
    ExperimentConfig setMaxByteSizeForCounting(Long maxByteSizeForCounting);

    @Iri(LSQ.Strs.maxResultCountForSerialization)
    Long getMaxResultCountForSerialization();
    ExperimentConfig setMaxResultCountForSerialization(Long maxItemCountForSerialization);

    @Iri(LSQ.Strs.maxByteSizeForSerialization)
    Long getMaxByteSizeForSerialization();
    ExperimentConfig setMaxByteSizeForSerialization(Long maxByteSizeForSerialization);

    @Iri(LSQ.Strs.maxCount)
    Long getMaxCount();
    ExperimentConfig setMaxCount(Long maxItemCountForCounting);

    @Iri(LSQ.Strs.maxCountAffectsTp)
    Boolean getMaxCountAffectsTp();
    ExperimentConfig getMaxCountAffectsTp(Boolean offOrOn);

    @Iri(LSQ.Strs.benchmarkSecondaryQueries)
    Boolean benchmarkSecondaryQueries();
    ExperimentConfig benchmarkSecondaryQueries(Boolean offOrOn);

}
