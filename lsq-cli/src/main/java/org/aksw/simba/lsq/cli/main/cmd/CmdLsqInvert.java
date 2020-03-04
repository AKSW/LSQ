package org.aksw.simba.lsq.cli.main.cmd;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Create a query-centric view from RDFized logs. Implicitly deduplicates queries.")
public class CmdLsqInvert {
	@Parameter(names={"-h", "--help"}, help=true)
	public boolean help = false;

//	@Parameter(names={"-m", "--format"}, description="Input log format")
//	public String inputLogFormat;
//
//	@Parameter(names={"-n", "--namespaces"}, description="Namespace prefix sources")
//	public List<String> prefixSources = new ArrayList<>();

	@Parameter(description="file-list to probe")
	public List<String> nonOptionArgs = new ArrayList<>();

	
}