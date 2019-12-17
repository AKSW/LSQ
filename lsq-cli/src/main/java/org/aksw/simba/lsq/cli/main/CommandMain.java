package org.aksw.simba.lsq.cli.main;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Show LSQ RDFizer options")
public class CommandMain {
	@Parameter(names={"-h", "--help"}, help=true)
	public boolean help = false;

	@Parameter(names={"-m", "--format"}, description="Input log format")
	public String inputLogFormat;

	@Parameter(names={"-n", "--namespaces"}, description="Namespace prefix sources")
	public List<String> prefixSources = new ArrayList<>();

	@Parameter(description="Non option args")
	public List<String> nonOptionArgs = new ArrayList<>();

	
}