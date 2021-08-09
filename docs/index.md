# LSQ
LSQ is a framework for extracting, analyzing and benchmarking SPARQL queries. A set of Linked Datasets describing SPARQL queries extracted from the logs of a variety of prominent public SPARQL endpoints. We argue that LSQ datasets have a variety of uses for the SPARQL research community, be it, for example, to generate benchmarks on-the-fly by selecting real-world queries with specific characteristics that we describe, or to conduct analysis of what SPARQL (1.1) query features are most often used to interrogate endpoints, or to characterize the behavior of the different types of agents that are using these endpoints, or to find out what queries agents are asking about a given resource, etc. 
  
Sources of queries are typically logs of triple stores or web servers in general.
Analyzation involves extracting the basic graph patterns, used features such as functions and operators, and statistics of these features.
Benchmarking evaluates queries by executing them against a given SPARQL endpoint thereby measuring performance and result set characteristics.

## Installation

* [Setup](Setup) of the LSQ Framework

## Using the LSQ Command Line Tool

* [Probing Logs](Probing-Logs) for their format
* [RDFization](RDFization) of query logs
* [Benchmarking](Benchmarking) documents how to create and run benchmarks

## LSQ datasets and Public SPARQL endpoint. 
The RDF dumps of the LSQ v2.0 datasets are available [here](https://hobbitdata.informatik.uni-leipzig.de/lsqv2/dumps/). We have also uploaded them virtuoso 7.2 available from [here](https://hobbitdata.informatik.uni-leipzig.de/lsqv2/endpoints/lsq-endpoints-v2/). A single virtuoso triplestore for the complete LSQ v2.0 is available from [here](https://hobbitdata.informatik.uni-leipzig.de/lsqv2/endpoints/). We also provide a public SPARQL endpoint for the complete LSQ v2.0 at [https://lsq.data.dice-research.org/sparql](https://lsq.data.dice-research.org/sparql), where each LSQ dataset is loaded into a separate named graph and can be retrieved using the following SPARQL query. 
```javascript
select distinct ?g where {Graph ?g {?s ?p ?o}}
```
LSQ V1.0 is available from [here](https://hobbitdata.informatik.uni-leipzig.de/lsq/).
## Useful Queries over LSQ datasets

Some of the useful queries which can be executed over LSQ datasets are available [here](https://docs.google.com/spreadsheets/d/1jndGJ2qicN2WworS3Q_4FAjM_QOEUGlWiPPX4hkRoNE/edit?usp=sharing) 

## Understanding LSQ

* [Data Model](Data-Model) of LSQ for capturing SPARQL queries, their constituents and benchmark executions
* [Named Graph Streams](Named-Graph-Streams) explains the design choices for using named graphs instead of vanilla triples
* [Skolemization](Skolemization) explains the procedure employed by LSQ for crafting IRIs based on a model full of blank nodes.

### Licencing 

LSQ is available under [ Creative Commons CC0 License](https://creativecommons.org/publicdomain/zero/1.0/). 
Please cite us Saleem, Muhammad, Muhammad Intizar Ali, Aidan Hogan, Qaiser Mehmood, and Axel-Cyrille Ngonga Ngomo. "LSQ: the linked SPARQL queries dataset." In International semantic web conference, pp. 261-269, 2015. 

### LSQ Team

<ul>
<li>
<a href="https://sites.google.com/site/saleemsweb/">Muhammad Saleem</a> (Maintainer) </li>
<li><a href="http://www.intizarali.org">Intizar Ali</a></li>
<li><a href="https://www.insight-centre.org/users/qaiser-mehmood">Qaiser Mehmood</a></li>
<li><a href="http://aidanhogan.com/">Aidan Hogan</a></li>
<li><a href="http://aksw.org/AxelNgonga.html">Axel-Cyrille Ngonga Ngomo</a></li>
</ul>

