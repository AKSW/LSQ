package org.aksw.simba.lsq.cli.main.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.aksw.simba.lsq.cli.main.MainCliLsq;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "create", description = "Create a benchmark configuration")
public class CmdLsqBenchmarkCreate
    implements Callable<Integer>
{
    @Option(names={"-h", "--help"}, help=true)
    public boolean help = false;

    @Option(names= {"-o", "--stdout"}, description="Output generated config to STDOUT instead of a file")
    public boolean stdout = false;

    @Option(names= {"-a", "--user-agent"}, description="User agent")
    public String userAgent = "Linked SPARQL Queries (LSQ) Client";

    @Option(names={"-b", "--base-uri"}, description="Prefix for generated IRIs for queries, log records, etc.")
    public String baseIri = "http://lsq.aksw.org/";

    @Option(names= {"-s", "--dataset-size"}, description="Dataset size used in statistics. If not given, it will be queried which might fail")
    public Long datasetSize = null;

    // The distribution Id on which the benchmarking is performed

//	@Parameter(names= {"--catalog", "--catalog"}, required=true, description="SPARQL endpoint URL on which to execute queries")
//	public String catalog = null;
//
//	@Parameter(names= {"--dist", "--distribution"}, required=true, description="SPARQL endpoint URL on which to execute queries")
//	public String distribution = null;

    @Option(names= {"-d", "--dataset"}, required=true, description="DatasetID identifier for the benchmark dataset")
    public String dataset = null;

    @Option(names= {"-e", "--endpoint"}, required=true, description="SPARQL endpoint URL on which to execute queries")
    public String endpoint = null;

    @Option(names= {"-g", "--default-graph"}, description="Graph(s) to use as the default graph - i.e. plain triple patterns such as { ?s ?p ?o } will match on those graphs")
    public List<String> defaultGraphs = new ArrayList<>();

    @Option(names= {"--ct", "--connection-timeout"}, description="Timeout in milliseconds")
    public Long connectionTimeoutInMs = null;

    @Option(names= {"--qt", "--query-timeout"}, description="Timeout in milliseconds")
    public Long queryTimeoutInMs = null;

    @Option(names= {"-y", "--delay"}, description="Delay between query requests in milliseconds")
    public Long delayInMs = 0l;

    @Option(names= {"-x", "--experiment"}, description="IRI for the experiment. Configuration and start/end time time will be attached to it.")
    public String experimentIri = null;


    @Override
    public Integer call() throws Exception {
        MainCliLsq.benchmarkCreate(this);
        return 0;
    }
}
