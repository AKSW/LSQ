---
parent: Usage
title: Rx vs Spark
nav_order: 1
---

# Rx vs Spark

Several LSQ commands come in two flavours: `rx` and `spark`.
[RxJava](https://github.com/ReactiveX/RxJava) is a framework for building workflows over streaming data.
Spark is framework for processing large amounts of (input) data in parallel, both locally and on a cluster.
It is possible to (re-)use rx-based functions within spark's `rdd.mapPartition` feature - and this is what `lsq spark` does.

Concretely, LSQ builds on [SANSA-Stack](https://github.com/SANSA-Stack/SANSA-Stack) which provides a foundation for working with RDF/SPARQL in Apache Spark.


## Differences
* `rx` commands are more lightweight and support immediate streaming from stdin (see the note on sorting below). For small input data they may perform faster because there is no overhead in initializing spark.
* `spark` commands can read input sources in parallel and thus **significantly** outperform plain `rx` on larger input data.

### A Note on Sorting
`lsq {spark|rx} rdfize` by default tries inverts log entries into a query centric named graphs and then sorts and merges them.
For this task the `rx` implementation used to rely on platform-specific sorting via `/usr/bin/sort` and will be updated to use a cross-platform Spark-based implementation.
