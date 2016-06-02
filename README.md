## LSQ: The Linked SPARQL Queries Dataset

#### Building the command line client
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
alias lsq=java -cp `find {your-absolute-folder} -name 'lsq*jar-with-dependencies.jar'` org.aksw.simba.lsq.cli.main.MainLSQ
```



#### Building the debian package
This happends when you build the project (under lsq-debian-cli/target)
You can conveniently install it with

```bash
sudo dpkg -i `find . -name '*.deb'`
```

####LSQ Homepage 
The Linked Dataset, a SPARQL endpoint, and complete dumps are all available on the LSQ [homepage](http://aksw.github.io/LSQ/) along with pointers a VoID description, example LSQ queries, and various other dataset assets.

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
-o, --output <File>   File where to store the output data.                     
-p, --public          Public endpoint URL - e.g. http://example.org/sparql     
-r, --rdfizer         RDFizer selection: Any combination of the letters (e)    
                        xecution, (l)og and (q)uery (default: elq)             
-t, --timeout <Long>  Timeout in milliseconds                                  
-x, --experiment      URI of the experiment environment                        
```

From the repository root folder, run:

```bash
lsq \
  -f lsq-core/src/test/resources/swdf.log \
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
  -f lsq-core/src/test/resources/swdf.log \
  -e http://localhost:8890/sparql \
  -g http://aksw.org/benchmark \
  -l swdf \
  -b http://lsq.aksw.org/res/ \
  -p http://data.semanticweb.org/sparql \
  -h 10 \
  -t 60000 \
  -r qel | rapper -i turtle -o ntriples - http://foo | sort -u > a.ttl
```


