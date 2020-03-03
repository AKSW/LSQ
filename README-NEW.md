## Work in Progress

The new version of LSQ now allows for decoupling of the RDFization of log files from the analysis.
The main improvements of the now approach of LSQ are:

* No more single monolithic processing command: The new approach is easier-to-use and less error-prone.
* No more single monolithic RDF output. We once created 3TB of plain ntriples - which was a huge pain to process. LSQ now employs an RDF Stream Processing approach, such that related information about web log entries and queries are grouped together in named graphs. Each named graph thus represents a meaningful unit of information that can be used indepedently.

## Working with streams of named graphs

* From the command line: Our [sparql-integrate](https://github.com/SmartDataAnalytics/Sparqlintegrate) project ships with the "named graph stream" (ngs) tool. The deduplication of RDFized web logs depends on NGS.
* * If you like the tool but miss packaging for your system, please consider contributing one :)
* From Apache Spark: We are working on components that would enable an implementation of NGS running on Apache Spark. However, there is no estimate on that yet.



## New features in LSQ 2

* format probing
* standalone log rdfization
* a-priori deduplication


### Probing for best matching format

The `lsq probe` sub-command attempts to parse a sample of `n` lines from each given files with all registered parser ordered by weight.
The weight corresponds to the average number of predicates obtained by parsing the sample using a format.
In detail, the weight is `w := parseRatio * avgNumberOfImmediatePredicates` with
`parseRatio = number of successfully parsed lines / n` and avgNumberOfImmediatePredicates is the average number of predicates among the successfully parsed lines.
Note, that that multiple files can be processed at once using globbing.
The following is the output on the test resources of the lsq project, and at the time of writing all resources were correctly classified.


```
➜  lsq probe lsq-core/src/test/resources/logs/*
lsq-core/src/test/resources/logs/bio2rdf.unknown.log	[bio2rdf=7.0, sparql2=2.0]
lsq-core/src/test/resources/logs/combined.swdf.log	[combined=10.0, common=9.0, distributed=9.0, bio2rdf=7.0, sparql2=2.0]
lsq-core/src/test/resources/logs/combined.swdf.single-query.log	[combined=10.0, common=9.0, distributed=9.0, bio2rdf=7.0, sparql2=2.0]
lsq-core/src/test/resources/logs/combined.swdf.single-triple-pattern.log	[combined=10.0, common=9.0, distributed=9.0, bio2rdf=7.0, sparql2=2.0]
lsq-core/src/test/resources/logs/commonVirtuoso.dbpedia.20151025-1.log	[commonVirtuoso=9.0, virtuoso=5.0, sparql2=2.0]
lsq-core/src/test/resources/logs/commonVirtuoso.dbpedia.20160411-1000.log	[commonVirtuoso=9.0, virtuoso=5.036, sparql2=2.0]
lsq-core/src/test/resources/logs/sparql2.debug.log	[sparql2=2.0, sparql=1.0]
lsq-core/src/test/resources/logs/sparql2.debug.ttl	[sparql2=2.0]
lsq-core/src/test/resources/logs/virtuoso.dbpedia351.log	[virtuoso=6.0, sparql2=2.0]
lsq-core/src/test/resources/logs/virtuoso.dbpedia.log	[virtuoso=6.0, sparql2=2.0]
lsq-core/src/test/resources/logs/wikidata.wikidata.20170612-10.log	[wikidata=4.0, sparql2=2.0]
```


## A-priori deduplication of queries




