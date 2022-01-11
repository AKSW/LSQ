# Linked SPARQL Queries (LSQ) Framework
A framework for RDFizing query logs and benchmarking queries and graph patterns.

## What's New

### 2020-08-06 LSQ2 Pre-release
LSQ2 introduces significant improvements over the prior version in every aspect: Ease-of-use, flexibility, modularity, consintency in the data model and generated IDs.

* Pretty CLI (thanks to on [picocli](https://github.com/remkop/picocli))
* Easier yet more flexible to use: RDFization, static analysis and benchmarking now decoupled
* Named graph stream approach: Information for each query is grouped in its own named graph which allows easily selecting subsets with complete information for detailed analysis.


## Documentation

### Detailed Documentation
For detailed documentation about setup, use and concepts of the LSQ command line tool please refer to our [LSQ Website](http://lsq.aksw.org/).

### Quick Reference

#### Setup

This is a typical maven project and can is thus built with `mvn clean install`.

**For Ubuntu/Debian users:** The build process creates a `.deb` package that can be conviently installed **after build** with

`./reinstall-deb.sh` (requires root access).


#### Quick Usage
A quick reference for the typical process is as follows:

```
lsq rx probe file.log
lsq rx rdfize -e http://server.from/which/the/log/is/from file.log > file.log.trig
lsq rx benchmark create -d myDatasetLabel -e http://localhost:8890/sparql -o > benchmark.conf.ttl
lsq rx benchmark prepare -c benchmark.conf.ttl -o > benchmark.run.ttl
lsq rx benchmark run -c benchmark.run.ttl *.log.trig
```

The `-o` option causes the settings to be written to the console. Omit `-o` to have LSQ auto-generate files.


#### Run with Docker

Run example running LSQ to RDFize SPARQL logs, input and output files in the current working directory (replace `$(pwd)` by `${PWD}` for Windows PowerShell):

```bash
docker run -it -v $(pwd):/data ghcr.io/aksw/lsq rx rdfize --endpoint=http://dbpedia.org/sparql virtuoso.dbpedia.log 
```

Build the Docker image from the source code:

```bash
docker build -t ghcr.io/aksw/lsq .
```


## License
The source code of this repo is published under the [Apache License Version 2.0](https://github.com/AKSW/jena-sparql-api/blob/master/LICENSE).



