## LSQ: The Linked SPARQL Queries Dataset

####Start-up

The LSQ folder contains the source code. You can start-up with RDFising your own query log from:

```
Package: org.aksw.simba.dataset.lsq

Class: LogRDFizer

```
Complete details given at [LogRDFizer](https://github.com/AKSW/LSQ/blob/gh-pages/LSQ/src/org/aksw/simba/dataset/lsq/LogRDFizer.java) class.

####LSQ Homepage 
The Linked Dataset, a SPARQL endpoint, and complete dumps are all available on the LSQ [homepage](http://aksw.github.io/LSQ/) along with pointers a VoID description, example LSQ queries, and various other dataset assets.

## Command line arguments
* -f apache log file to process
* -e SPARQL endpoint
  * -g default graph for query executions. Can be specified multiple times.
* -l label for the dataset; will be used in URIs
* -b baseUri for generated resources; defaults to http://lsq.aksw.org/res/

## Example usage

From the repository root folder, run:

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
-o, --output <File>   File where to store the output data.                     
-p, --public          Public endpoint URL - e.g. http://example.org/sparql     
-r, --rdfizer         RDFizer selection: Any combination of the letters (e)    
                        xecution, (l)og and (q)uery (default: elq)             
-t, --timeout <Long>  Timeout in milliseconds                                  
-x, --experiment      URI of the experiment environment                        
```

```bash
lsq \
  -f lsq-core/src/test/resources/swdf.log \
  -e http://localhost:8890/sparql \
  -g http://aksw.org/benchmark \
  -l swdf \
  -b http://lsq.aksw.org/res/ \
  -p http://localhost/service/org.semanticweb.swdf_swdf-full_latest_public_sparql \
  -h 10 \
  -r qel \
  -t 60000 \
  -o outfile.ttl
```

```bash
lsq \
  -f lsq-core/src/test/resources/swdf.log \
  -e http://localhost:8890/sparql \
  -g http://aksw.org/benchmark \
  -l swdf \
  -b http://lsq.aksw.org/res/ \
  -p http://localhost/service/org.semanticweb.swdf_swdf-full_latest_public_sparql \
  -h 10 \
  -t 60000 \
  -r qel | rapper -i turtle -o ntriples - http://foo | sort -u > a.ttl
```



Probably outdated lsq -f lsq-core/src/test/resources/swdf.log -e http://localhost:8890/sparql -g http://aksw.org/benchmark -l swdf


## Work in progress
The LSQ tool can be used to RDFize SPARQL queries as well as execute them.

### RDFization


### Execution
* -m local / remote
* -e SPARQL endpoint
  * -g default graph for query executions. Can be specified multiple times.
* TODO environment URI



### Environment creator
Tool for easing the creation of RDF specification for a SPARQL execution environment.
Actually, We could use a registry and use a mixture of dcat, service description, (host description), and void to combine all this information.
E.g. if one specifies dbpedia, we could lookup the host description
