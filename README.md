# The Linked SPARQL Queries (LSQ) Framework
LSQ is a framework for RDFizing arbitrary SPARQL query logs with support for several types of analysis.
This project page describes the framework; information about the Linked Dataset, a SPARQL endpoint, and complete dumps are all available on the LSQ [homepage](http://dice-group.github.io/LSQ/) along with pointers a VoID description, example LSQ queries, and various other dataset assets.


## Architecture
LSQ's architecture is based on a classic batch processing one.

* An item reader reads each entry of the input log file, normalizes the data by converting it to an RDF representation (based on the log format)
* An item processor performs the analysis steps according to the provided configuration.
* An output writer serializes each target resource to a file or STDOUT.

### Supported Analysis Types
* Structural features: This comprises metrics directly from a SPARQL query, such as the number of triple patterns, BGPs, projection variables.
* Executions analysis measures aspects of a query and its parts, such as BGPs and TPs, in regard to a dataset hosted in an RDF store. These aspects are:
 * Performance analysis
 * Result set sizes and selectivity

Executions can be marked as local and remote: a remote execution of a query takes place at the SPARQL endpoint whose log file contained the query, whereas a local execution take place on a 'local' instance of that RDF store setup.


### Supported log formats
Default log formats are configured in the [default-log-formats.ttl](lsq-core/src/main/resources/default-log-formats.ttl) file, which contains entries such as:
```rdf
fmt:combined
  a lsq:WebAccessLogFormat ;
  lsq:pattern "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-agent}i\"" ;
.
```
At present, LSQ performs lookup of patterns by their local name, such as _combined_ in this example.

The pattern definitions follow the specification of Apache's [mod_log_config](http://httpd.apache.org/docs/current/mod/mod_log_config.html).
Custom log formats can thus be easily added by simply extending the underlying RDF model.


## Vocabulary
![LSQ Vocabulary Depiction](lsq-docs/lsq-vocab.png "")


## Java API

## The command line client (CLI)

#### Building the CLI Jar
Build the whole project with

```
mvn clean install
```

Build the command line client with
```bash
cd lsq-cli
mvn assembly:assembly
```

A self-contained jar is then located under lsq-cli/target/lsq-cli-{version}-jar-with-dependencies.jar
You can run it with

```bash
java -cp `find . -name 'lsq*jar-with-dependencies.jar'` org.aksw.simba.lsq.cli.main.MainLSQ
```

```bash
alias lsq='java -cp `find "/path/to/lsq-cli/target/" -name "lsq*jar-with-dependencies.jar"` org.aksw.simba.lsq.cli.main.MainLSQ'
```



#### Building the CLI debian package
This happends when you build the project (under lsq-debian-cli/target)
You can conveniently install it with

```bash
sudo dpkg -i `find . -name '*.deb'`
```


## Example usage

The following options exist:
```bash
Option                Description                                              
------                -----------                                              
-b, --base            Base URI for URI generation (default: http://lsq.aksw.   
                        org/res/)                                              
-e, --endpoint        Local SPARQL service (endpoint) URL on which to execute  
                        queries (default: http://localhost:8890/sparql)        
-f, --file <File>     File containing input data                               
-g, --graph           Local graph(s) from which to retrieve the data           
-h, --head <Long>     Only process n entries starting from the top             
-l, --label           Label of the dataset, such as 'dbpedia' or 'lgd'. Will be
                        used in URI generation (default: mydata)               
-m, --format          Format of the input data. Available options: [virtuoso,  
                        apache, distributed, sparql] (default: apache)                              
-o, --output <File>   File where to store the output data.                     
-p, --public          Public endpoint URL - e.g. http://example.org/sparql     
-r, --rdfizer         RDFizer selection: Any combination of the letters (e)    
                        xecution, (l)og and (q)uery (default: elq)             
-t, --timeout <Long>  Timeout in milliseconds  
-w --outformat            output format, e.g., "N-Triples/ascii" default is "Turtle/blocks"
-x, --experiment      URI of the experiment environment ```

From the repository root folder, run:

```bash
lsq \
  -f lsq-core/src/test/resources/swdf.apache.log \
  -e http://localhost:8890/sparql \
  -g http://aksw.org/benchmark \
  -l swdf \
  -b http://lsq.aksw.org/res/ \
  -p http://data.semanticweb.org/sparql \
  -h 10 \
  -r qel \
  -t 60000 \
  -o outfile.ttl
```

```bash
lsq \
  -f lsq-core/src/test/resources/dbpedia.virtuoso.log \
  -m virtuoso \
  -e http://localhost:8890/sparql \
  -g http://aksw.org/benchmark \
  -l swdf \
  -b http://lsq.aksw.org/res/ \
  -p http://data.semanticweb.org/sparql \
  -h 10 \
  -t 60000 \
  -r qel | rapper -i turtle -o ntriples - http://foo | sort -u > a.ttl
```


