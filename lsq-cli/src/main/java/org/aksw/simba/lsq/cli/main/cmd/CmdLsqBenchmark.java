package org.aksw.simba.lsq.cli.main.cmd;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Show LSQ RDFizer options")
public class CmdLsqBenchmark {
	@Parameter(names={"-h", "--help"}, help=true)
	public boolean help = false;

	@Parameter(description="Non option args")
	public List<String> nonOptionArgs = new ArrayList<>();

	@Parameter(names= {"-a", "--user-agent"}, description="User agent")
	public String userAgent = "Linked SPARQL Queries (LSQ) Client";

	@Parameter(names= {"-s", "--dataset-size"}, description="Dataset size used in statistics. If not given, it will be queried which might fail")
	public Long datasetSize = null;

	@Parameter(names= {"-e", "--endpoint"}, description="SPARQL endpoint URL on which to execute queries")
	public String endpoint = null;

	@Parameter(names= {"-g", "--default-graph"}, description="Graph(s) to use as the default graph - i.e. plain triple patterns such as { ?s ?p ?o } will match on those graphs")
	public List<String> defaultGraphs = new ArrayList<>();

	@Parameter(names= {"-t", "--timeout"}, description="Timeout in milliseconds")
	public Long timeoutInMs = null;

	@Parameter(names= {"-d", "--delay"}, description="Delay between query requests in milliseconds")
	public Long delayInMs = 0l;

	@Parameter(names= {"-x", "--experiment"}, description="IRI for the experiment. Configuration and start/end time time will be attached to it.")
	public String experimentIri = null;

	
//	@Parameter(names={"-n", "--namespaces"}, description="Namespace prefix sources")
//	public List<String> prefixSources = new ArrayList<>();

	
	//	-x, --experiment <String>    URI of the experiment environment                  	
//-i, --logirisasbase          Use IRIs in RDF query logs as the base IRIs        
//-l, --label <String>         Label of the dataset, such as 'dbpedia' or 'lgd'.  
//            Will be used in URI generation (default: mydata) 
//	-p, --public <String>        Public endpoint URL for record purposes - e.g.     
//    http://dbpedia.org/sparql          
//	-r, --rdfizer [String]       RDFizer selection: Any combination of the letters  
//    (e)xecution, (l)og, (q)uery and (p)rocess        
//    metadata (default: elq)
//    -q, --querypattern [String]  Pattern to parse out query ids; use empty string   
//    to use whole IRI (default: q-([^->]+))           
//    
	
}