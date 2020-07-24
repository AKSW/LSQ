package org.aksw.simba.lsq.cli.main.cmd;

import picocli.CommandLine.Command;

@Command(name="benchmark", description = "LSQ Benchmark Subcommands", subcommands = {
        CmdLsqBenchmarkCreate.class,
        CmdLsqBenchmarkPrepare.class,
        CmdLsqBenchmarkExecute.class,
})
public class CmdLsqBenchmarkMain {

}
