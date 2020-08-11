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
For detailed documentation about setup, use and concepts of the LSQ command line tool please refer to our [wiki pages](https://github.com/AKSW/LSQ/wiki).

### Quick Reference

#### Setup

This is a typical maven project and can is thus built with `mvn clean install`.

**For Ubuntu/Debian users:** The build process creates a `.deb` package that can be conviently installed **after build** with

`./reinstall-debs.sh` (requires root access).


#### Quick Usage
A quick reference for the typical process is as follows:

```
lsq probe file.log
lsq rdfize file.log
lsq benchmark create -d myDatasetLabel -e http://localhost:8890/sparql -o > benchmark.conf.ttl
lsq benchmark prepare -c benchmark.conf.ttl -o > benchmark.run.ttl
lsq benchmark run -c benchmark.run.ttl *.log
```

The `-o` option causes the settings to be written to the console. Omit `-o` to have LSQ auto-generate files.


![LSQ Process Overview](lsq-docs/lsq2-overview.svg "")


## License
The source code of this repo is published under the [Apache License Version 2.0](https://github.com/AKSW/jena-sparql-api/blob/master/LICENSE).



