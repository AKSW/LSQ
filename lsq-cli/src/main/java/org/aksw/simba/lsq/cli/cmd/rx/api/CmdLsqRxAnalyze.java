package org.aksw.simba.lsq.cli.cmd.rx.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.simba.lsq.cli.main.MainCliLsq;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Static analysis of queries
 *
 * @author raven
 *
 */
@Command(name = "analyze", description = "Analyze queries and emit structural features")
public class CmdLsqRxAnalyze implements Callable<Integer> {
    @Option(names = { "-h", "--help" }, usageHelp = true)
    public boolean help = false;

    @Parameters(arity = "1..*", description = "file-list to probe")
    public List<String> nonOptionArgs = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        MainCliLsq.analyze(this);
        return 0;
    }
}
