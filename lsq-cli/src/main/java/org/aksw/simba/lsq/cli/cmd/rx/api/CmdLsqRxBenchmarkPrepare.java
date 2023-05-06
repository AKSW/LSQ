package org.aksw.simba.lsq.cli.cmd.rx.api;

import java.util.concurrent.Callable;

import org.aksw.simba.lsq.cli.main.MainCliLsq;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "prepare", description = "Prepare a benchmark run")
public class CmdLsqRxBenchmarkPrepare implements Callable<Integer> {
    @Option(names = { "-h", "--help" }, usageHelp = true)
    public boolean help = false;

    @Option(names = { "-o", "--stdout" }, description = "Output generated config to STDOUT instead of a file")
    public boolean stdout = false;

    @Option(names = { "-c", "--config" }, required = true, description = "Configuration file (RDF)")
    public String config = null;

//    @Parameters(arity = "1..*", paramLabel="FILE", description="Log files to process")
//    public List<String> logSources = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        MainCliLsq.benchmarkPrepare(this);
        return 0;
    }

}
