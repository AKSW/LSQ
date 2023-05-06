package org.aksw.simba.lsq.cli.trash;

import java.io.File;
import java.util.List;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.apache.jena.rdf.model.Resource;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators="=", commandDescription="Parameters")
public interface LsqCliConfig
	extends Resource
{
	
//	@Iri("eg:nonOptionArg")
//	List<String> getNonOptionArgs();
//
//	@Iri("eg:help")
//	boolean isHelp();// = false;
//			
//	@Parameter(names = "--help", help = true)
//	CommandMain setHelp(boolean help);
//	
//	@Parameter(description = "Non option args")
//	CommandMain setNonOptionArgs(List<String> args);
	
	
	@IriNs("lsq")
	List<String> getNonOptionArgs();

	@Parameter(names="--help", help=true)
	LsqCliConfig setNonOptionArgs(List<String> args);

	@IriNs("lsq")
	Boolean isHelp();
	LsqCliConfig setHelp(Boolean isHelp);

	
	@IriNs("lsq")
    String getInput();
    LsqCliConfig setInput(String inputOs);

	@IriNs("lsq")
    File getOutput();
	LsqCliConfig setOutput(File output);
    
    
	@IriNs("lsq")
    String getLogFormat();
	LsqCliConfig setLogFormat(String logFormat);
	
	@IriNs("lsq")
    String getOutFormat();
	LsqCliConfig setOutFormat(String outFormat);

	@IriNs("lsq")
	String getRdfizer();
	LsqCliConfig setRdfizer(String rdfizer);
    
	@IriNs("lsq")
    String getBenchmarkEndpointUrl();
	LsqCliConfig setBenchmarkEndpointUrl(String benchmarkEndpointUrl);
	
	@IriNs("lsq")
    String getGraphUriOs();
	LsqCliConfig setGraphUriOs(String graphUri);

	@IriNs("lsq")
    String getDatasetLabel();
	LsqCliConfig setDatasetLabel(String datasetLabel);

	@IriNs("lsq")
	Long getHead();
	LsqCliConfig setHead(Long head);
	
	@IriNs("lsq")
    Long getDatasetSize();
	LsqCliConfig setDatasetSize(Long datasetSize);
	
	@IriNs("lsq")
    Long getTimeoutInMs();
	LsqCliConfig setTimeoutInMs(Long timeoutInMs);
	
	@IriNs("lsq")
    String getBaseUri();
	LsqCliConfig setBaseUri(String baseUri);
    
	@IriNs("lsq")
    Boolean getLogIriAsBaseIri();
	LsqCliConfig setLogIriAsBaseIri(String logIriAsBaseIri);
	
	@IriNs("lsq")
    Boolean getQueryIdPattern();
	LsqCliConfig setQueryIdPattern(Boolean queryIdPattern);
	
	@IriNs("lsq")
    String getDatasetEndpointUri();
	LsqCliConfig setDatasetEndpointUri(String datasetEndpointUri);
	
	@IriNs("lsq")
    String getExpBaseUri();
	LsqCliConfig exprBaseUri(String expBaseUri);
	
	@IriNs("lsq")
    List<String> getFedEndpoints();
	LsqCliConfig setFedEndpoints(List<String> fedEndpoints);

	@IriNs("lsq")
    List<File> getFedEndpointsFile();
    LsqCliConfig setFedEndpointsFile(List<File> files);
	
}
