package org.aksw.simba.lsq.cli.main.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.simba.lsq.spark.cmd.impl.CmdRdfIo;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class CmdRdfIoBase implements CmdRdfIo {
    @Option(names = { "-h", "--help" }, usageHelp = true)
    public boolean help = false;

    @Parameters(arity = "1..*", description = "List of input files")
    protected List<String> nonOptionArgs = new ArrayList<>();

    @Option(names = { "--out-file" }, description = "Output folder (for hadoop)")
    protected String outFile;

    @Option(names = { "--out-folder" }, description = "Output folder")
    protected String outFolder;

    @Option(names = { "-m", "--out-format" }, description = "Output RDF format", defaultValue = "trig/blocks")
    protected String outFormat;

    @Option(names = { "-n", "--namespaces" }, description = "Namespace prefix sources")
    public List<String> prefixSources = new ArrayList<>(Arrays.asList("rdf-prefixes/prefix.cc.2019-12-17.ttl"));

    @Option(names = { "-d",
            "--used-prefixes" }, description = "Number of records by which to defer RDF output in order to analyze used prefixes; default: ${DEFAULT-VALUE}", defaultValue = "20")
    protected long deferOutputForUsedPrefixes;

    @Override
    public List<String> getNonOptionArgs() {
        return nonOptionArgs;
    }

    @Override
    public String getOutFile() {
        return outFile;
    }

    @Override
    public CmdRdfIo setOutFile(String outFile) {
        this.outFile = outFile;
        return this;
    }

    @Override
    public String getOutFolder() {
        return outFolder;
    }

    @Override
    public CmdRdfIo setOutFolder(String outFolder) {
        this.outFolder = outFolder;
        return this;
    }

    @Override
    public String getOutFormat() {
        return outFormat;
    }

    @Override
    public CmdRdfIo setOutFormat(String outFormat) {
        this.outFormat = outFormat;
        return this;
    }

    @Override
    public List<String> getPrefixSources() {
        return prefixSources;
    }

    @Override
    public long getDeferOutputForUsedPrefixes() {
        return deferOutputForUsedPrefixes;
    }

    @Override
    public CmdRdfIo setDeferOutputForUsedPrefixes(long n) {
        this.deferOutputForUsedPrefixes = n;
        return this;
    }

}
