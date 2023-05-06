package org.aksw.simba.lsq.cli.cmd.spark.api;

import picocli.CommandLine.Command;

@Command(name="spark", description = "LSQ Spark Subcommands", subcommands = {
        CmdLsqSparkProbe.class,
        CmdLsqSparkRdfize.class,
        CmdLsqSparkAnalyze.class
})
public class CmdLsqSparkParent {

}
