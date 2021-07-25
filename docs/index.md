LSQ is a framework for extracting, analyzing and benchmarking SPARQL queries.

Sources of queries are typically logs of triple stores or web servers in general.
Analyzation involves extracting the basic graph patterns, used features such as functions and operators, and statistics of these features.
Benchmarking evaluates queries by executing them against a given SPARQL endpoint thereby measuring performance and result set characteristics.

## Installation

* [Setup](Setup) of the LSQ Framework

## Using the LSQ Command Line Tool

* [Probing Logs](Probing-Logs) for their format
* [RDFization](RDFization) of query logs
* [Benchmarking](Benchmarking) documents how to create and run benchmarks

## Useful Queries over LSQ datasets

Some of the useful queries which can be executed over LSQ datasets are available [here](https://docs.google.com/spreadsheets/d/1jndGJ2qicN2WworS3Q_4FAjM_QOEUGlWiPPX4hkRoNE/edit?usp=sharing) 

## Understanding LSQ

* [Data Model](Data-Model) of LSQ for capturing SPARQL queries, their constituents and benchmark executions
* [Named Graph Streams](Named-Graph-Streams) explains the design choices for using named graphs instead of vanilla triples
* [Skolemization](Skolemization) explains the procedure employed by LSQ for crafting IRIs based on a model full of blank nodes.
