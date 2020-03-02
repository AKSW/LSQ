package org.aksw.simba.lsq.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.apache.jena.rdfconnection.RDFConnection;

/**
 * Bean implementation of the LSQ config.
 * The code base should use the interface
 * 
 * @author Claus Stadler, Jan 28, 2019
 *
 */
public class LsqConfigImpl {
    protected Map<String, ResourceParser> logFmtRegistry;

    // Use LsqUtils.applyDefaults for default values
    public LsqConfigImpl() {
    	//this.logFmtRegistry = LsqUtils.createDefaultLogFmtRegistry();
    	this.federationEndpoints = new ArrayList<>();
    }
    
    //protected Map<String, Mapper> logFmtRegistry;

    protected List<String> inQueryLogFiles;
    protected String inQueryLogFormat;

    protected File outRdfFile;
    protected String outRdfFormat;
//    protected Function<String, SparqlStmt> sparqlStmtParser;

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
    
    
    protected String httpUserAgent;
    protected Long delayInMs;
    
    
    protected Iterable<String> prefixSources;

    
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



    public LsqConfigImpl setBenchmarkEndpointDescription(SparqlServiceReference benchmarkEndpointDescription) {
        this.benchmarkEndpointDescription = benchmarkEndpointDescription;
        return this;
    }



    public LsqConfigImpl setFetchDatasetSizeEnabled(boolean isFetchDatasetSizeEnabled) {
        this.isFetchDatasetSizeEnabled = isFetchDatasetSizeEnabled;
        return this;
    }


//    public Function<String, SparqlStmt> getSparqlStmtParser() {
//        return sparqlStmtParser;
//    }
//
//
//    public LsqConfigImpl setSparqlStmtParser(Function<String, SparqlStmt> sparqlStmtParser) {
//        this.sparqlStmtParser = sparqlStmtParser;
//        return this;
//    }
    
    // Use sequence ids for local executions
    public boolean isDeterministicPseudoTimestamps() {
		return deterministicPseudoTimestamps;
	}

	public LsqConfigImpl setDeterministicPseudoTimestamps(boolean deterministicPseudoTimestamps) {
		this.deterministicPseudoTimestamps = deterministicPseudoTimestamps;
		return this;
	}

	public Map<String, ResourceParser> getLogFmtRegistry() {
        return logFmtRegistry;
    }



    public LsqConfigImpl setLogFmtRegistry(Map<String, ResourceParser> logFmtRegistry) {
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

	public LsqConfigImpl setBenchmarkConnection(RDFConnection benchmarkConnection) {
		this.benchmarkConnection = benchmarkConnection;
		return this;
	}

    public RDFConnection getDataConnection() {
		return dataConnection;
	}

	public LsqConfigImpl setDataConnection(RDFConnection dataConnection) {
		this.dataConnection = dataConnection;
		return this;
	}

	public File getOutFile() {
        return outFile;
    }


	public LsqConfigImpl setOutFile(File outFile) {
        this.outFile = outFile;
        return this;
    }


    public List<String> getInQueryLogFiles() {
        return inQueryLogFiles;
    }

    public LsqConfigImpl setInQueryLogFiles(List<String> inQueryLogFiles) {
        this.inQueryLogFiles = inQueryLogFiles;
        return this;
    }

    public String getInQueryLogFormat() {
        return inQueryLogFormat;
    }

    public LsqConfigImpl setInQueryLogFormat(String inQueryLogFormat) {
        this.inQueryLogFormat = inQueryLogFormat;
        return this;
    }

    public File getOutRdfFile() {
        return outRdfFile;
    }

    public LsqConfigImpl setOutRdfFile(File outRdfFile) {
        this.outRdfFile = outRdfFile;
        return this;
    }

    public String getOutRdfFormat() {
        return outRdfFormat;
    }

    public LsqConfigImpl setOutRdfFormat(String outRdfFormat) {
        this.outRdfFormat = outRdfFormat;
        return this;
    }

    public boolean isRdfizerQueryExecutionEnabled() {
        return isRdfizerQueryExecutionEnabled;
    }

    public LsqConfigImpl setRdfizerQueryExecutionEnabled(boolean isRdfizerQueryExecutionEnabled) {
        this.isRdfizerQueryExecutionEnabled = isRdfizerQueryExecutionEnabled;
        return this;
    }

    public boolean isRdfizerQueryLogRecordEnabled() {
        return isRdfizerQueryLogRecordEnabled;
    }

    public LsqConfigImpl setRdfizerQueryLogRecordEnabled(boolean isRdfizerQueryLogRecordEnabled) {
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


    public LsqConfigImpl setRdfizerQueryStructuralFeaturesEnabled(boolean isRdfizerQueryStructuralFeaturesEnabled) {
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

    public LsqConfigImpl setDatasetLabel(String datasetLabel) {
        this.datasetLabel = datasetLabel;
        return this;
    }

    public Long getBenchmarkQueryExecutionTimeoutInMs() {
        return benchmarkQueryExecutionTimeoutInMs;
    }

    public LsqConfigImpl setBenchmarkQueryExecutionTimeoutInMs(Long datasetQueryExecutionTimeoutInMs) {
        this.benchmarkQueryExecutionTimeoutInMs = datasetQueryExecutionTimeoutInMs;
        return this;
    }

    public String getExperimentIri() {
        return experimentIri;
    }

    public LsqConfigImpl setExperimentIri(String experimentIri) {
        this.experimentIri = experimentIri;
        return this;
    }

    public boolean isRdfizerQueryEnabled() {
        return isRdfizerQueryStructuralFeaturesEnabled;
    }


    public LsqConfigImpl setRdfizerQueryEnabled(boolean isRdfizerQueryEnabled) {
        this.isRdfizerQueryStructuralFeaturesEnabled = isRdfizerQueryEnabled;
        return this;
    }

    public SparqlServiceReference getDatasetEndpointDescription() {
        return datasetEndpointDescription;
    }


    public LsqConfigImpl setDatasetEndpointDescription(SparqlServiceReference datasetEndpointDescription) {
        this.datasetEndpointDescription = datasetEndpointDescription;
        return this;
    }

    public List<String> getFederationEndpoints() {
        return federationEndpoints;
    }

    public LsqConfigImpl setFederationEndpoints(List<String> federationEndpoints) {
        this.federationEndpoints = federationEndpoints;
        return this;
    }

    public Long getFirstItemOffset() {
        return firstItemOffset;
    }

    public LsqConfigImpl setFirstItemOffset(Long firstItemOffset) {
        this.firstItemOffset = firstItemOffset;
        return this;
    }

    public File getFederationConfigFile() {
        return federationConfigFile;
    }

    public LsqConfigImpl setFederationConfigFile(File federationConfigFile) {
        this.federationConfigFile = federationConfigFile;
        return this;
    }

    public String getOutBaseIri() {
        return outBaseIri;
    }

    public LsqConfigImpl setOutBaseIri(String outBaseIri) {
        this.outBaseIri = outBaseIri;
        return this;
    }

    public Long getDatasetSize() {
        return datasetSize;
    }

    public LsqConfigImpl setDatasetSize(Long datasetSize) {
        this.datasetSize = datasetSize;
        return this;
    }

    public boolean isReuseLogIri() {
        return reuseLogIri;
    }

    public LsqConfigImpl setReuseLogIri(boolean reuseLogIri) {
        this.reuseLogIri = reuseLogIri;
        return this;
    }

    public boolean isEmitProcessMetadata() {
        return emitProcessMetadata;
    }

    public LsqConfigImpl setEmitProcessMetadata(boolean emitProcessMetadata) {
        this.emitProcessMetadata = emitProcessMetadata;
        return this;
    }

    public Pattern getQueryIdPattern() {
        return queryIdPattern;
    }

    public LsqConfigImpl setQueryIdPattern(Pattern queryIdPattern) {
        this.queryIdPattern = queryIdPattern;
        return this;
    }

	public String getHttpUserAgent() {
		return httpUserAgent;
	}

	public void setHttpUserAgent(String httpUserAgent) {
		this.httpUserAgent = httpUserAgent;
	}

	public Long getDelayInMs() {
		return delayInMs;
	}

	public LsqConfigImpl setDelayInMs(Long delayInMs) {
		this.delayInMs = delayInMs;
		return this;
	}

	public Iterable<String> getPrefixSources() {
		return prefixSources;
	}

	public LsqConfigImpl setPrefixSources(Iterable<String> prefixSources) {
		this.prefixSources = prefixSources;
		return this;
	}
	
	
}

