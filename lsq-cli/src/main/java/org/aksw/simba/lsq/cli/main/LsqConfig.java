package org.aksw.simba.lsq.cli.main;

import java.io.File;
import java.util.List;

public class LsqConfig {
    protected File inQueryLogFile;
    protected String inQueryLogFormat;

    protected File outRdfFile;
    protected String outRdfFormat;

    // base Iri of output resources
    protected String outBaseIri;

    protected boolean rdfizeQuery;
    protected boolean rdfizeLog;
    protected boolean rdfizeQueryExecution;

    protected String datasetLabel;
    protected String datasetEndpointIri;
    protected List<String> datasetDefaultGraphIris;
    protected boolean fetchDatasetSize;

    protected Long queryTimeoutInMs;

    protected String experimentIri;

    protected String endpointUrl;

    protected Long datasetSize;

    protected Long firstItemOffset;

    protected List<String> federationEndpoints;
    protected File federationConfigFile;

    public File getInQueryLogFile() {
        return inQueryLogFile;
    }

    public void setInQueryLogFile(File inQueryLogFile) {
        this.inQueryLogFile = inQueryLogFile;
    }

    public String getInQueryLogFormat() {
        return inQueryLogFormat;
    }

    public void setInQueryLogFormat(String inQueryLogFormat) {
        this.inQueryLogFormat = inQueryLogFormat;
    }

    public File getOutRdfFile() {
        return outRdfFile;
    }

    public void setOutRdfFile(File outRdfFile) {
        this.outRdfFile = outRdfFile;
    }

    public String getOutRdfFormat() {
        return outRdfFormat;
    }

    public void setOutRdfFormat(String outRdfFormat) {
        this.outRdfFormat = outRdfFormat;
    }

    public boolean isRdfizeQuery() {
        return rdfizeQuery;
    }

    public void setRdfizeQuery(boolean rdfizeQuery) {
        this.rdfizeQuery = rdfizeQuery;
    }

    public boolean isRdfizeLog() {
        return rdfizeLog;
    }

    public void setRdfizeLog(boolean rdfizeLog) {
        this.rdfizeLog = rdfizeLog;
    }

    public boolean isRdfizeQueryExecution() {
        return rdfizeQueryExecution;
    }

    public void setRdfizeQueryExecution(boolean rdfizeQueryExecution) {
        this.rdfizeQueryExecution = rdfizeQueryExecution;
    }

    public String getDatasetLabel() {
        return datasetLabel;
    }

    public void setDatasetLabel(String datasetLabel) {
        this.datasetLabel = datasetLabel;
    }

    public String getDatasetEndpointIri() {
        return datasetEndpointIri;
    }

    public void setDatasetEndpointIri(String datasetEndpointIri) {
        this.datasetEndpointIri = datasetEndpointIri;
    }

    public boolean isFetchDatasetSize() {
        return fetchDatasetSize;
    }

    public void setFetchDatasetSize(boolean fetchDatasetSize) {
        this.fetchDatasetSize = fetchDatasetSize;
    }

    public Long getQueryTimeoutInMs() {
        return queryTimeoutInMs;
    }

    public void setQueryTimeoutInMs(Long queryTimeoutInMs) {
        this.queryTimeoutInMs = queryTimeoutInMs;
    }

    public String getExperimentIri() {
        return experimentIri;
    }

    public void setExperimentIri(String experimentIri) {
        this.experimentIri = experimentIri;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }


    public List<String> getDatasetDefaultGraphIris() {
        return datasetDefaultGraphIris;
    }

    public void setDatasetDefaultGraphIris(List<String> datasetDefaultGraphIris) {
        this.datasetDefaultGraphIris = datasetDefaultGraphIris;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public List<String> getFederationEndpoints() {
        return federationEndpoints;
    }

    public void setFederationEndpoints(List<String> federationEndpoints) {
        this.federationEndpoints = federationEndpoints;
    }


    public Long getFirstItemOffset() {
        return firstItemOffset;
    }

    public void setFirstItemOffset(Long firstItemOffset) {
        this.firstItemOffset = firstItemOffset;
    }

    public File getFederationConfigFile() {
        return federationConfigFile;
    }

    public void setFederationConfigFile(File federationConfigFile) {
        this.federationConfigFile = federationConfigFile;
    }

    public String getOutBaseIri() {
        return outBaseIri;
    }

    public void setOutBaseIri(String outBaseIri) {
        this.outBaseIri = outBaseIri;
    }

    public Long getDatasetSize() {
        return datasetSize;
    }

    public void setDatasetSize(Long datasetSize) {
        this.datasetSize = datasetSize;
    }



}
