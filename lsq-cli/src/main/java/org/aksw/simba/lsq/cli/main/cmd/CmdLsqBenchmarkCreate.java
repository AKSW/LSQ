package org.aksw.simba.lsq.cli.main.cmd;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Show LSQ RDFizer options")
public class CmdLsqBenchmarkCreate {
    @Parameter(names={"-h", "--help"}, help=true)
    public boolean help = false;


    @Parameter(names= {"-a", "--user-agent"}, description="User agent")
    public String userAgent = "Linked SPARQL Queries (LSQ) Client";

    @Parameter(names={"-b", "--base-uri"}, description="Prefix for generated IRIs for queries, log records, etc.")
    public String baseIri = "http://lsq.aksw.org/";

    @Parameter(names= {"-s", "--dataset-size"}, description="Dataset size used in statistics. If not given, it will be queried which might fail")
    public Long datasetSize = null;

    // The distribution Id on which the benchmarking is performed

//	@Parameter(names= {"--catalog", "--catalog"}, required=true, description="SPARQL endpoint URL on which to execute queries")
//	public String catalog = null;
//
//	@Parameter(names= {"--dist", "--distribution"}, required=true, description="SPARQL endpoint URL on which to execute queries")
//	public String distribution = null;

    @Parameter(names= {"-d", "--dataset"}, required=true, description="DatasetID identifier for the benchmark dataset")
    public String dataset = null;

    @Parameter(names= {"-e", "--endpoint"}, required=true, description="SPARQL endpoint URL on which to execute queries")
    public String endpoint = null;

    @Parameter(names= {"-g", "--default-graph"}, description="Graph(s) to use as the default graph - i.e. plain triple patterns such as { ?s ?p ?o } will match on those graphs")
    public List<String> defaultGraphs = new ArrayList<>();

    @Parameter(names= {"--ct", "--connection-timeout"}, description="Timeout in milliseconds")
    public Long connectionTimeoutInMs = null;

    @Parameter(names= {"--qt", "--query-timeout"}, description="Timeout in milliseconds")
    public Long queryTimeoutInMs = null;

    @Parameter(names= {"-y", "--delay"}, description="Delay between query requests in milliseconds")
    public Long delayInMs = 0l;

    @Parameter(names= {"-x", "--experiment"}, description="IRI for the experiment. Configuration and start/end time time will be attached to it.")
    public String experimentIri = null;

}