package org.aksw.simba.lsq.cli.main.cmd;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Static analysis of queries
 * 
 * @author raven
 *
 */
@Parameters(separators = "=", commandDescription = "Analyze queries and emit structural features")
public class CmdLsqAnalyze {
	@Parameter(names={"-h", "--help"}, help=true)
	public boolean help = false;

	@Parameter(description="file-list to probe")
	public List<String> nonOptionArgs = new ArrayList<>();

}
