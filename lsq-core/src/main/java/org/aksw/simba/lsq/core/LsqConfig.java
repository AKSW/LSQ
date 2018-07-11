package org.aksw.simba.lsq.core;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;

public class LsqConfig {
    protected Map<String, Function<InputStream, Stream<Resource>>> logFmtRegistry;

    // Use LsqUtils.applyDefaults for default values
    public LsqConfig() {
    	//this.logFmtRegistry = LsqUtils.createDefaultLogFmtRegistry();
    	this.federationEndpoints = new ArrayList<>();
    }
    
    //protected Map<String, Mapper> logFmtRegistry;

    protected String inQueryLogFile;
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
    
    // If no benchmark connection is set, the endpoint description will be used to create one
    // Conversely, if the connection is set, it will be used regardless of the benchmarkEndpointDescripton
    protected RDFConnection benchmarkConnection;
    
    protected RDFConnection dataConnection;
    
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

    protected Long seenQueryCacheSize;
    
    
    protected boolean deterministicPseudoTimestamps;
    
    
    public Long getSeenQueryCacheSize() {
		return seenQueryCacheSize;
	}

	public void setSeenQueryCacheSize(Long seenQueryCacheSize) {
		this.seenQueryCacheSize = seenQueryCacheSize;
	}

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



    public LsqConfig setBenchmarkEndpointDescription(SparqlServiceReference benchmarkEndpointDescription) {
        this.benchmarkEndpointDescription = benchmarkEndpointDescription;
        return this;
    }



    public LsqConfig setFetchDatasetSizeEnabled(boolean isFetchDatasetSizeEnabled) {
        this.isFetchDatasetSizeEnabled = isFetchDatasetSizeEnabled;
        return this;
    }


    public Function<String, SparqlStmt> getSparqlStmtParser() {
        return sparqlStmtParser;
    }


    public LsqConfig setSparqlStmtParser(Function<String, SparqlStmt> sparqlStmtParser) {
        this.sparqlStmtParser = sparqlStmtParser;
        return this;
    }
    
    // Use sequence ids for local executions
    public boolean isDeterministicPseudoTimestamps() {
		return deterministicPseudoTimestamps;
	}

	public LsqConfig setDeterministicPseudoTimestamps(boolean deterministicPseudoTimestamps) {
		this.deterministicPseudoTimestamps = deterministicPseudoTimestamps;
		return this;
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
//    public LsqConfig setLogFmtRegistry(Map<String, Mapper> logFmtRegistry) {
//        this.logFmtRegistry = logFmtRegistry;
//    }


    public RDFConnection getBenchmarkConnection() {
		return benchmarkConnection;
	}

	public LsqConfig setBenchmarkConnection(RDFConnection benchmarkConnection) {
		this.benchmarkConnection = benchmarkConnection;
		return this;
	}

    public RDFConnection getDataConnection() {
		return dataConnection;
	}

	public LsqConfig setDataConnection(RDFConnection dataConnection) {
		this.dataConnection = dataConnection;
		return this;
	}

	public File getOutFile() {
        return outFile;
    }


	public LsqConfig setOutFile(File outFile) {
        this.outFile = outFile;
        return this;
    }


    public String getInQueryLogFile() {
        return inQueryLogFile;
    }

    public LsqConfig setInQueryLogFile(String inQueryLogFile) {
        this.inQueryLogFile = inQueryLogFile;
        return this;
    }

    public String getInQueryLogFormat() {
        return inQueryLogFormat;
    }

    public LsqConfig setInQueryLogFormat(String inQueryLogFormat) {
        this.inQueryLogFormat = inQueryLogFormat;
        return this;
    }

    public File getOutRdfFile() {
        return outRdfFile;
    }

    public LsqConfig setOutRdfFile(File outRdfFile) {
        this.outRdfFile = outRdfFile;
        return this;
    }

    public String getOutRdfFormat() {
        return outRdfFormat;
    }

    public LsqConfig setOutRdfFormat(String outRdfFormat) {
        this.outRdfFormat = outRdfFormat;
        return this;
    }

    public boolean isRdfizerQueryExecutionEnabled() {
        return isRdfizerQueryExecutionEnabled;
    }

    public LsqConfig setRdfizerQueryExecutionEnabled(boolean isRdfizerQueryExecutionEnabled) {
        this.isRdfizerQueryExecutionEnabled = isRdfizerQueryExecutionEnabled;
        return this;
    }

    public boolean isRdfizerQueryLogRecordEnabled() {
        return isRdfizerQueryLogRecordEnabled;
    }

    public LsqConfig setRdfizerQueryLogRecordEnabled(boolean isRdfizerQueryLogRecordEnabled) {
        this.isRdfizerQueryLogRecordEnabled = isRdfizerQueryLogRecordEnabled;
        return this;
    }




//    public boolean isRdfizerRemoteExecutionEnabled() {
//        return isRdfizerRemoteExecutionEnabled;
//    }
//
//    public LsqConfig setRdfizerRemoteExecutionEnabled(boolean isRdfizerRemoteExecutionEnabled) {
//        this.isRdfizerRemoteExecutionEnabled = isRdfizerRemoteExecutionEnabled;
//    }


    public boolean isRdfizerQueryStructuralFeaturesEnabled() {
        return isRdfizerQueryStructuralFeaturesEnabled;
    }


    public LsqConfig setRdfizerQueryStructuralFeaturesEnabled(boolean isRdfizerQueryStructuralFeaturesEnabled) {
        this.isRdfizerQueryStructuralFeaturesEnabled = isRdfizerQueryStructuralFeaturesEnabled;
        return this;
    }


//    public boolean isQueryExecutionRemote() {
//        return isQueryExecutionRemote;
//    }
//
//
//    public LsqConfig setQueryExecutionRemote(boolean isQueryExecutionRemote) {
//        this.isQueryExecutionRemote = isQueryExecutionRemote;
//    }


//    public Long getDatasetEndpointPagination() {
//        return datasetEndpointPagination;
//    }
//
//
//    public LsqConfig setDatasetEndpointPagination(Long datasetEndpointPagination) {
//        this.datasetEndpointPagination = datasetEndpointPagination;
//    }


    public String getDatasetLabel() {
        return datasetLabel;
    }

    public LsqConfig setDatasetLabel(String datasetLabel) {
        this.datasetLabel = datasetLabel;
        return this;
    }

    public Long getBenchmarkQueryExecutionTimeoutInMs() {
        return benchmarkQueryExecutionTimeoutInMs;
    }

    public LsqConfig setBenchmarkQueryExecutionTimeoutInMs(Long datasetQueryExecutionTimeoutInMs) {
        this.benchmarkQueryExecutionTimeoutInMs = datasetQueryExecutionTimeoutInMs;
        return this;
    }

    public String getExperimentIri() {
        return experimentIri;
    }

    public LsqConfig setExperimentIri(String experimentIri) {
        this.experimentIri = experimentIri;
        return this;
    }

    public boolean isRdfizerQueryEnabled() {
        return isRdfizerQueryStructuralFeaturesEnabled;
    }


    public LsqConfig setRdfizerQueryEnabled(boolean isRdfizerQueryEnabled) {
        this.isRdfizerQueryStructuralFeaturesEnabled = isRdfizerQueryEnabled;
        return this;
    }

    public SparqlServiceReference getDatasetEndpointDescription() {
        return datasetEndpointDescription;
    }


    public LsqConfig setDatasetEndpointDescription(SparqlServiceReference datasetEndpointDescription) {
        this.datasetEndpointDescription = datasetEndpointDescription;
        return this;
    }

    public List<String> getFederationEndpoints() {
        return federationEndpoints;
    }

    public LsqConfig setFederationEndpoints(List<String> federationEndpoints) {
        this.federationEndpoints = federationEndpoints;
        return this;
    }

    public Long getFirstItemOffset() {
        return firstItemOffset;
    }

    public LsqConfig setFirstItemOffset(Long firstItemOffset) {
        this.firstItemOffset = firstItemOffset;
        return this;
    }

    public File getFederationConfigFile() {
        return federationConfigFile;
    }

    public LsqConfig setFederationConfigFile(File federationConfigFile) {
        this.federationConfigFile = federationConfigFile;
        return this;
    }

    public String getOutBaseIri() {
        return outBaseIri;
    }

    public LsqConfig setOutBaseIri(String outBaseIri) {
        this.outBaseIri = outBaseIri;
        return this;
    }

    public Long getDatasetSize() {
        return datasetSize;
    }

    public LsqConfig setDatasetSize(Long datasetSize) {
        this.datasetSize = datasetSize;
        return this;
    }

    public boolean isReuseLogIri() {
        return reuseLogIri;
    }

    public LsqConfig setReuseLogIri(boolean reuseLogIri) {
        this.reuseLogIri = reuseLogIri;
        return this;
    }

    public boolean isEmitProcessMetadata() {
        return emitProcessMetadata;
    }

    public LsqConfig setEmitProcessMetadata(boolean emitProcessMetadata) {
        this.emitProcessMetadata = emitProcessMetadata;
        return this;
    }

    public Pattern getQueryIdPattern() {
        return queryIdPattern;
    }

    public LsqConfig setQueryIdPattern(Pattern queryIdPattern) {
        this.queryIdPattern = queryIdPattern;
        return this;
    }
}

