package org.aksw.simba.lsq.cli.main.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.simba.lsq.cli.main.MainCliLsq;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "rdfize", description = "RDFize query logs")
public class CmdLsqRdfize
    implements Callable<Integer>
{
    @Option(names={"-h", "--help"}, help=true)
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

    @Option(names={"-e", "--endpoint"}, required=true, description="Service endpoint for which the logs were generated")
    public String endpointUrl = null;

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

    @Override
    public Integer call() throws Exception {
        MainCliLsq.rdfize(this);
        return 0;
    }

}
