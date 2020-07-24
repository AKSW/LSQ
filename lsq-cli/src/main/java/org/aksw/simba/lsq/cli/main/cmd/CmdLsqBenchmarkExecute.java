package org.aksw.simba.lsq.cli.main.cmd;

import java.util.ArrayList;
import java.util.List;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(name="Execute a benchmark run")
public class CmdLsqBenchmarkExecute {
    @Option(names={"-h", "--help"}, help=true)
    public boolean help = false;

    @Option(names= {"-c", "--config"}, required=true, description="Configuration file (RDF)")
    public String config = null;

    @Parameters(arity = "1..*", paramLabel="FILE", description="Log files to process")
    public List<String> logSources = new ArrayList<>();
}

