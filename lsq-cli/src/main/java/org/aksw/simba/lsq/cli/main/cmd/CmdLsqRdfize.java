package org.aksw.simba.lsq.cli.main.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.sparql_integrate.ngs.cli.cmd.CmdNgsSort;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

@Parameters(separators = "=", commandDescription = "RDFize query logs")
public class CmdLsqRdfize {
	@Parameter(names={"-h", "--help"}, help=true)
	public boolean help = false;

	@Parameter(names={"-b", "--base-uri"}, description="Prefix for generated IRIs for queries, log records, etc.")
	public String baseIri = "http://lsq.aksw.org/";
	
	@Parameter(names={"-m", "--format"}, description="Input log format. If absent, probing is attempted.")
	public String inputLogFormat;

	@Parameter(names={"-n", "--namespaces"}, description="Namespace prefix sources")
	public List<String> prefixSources = new ArrayList<>(Arrays.asList("rdf-prefixes/prefix.cc.2019-12-17.jsonld"));

	@Parameter(names={"--no-merge"}, description="Neither sort name graphs nor merge consecutive ones of same name")
	public boolean noMerge = false;
	
	@Parameter(names={"--no-hash"}, description="For privacy, host names are hashed by default. This option prevents it.")
	public boolean noHostHash = false;

	@Parameter(names={"--salt"}, description="Prepend the given salt to host names before hashing - otherwise a random uuid will be used")
	public String hostSalt = null;

	@Parameter(names={"-s", "--slim"}, description="Slim output only retains query, hostname, timestamp and sequence id")
	public boolean slimMode = false;

	@Parameter(names={"-e", "--endpoint"}, required=true, description="Service endpoint for which the logs were generated")
	public String endpointUrl = null;

	@Parameter(description="log sources")
	public List<String> nonOptionArgs = new ArrayList<>();

//	@ParametersDelegate
//	public CmdNgsSort sortOptions = new CmdNgsSort();

	/*
	 * Options for sorting operations
	 */

	@Parameter(names={"-S", "--buffer-size"})
	public String bufferSize = "1G";

	@Parameter(names={"-T", "--temporary-directory"})
	public String temporaryDirectory = null;

	// TODO Integrate oshi to get physical core count by default
	@Parameter(names={"--parallel"})
	public int parallel = -1;

}
