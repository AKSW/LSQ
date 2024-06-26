package org.aksw.simba.lsq.cli.cmd.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.simba.lsq.core.LsqRdfizeSpec;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class CmdLsqRdfizeBase
    implements LsqRdfizeSpec
{
    @Option(names={"-h", "--help"}, usageHelp = true)
    public boolean help = false;

    @Option(names={"-b", "--base-uri"}, description="Prefix for generated IRIs for queries, log records, etc.")
    public String baseIri = "http://lsq.aksw.org/";

    @Option(names={"-m", "--format"}, description="Input log format. If absent, probing is attempted.")
    public String inputLogFormat;

    @Option(names={"-n", "--namespaces"}, description="Namespace prefix sources")
    public List<String> prefixSources = new ArrayList<>(Arrays.asList("rdf-prefixes/prefix.cc.2019-12-17.ttl"));

    @Option(names={"--no-merge"}, description="Neither sort name graphs nor merge consecutive ones of same name")
    public boolean noMerge = false;

    @Option(names={"--no-hash"}, description="For privacy, host names are hashed by default. This option prevents it.")
    public boolean noHostHash = false;

    @Option(names={"--salt"}, description="Prepend the given salt to host names before hashing - otherwise a random uuid will be used")
    public String hostSalt = null;

    @Option(names={"-s", "--slim"}, description="Slim output only retains query, hostname, timestamp and sequence id")
    public boolean slimMode = false;


    @ArgGroup(exclusive = true, multiplicity = "1")
    public RdfizationLevel rdfizationLevel = new RdfizationLevel();

    public static class RdfizationLevel {
        @Option(names={"--query-only"}, description="Only RDFize the query. Do not track its occurrence.")
        public boolean queryOnly = false;

        // Endpoint is not needed if --query-only is specified
        @Option(names={"-e", "--endpoint"}, required=true, description="Service endpoint for which the logs were generated")
        public String endpointUrl = null;
    }

    @Option(names = { "-d", "--used-prefixes" }, description = "Number of records (bindings/quads) by which to defer RDF output in order to analyze used prefixes; default: ${DEFAULT-VALUE}", defaultValue = "100")
    public long usedPrefixDefer;


    @Parameters(arity="1..*", description="log sources")
    public List<String> nonOptionArgs = new ArrayList<>();




//	@OptionsDelegate
//	public CmdNgsSort sortOptions = new CmdNgsSort();

    /*
     * Options for sorting operations
     */

    @Option(names={"-S", "--buffer-size"})
    public String bufferSize = "1G";

    @Option(names={"-T", "--temporary-directory"})
    public String temporaryDirectory = null;

    // TODO Integrate oshi to get physical core count by default
    @Option(names={"--parallel"})
    public int parallel = -1;


    public boolean isHelp() {
        return help;
    }

    @Override
    public String getBaseIri() {
        return baseIri;
    }

    @Override
    public String getInputLogFormat() {
        return inputLogFormat;
    }

    @Override
    public List<String> getPrefixSources() {
        return prefixSources;
    }

    @Override
    public boolean isNoMerge() {
        return noMerge;
    }

    @Override
    public boolean isNoHostHash() {
        return noHostHash;
    }

    @Override
    public String getHostSalt() {
        return hostSalt;
    }

    @Override
    public String getEndpointUrl() {
        return rdfizationLevel.endpointUrl;
    }

    @Override
    public boolean isQueryOnly() {
        return rdfizationLevel.queryOnly;
    }

    @Override
    public boolean isSlimMode() {
        return slimMode;
    }

    @Override
    public List<String> getNonOptionArgs() {
        return nonOptionArgs;
    }

    @Override
    public String getBufferSize() {
        return bufferSize;
    }

    @Override
    public String getTemporaryDirectory() {
        return temporaryDirectory;
    }

    @Override
    public int getParallel() {
        return parallel;
    }
}
