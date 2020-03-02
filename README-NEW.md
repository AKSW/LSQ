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

The `lsq-probe` command attempts to parse a sample of `n` lines from a given file with all registered parser ordered by weight.
The weight corresponds to the average number of predicates obtained by parsing the sample using a format.
In detail, the weight is `w := parseRatio * avgNumberOfImmediatePredicates` with
`parseRatio = number of successfully parsed lines / n` and avgNumberOfImmediatePredicates is the average number of predicates among the successfully parsed lines.


```bash
$> lsq-probe wikidata-sample.log

wikidata-sample.log [wikidata, sparql2]
```

## A-priori deduplication of queries




