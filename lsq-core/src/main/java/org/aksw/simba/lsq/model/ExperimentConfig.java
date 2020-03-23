package org.aksw.simba.lsq.model;

import java.math.BigDecimal;
import java.util.Calendar;

import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface ExperimentConfig
    extends Resource
{
    @Iri("dct:identifier")
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

    @Iri(LSQ.Strs.connectionTimeout)
    BigDecimal getConnectionTimeout();
    ExperimentConfig setConnectionTimeout(BigDecimal requestDelay);

    @Iri(LSQ.Strs.queryTimeout)
    BigDecimal getQueryTimeout();
    ExperimentConfig setQueryTimeout(BigDecimal requestDelay);

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
}
