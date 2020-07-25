package org.aksw.simba.lsq.cli.main.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.simba.lsq.cli.main.MainCliLsq;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;


@Command(name = "prepare", description = "Prepare a benchmark run")
public class CmdLsqBenchmarkPrepare
    implements Callable<Integer>
{
    @Option(names={"-h", "--help"}, help=true)
    public boolean help = false;

    @Option(names= {"-c", "--config"}, required=true, description="Configuration file (RDF)")
    public String config = null;

//    @Parameters(arity = "1..*", paramLabel="FILE", description="Log files to process")
//    public List<String> logSources = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        MainCliLsq.benchmarkPrepare(this);
        return 0;
    }


}
