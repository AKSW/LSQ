package org.aksw.simba.lsq.cli.main.cmd;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "RDFize query logs")
public class CmdLsqRdfize {
	@Parameter(names={"-h", "--help"}, help=true)
	public boolean help = false;

	@Parameter(names={"-m", "--format"}, description="Input log format")
	public String inputLogFormat;

	@Parameter(names={"-n", "--namespaces"}, description="Namespace prefix sources")
	public List<String> prefixSources = new ArrayList<>();

	@Parameter(names={"--no-merge"}, description="Do not merge inverted output by graph")
	public boolean noMerge = false;
	
	@Parameter(names={"--no-hash"}, description="For privacy, host names are hashed by default. This option prevents it.")
	public boolean noHostHash = false;

	@Parameter(names={"--salt"}, description="Prepend the given salt to host names before hashing - otherwise a random uuid will be used")
	public String hostSalt = null;

	@Parameter(names={"-s", "--slim"}, description="Slim output only retains query, hostname and timestamp")
	public boolean slimMode = false;

	@Parameter(description="log sources")
	public List<String> nonOptionArgs = new ArrayList<>();
}
