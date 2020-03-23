package org.aksw.simba.lsq.cli.main.cmd;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Show LSQ RDFizer options")
public class CmdLsqBenchmarkRun {
    @Parameter(names={"-h", "--help"}, help=true)
    public boolean help = false;

    @Parameter(names= {"-c", "--config"}, required=true, description="Configuration file (RDF)")
    public String config = null;

    @Parameter(description="Log files to process")
    public List<String> logSources = new ArrayList<>();
}