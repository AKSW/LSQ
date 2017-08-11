package org.aksw.simba.lsq.cli.main;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.apache.jena.rdf.model.Resource;

public class LsqConfig {
    protected Map<String, Function<InputStream, Stream<Resource>>> logFmtRegistry;

    //protected Map<String, Mapper> logFmtRegistry;

    protected File inQueryLogFile;
    protected String inQueryLogFormat;

    protected File outRdfFile;
    protected String outRdfFormat;
    protected Function<String, SparqlStmt> sparqlStmtParser;

    protected File outFile;


    // base Iri of output resources
    protected String outBaseIri;

    protected boolean isRdfizerQueryStructuralFeaturesEnabled;
    protected boolean isRdfizerQueryExecutionEnabled;
    protected boolean isRdfizerQueryLogRecordEnabled;
    //protected boolean isQueryExecutionRemote;
    //protected boolean isRdfizerRemoteExecutionEnabled;

    protected String datasetLabel;

    protected SparqlServiceReference datasetEndpointDescription;

    protected SparqlServiceReference benchmarkEndpointDescription;

    //protected Long datasetEndpointPagination;
    protected boolean isFetchDatasetSizeEnabled;
    //protected boolean fetchDatasetEndpointSize;

//    protected SparqlServiceReference remoteEndpoint;
//    protected Long remoteDatasetEndpointFetchSize;
//    protected boolean fetchRemoteDatasetEndpointSize;

    protected Long benchmarkQueryExecutionTimeoutInMs;

    protected String experimentIri;
    protected Long datasetSize;

    protected Long firstItemOffset;

    protected List<String> federationEndpoints;
    protected File federationConfigFile;

    protected boolean reuseLogIri;

    protected boolean emitProcessMetadata;

    /**
     * Regex for matching a query ID from a prior IRI;
     * only applicable when processing queries from RDF input
     */
    protected Pattern queryIdPattern;

    public boolean isFetchDatasetSizeEnabled() {
        return isFetchDatasetSizeEnabled;
    }



    public SparqlServiceReference getBenchmarkEndpointDescription() {
        return benchmarkEndpointDescription;
    }



    public void setBenchmarkEndpointDescription(SparqlServiceReference benchmarkEndpointDescription) {
        this.benchmarkEndpointDescription = benchmarkEndpointDescription;
    }



    public void setFetchDatasetSizeEnabled(boolean isFetchDatasetSizeEnabled) {
        this.isFetchDatasetSizeEnabled = isFetchDatasetSizeEnabled;
    }


    public Function<String, SparqlStmt> getSparqlStmtParser() {
        return sparqlStmtParser;
    }


    public void setSparqlStmtParser(Function<String, SparqlStmt> sparqlStmtParser) {
        this.sparqlStmtParser = sparqlStmtParser;
    }


    public Map<String, Function<InputStream, Stream<Resource>>> getLogFmtRegistry() {
        return logFmtRegistry;
    }



    public LsqConfig setLogFmtRegistry(Map<String, Function<InputStream, Stream<Resource>>> logFmtRegistry) {
        this.logFmtRegistry = logFmtRegistry;
        return this;
    }


//    public Map<String, Mapper> getLogFmtRegistry() {
//        return logFmtRegistry;
//    }
//
//
//    public void setLogFmtRegistry(Map<String, Mapper> logFmtRegistry) {
//        this.logFmtRegistry = logFmtRegistry;
//    }





    public File getOutFile() {
        return outFile;
    }

    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }


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

    public boolean isRdfizerQueryExecutionEnabled() {
        return isRdfizerQueryExecutionEnabled;
    }

    public void setRdfizerQueryExecutionEnabled(boolean isRdfizerQueryExecutionEnabled) {
        this.isRdfizerQueryExecutionEnabled = isRdfizerQueryExecutionEnabled;
    }

    public boolean isRdfizerQueryLogRecordEnabled() {
        return isRdfizerQueryLogRecordEnabled;
    }

    public void setRdfizerQueryLogRecordEnabled(boolean isRdfizerQueryLogRecordEnabled) {
        this.isRdfizerQueryLogRecordEnabled = isRdfizerQueryLogRecordEnabled;
    }




//    public boolean isRdfizerRemoteExecutionEnabled() {
//        return isRdfizerRemoteExecutionEnabled;
//    }
//
//    public void setRdfizerRemoteExecutionEnabled(boolean isRdfizerRemoteExecutionEnabled) {
//        this.isRdfizerRemoteExecutionEnabled = isRdfizerRemoteExecutionEnabled;
//    }


    public boolean isRdfizerQueryStructuralFeaturesEnabled() {
        return isRdfizerQueryStructuralFeaturesEnabled;
    }


    public void setRdfizerQueryStructuralFeaturesEnabled(boolean isRdfizerQueryStructuralFeaturesEnabled) {
        this.isRdfizerQueryStructuralFeaturesEnabled = isRdfizerQueryStructuralFeaturesEnabled;
    }


//    public boolean isQueryExecutionRemote() {
//        return isQueryExecutionRemote;
//    }
//
//
//    public void setQueryExecutionRemote(boolean isQueryExecutionRemote) {
//        this.isQueryExecutionRemote = isQueryExecutionRemote;
//    }


//    public Long getDatasetEndpointPagination() {
//        return datasetEndpointPagination;
//    }
//
//
//    public void setDatasetEndpointPagination(Long datasetEndpointPagination) {
//        this.datasetEndpointPagination = datasetEndpointPagination;
//    }


    public String getDatasetLabel() {
        return datasetLabel;
    }

    public void setDatasetLabel(String datasetLabel) {
        this.datasetLabel = datasetLabel;
    }

    public Long getBenchmarkQueryExecutionTimeoutInMs() {
        return benchmarkQueryExecutionTimeoutInMs;
    }

    public void setBenchmarkQueryExecutionTimeoutInMs(Long datasetQueryExecutionTimeoutInMs) {
        this.benchmarkQueryExecutionTimeoutInMs = datasetQueryExecutionTimeoutInMs;
    }

    public String getExperimentIri() {
        return experimentIri;
    }

    public void setExperimentIri(String experimentIri) {
        this.experimentIri = experimentIri;
    }

    public boolean isRdfizerQueryEnabled() {
        return isRdfizerQueryStructuralFeaturesEnabled;
    }


    public void setRdfizerQueryEnabled(boolean isRdfizerQueryEnabled) {
        this.isRdfizerQueryStructuralFeaturesEnabled = isRdfizerQueryEnabled;
    }

    public SparqlServiceReference getDatasetEndpointDescription() {
        return datasetEndpointDescription;
    }


    public void setDatasetEndpointDescription(SparqlServiceReference datasetEndpointDescription) {
        this.datasetEndpointDescription = datasetEndpointDescription;
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

    public boolean isReuseLogIri() {
        return reuseLogIri;
    }

    public void setReuseLogIri(boolean reuseLogIri) {
        this.reuseLogIri = reuseLogIri;
    }

    public boolean isEmitProcessMetadata() {
        return emitProcessMetadata;
    }

    public void setEmitProcessMetadata(boolean emitProcessMetadata) {
        this.emitProcessMetadata = emitProcessMetadata;
    }

    public Pattern getQueryIdPattern() {
        return queryIdPattern;
    }

    public void setQueryIdPattern(Pattern queryIdPattern) {
        this.queryIdPattern = queryIdPattern;
    }
}

