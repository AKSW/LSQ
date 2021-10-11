package org.aksw.simba.lsq.cli.cmd.rx.api;

import picocli.CommandLine.Command;

@Command(name="rx", version="LSQ version", description = "Tasks based on rx", subcommands = {
        CmdLsqRxAnalyze.class,
        CmdLsqRxRdfize.class,
        CmdLsqRxBenchmarkParent.class,
        CmdLsqRxProbe.class,
})
public class CmdLsqRxParent {
}
