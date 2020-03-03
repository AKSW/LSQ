package org.aksw.simba.lsq.cli.main.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "LSQ Subcommands")
public class CmdLsqMain {
	@Parameter(names={"-h", "--help"}, help=true, description="Show general help")
	public boolean help = false;
}
