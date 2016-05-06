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
lsq -f lsq-core/src/test/resources/swdf.log -e http://localhost:8890/sparql -g http://aksw.org/benchmark -l swdf

