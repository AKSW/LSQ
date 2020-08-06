package org.aksw.simba.lsq.cli.main.cmd;

import picocli.CommandLine.Command;

@Command(name="benchmark", description = "Manage benchmarks of query logs", subcommands = {
        CmdLsqBenchmarkCreate.class,
        CmdLsqBenchmarkPrepare.class,
        CmdLsqBenchmarkRun.class,
})
public class CmdLsqBenchmarkMain {

}
