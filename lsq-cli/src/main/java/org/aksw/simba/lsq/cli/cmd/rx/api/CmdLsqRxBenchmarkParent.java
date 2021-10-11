package org.aksw.simba.lsq.cli.cmd.rx.api;

import picocli.CommandLine.Command;

@Command(name="benchmark", description = "Manage benchmarks of query logs", subcommands = {
        CmdLsqRxBenchmarkCreate.class,
        CmdLsqRxBenchmarkPrepare.class,
        CmdLsqRxBenchmarkRun.class,
})
public class CmdLsqRxBenchmarkParent {

}
