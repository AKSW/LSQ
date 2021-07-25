---
parent: Usage
title: Probing Log Files
nav_order: 1
---

Probing is performed with the `lsq probe` subcommand. Multiple files can be processed at once using globbing.

It attempts to automatically detect the format of an input log in order to recognize the semantics of the contained records.
By default, up to the first 1000 lines of a log file are parsed against all known log formats. The format for which the highest score could be obtained wins.

In detail, for a given sample of _n_ lines, the score _s_ is `s := parseRatio * avgNumberOfImmediatePredicates` with
`parseRatio = number of successfully parsed lines / n` and avgNumberOfImmediatePredicates is the average number of RDF predicates obtained from successfully parsed lines with the log format being probed.

The following is the output on the test resources of the lsq project, and at the time of writing all resources were correctly classified:

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

LSQ's database of default known log formats is stored in the file [default-log-formats.ttl](https://github.com/AKSW/LSQ/blob/master/lsq-parser/src/main/resources/default-log-formats.ttl).
The patterns are processed by the [lsq-parser](https://github.com/AKSW/LSQ/blob/master/lsq-parser/) module which implements a subset of Apache's [mod_log_config options](http://httpd.apache.org/docs/current/mod/mod_log_config.html).
