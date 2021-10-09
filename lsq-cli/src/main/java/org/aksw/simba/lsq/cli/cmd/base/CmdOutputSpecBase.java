package org.aksw.simba.lsq.cli.cmd.base;

import picocli.CommandLine.Option;

public class CmdOutputSpecBase {
    @Option(names = { "-o", "--out-format" }, description = "Output format", defaultValue = "trig/blocks")
    public String outFormat = null;

    @Option(names = { "-f", "--out-file" }, description = "Output file")
    public String outFile = null;
}
