package org.aksw.simba.lsq.cli.cmd.spark;

import picocli.CommandLine.Command;

@Command(name="spark", description = "LSQ Spark Subcommands", subcommands = {
        CmdLsqProbeSpark.class,
})
public class CmdLsqSparkParent {

}
