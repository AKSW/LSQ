---
title: LSQ
nav_order: 1
---

# LSQ
LSQ is a framework for extracting, analyzing and benchmarking SPARQL queries. A set of Linked Datasets describing SPARQL queries extracted from the logs of a variety of prominent public SPARQL endpoints is made available. We argue that LSQ datasets have a variety of uses for the SPARQL research community, be it, for example, to generate benchmarks on-the-fly by selecting real-world queries with specific characteristics that we describe, or to conduct analysis of what SPARQL (1.1) query features are most often used to interrogate endpoints, or to characterize the behavior of the different types of agents that are using these endpoints, or to find out what queries agents are asking about a given resource, etc. 
  
Sources of queries are typically logs of triple stores or web servers in general.
Analysis involves extracting the basic graph patterns, used features such as functions and operators, and statistics of these features.
Benchmarking evaluates queries by executing them against a given SPARQL endpoint thereby measuring performance and result set characteristics.

## Installation of LSQ Framework

* [Setup](v2/setup.md) of the LSQ Framework

## Using the LSQ Command Line Tool

* [Probing Logs](v2/usage/probing-logs.md) for their format
* [RDFization](v2/usage/rdfization.md) of query logs
* [Benchmarking](v2/usage/benchmarking.md) documents how to create and run benchmarks

## LSQ datasets and Public SPARQL endpoint. 

* The RDF dumps of the LSQ v2.0 datasets are available [here](https://hobbitdata.informatik.uni-leipzig.de/lsqv2/dumps/).
* A public SPARQL endpoint for the complete LSQ v2.0 dataset can be queried at [https://lsq.aksw.org/sparql](https://lsq.aksw.org/sparql), where each dataset (log) is loaded into a separate named graph (see useful queries below). 
* The original datasets were uploaded to Virtuoso 7.2, with instances available from [here](https://hobbitdata.informatik.uni-leipzig.de/lsqv2/endpoints/lsq-endpoints-v2/).
* A Virtuoso triplestore for the complete LSQ v2.0 is available from [here](https://hobbitdata.informatik.uni-leipzig.de/lsqv2/endpoints/). 
* LSQ v1.0 documentation is available from [here](v1/index.html).

## Useful Queries over LSQ datasets

In order to see the named graphs referring to different logs in the [LSQ v2.0 endpoint](https://lsq.aksw.org/sparql) you can use:

````
SELECT DISTINCT ?g WHERE {GRAPH ?g {?s ?p ?o}}
````

In order to find raw query texts from, e.g., the DBpedia log, you can use:

````
PREFIX lsqv: <http://lsq.aksw.org/vocab#> 
SELECT DISTINCT ?o
WHERE { 
   GRAPH <http://lsq.aksw.org/dbpedia> { ?s lsqv:text ?o }
}
````

In order to find (for example) queries in all logs that use `OPTIONAL` and have at least two triple triple patterns, you can use:

````
PREFIX lsqv: <http://lsq.aksw.org/vocab#> 
PREFIX sd: <http://www.w3.org/ns/sparql-service-description#> 
SELECT DISTINCT ?text ?endpoint ?tpCount 
WHERE { 
   ?query lsqv:hasRemoteExec/lsqv:endpoint ?endpoint . 
   ?query lsqv:text ?text . 
   ?query lsqv:hasStructuralFeatures ?features . 
   ?features lsqv:usesFeature lsqv:Optional . 
   ?features lsqv:tpCount ?tpCount . 
   FILTER(?tpCount > 2) 
} LIMIT 10
````

Other useful queries which can be executed over LSQ datasets in order to collect statistics are available [here](https://docs.google.com/spreadsheets/d/1jndGJ2qicN2WworS3Q_4FAjM_QOEUGlWiPPX4hkRoNE/edit?usp=sharing) 

## Understanding LSQ

* [Data Model](v2/concepts/data-model.md) of LSQ for capturing SPARQL queries, their constituents and benchmark executions
* [Named Graph Streams](v2/concepts/named-graph-streams.md) explains the design choices for using named graphs instead of vanilla triples
* [Skolemization](v2/concepts/skolemization.md) explains the procedure employed by LSQ for crafting IRIs based on a model full of blank nodes.

### Licencing 

LSQ is available under [Creative Commons CC0 License](https://creativecommons.org/publicdomain/zero/1.0/). 

If you use this dataset, please cite us 

Muhammad Saleem, Muhammad Intizar Ali, Aidan Hogan, Qaiser Mehmood, and Axel-Cyrille Ngonga Ngomo. "[LSQ: the Linked SPARQL Queries Dataset.](https://aidanhogan.com/docs/LSQ_ISWC2015.pdf)" In International Semantic Web Conference, pp. 261-269, Springer, 2015.

````
@inproceedings{SaleemAHMN15,
  author    = {Muhammad Saleem and
               Muhammad Intizar Ali and
               Aidan Hogan and
               Qaiser Mehmood and
               Axel{-}Cyrille Ngonga Ngomo},
  title     = {LSQ: The Linked SPARQL Queries Dataset},
  booktitle = {International Semantic Web Conference (ISWC)},
  series    = {LNCS},
  volume    = {9367},
  pages     = {261--269},
  publisher = {Springer},
  year      = {2015},
  url       = {https://doi.org/10.1007/978-3-319-25010-6\_15},
  doi       = {10.1007/978-3-319-25010-6\_15}
}
````

### LSQ Team

<ul>
<li>
<a href="https://sites.google.com/site/saleemsweb/">Muhammad Saleem</a> </li>
<li><a href="https://aksw.org/ClausStadler.html">Claus Stadler</a></li>
<li><a href="https://www.insight-centre.org/users/qaiser-mehmood">Qaiser Mehmood</a></li>
<li><a href="http://aidanhogan.com/">Aidan Hogan</a></li>
<li><a href="https://www.carlosbuil.com/">Carlos Buil-Aranda</a></li>
<li><a href="http://aksw.org/AxelNgonga.html">Axel-Cyrille Ngonga Ngomo</a></li>
</ul>

