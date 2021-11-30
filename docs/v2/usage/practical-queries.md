---
parent: Usage
title: Useful Queries
nav_order: 6
---


### Pratical Queries For Working With LSQ


For reference, here is a depicition of the [LSQ data model](Data-Model)
![Depiction of the LSQ2 Data Model](https://github.com/AKSW/LSQ/blob/develop/docs/v2/images/lsq2-datamodel.png)

```sparql
Get all SELECT queries from DBpedia log 
SELECT DISTINCT ?text from <http://lsq.aksw.org/dbpedia> WHERE
{
?s <http://lsq.aksw.org/vocab#text> ?text .
?s <http://lsq.aksw.org/vocab#hasSpin> ?spin .
?spin a <http://spinrdf.org/sp#Select> . 
}

```

```sparql
Get all SELECT queries from DBpedia log 
SELECT DISTINCT ?text from <http://lsq.aksw.org/dbpedia> WHERE
{
?s <http://lsq.aksw.org/vocab#text> ?text .
?s <http://lsq.aksw.org/vocab#hasSpin> ?spin .
?spin a <http://spinrdf.org/sp#Select> . 
}

```

```sparql
Get all queries having resultset zero. 
SELECT DISTINCT ?text WHERE 
{
?s <http://lsq.aksw.org/vocab#text> ?text .
?s <http://lsq.aksw.org/vocab#hasRemoteExec> ?re . 
?s <http://lsq.aksw.org/vocab#hasLocalExec> ?le . 
?le <http://lsq.aksw.org/vocab#hasQueryExec> ?qe . 
?qe <http://lsq.aksw.org/vocab#resultCount> ?rs  . 
FILTER(?rs = 0)}

```

```sparql
Get all star shaped queries (There are four types of nodes in LSQ namely Star, Path, Hybrid, Sink). 
SELECT DISTINCT ?text
WHERE 
{ 
?s <http://lsq.aksw.org/vocab#text> ?text . 
?s <http://lsq.aksw.org/vocab#hasStructuralFeatures> ?sf . 
?sf <http://lsq.aksw.org/vocab#hasBgp> ?bgp .
?bgp <http://lsq.aksw.org/vocab#hasBgpNode> ?jv .
?jv <http://lsq.aksw.org/vocab#joinVertexType> <http://lsq.aksw.org/vocab#star>. 
}
```
```sparql
Queries along with their resultset sizes and runtimes
SELECT ?text ?rs ?sec
WHERE { 
?s <http://lsq.aksw.org/vocab#text> ?text . 
?s <http://lsq.aksw.org/vocab#hasLocalExec> ?le .
?s <http://lsq.aksw.org/vocab#hasRemoteExec> ?re .
?le <http://lsq.aksw.org/vocab#hasQueryExec> ?qe .
?qe <http://lsq.aksw.org/vocab#resultCount> ?rs. 
?qe <http://lsq.aksw.org/vocab#evalDuration> ?sec 
}


```
```sparql
Queries with number of triple patterns
SELECT ?text ?tp 
WHERE { ?s <http://lsq.aksw.org/vocab#text> ?text .
?s <http://lsq.aksw.org/vocab#hasStructuralFeatures> ?sf .
?sf <http://lsq.aksw.org/vocab#tpCount> ?tp. }
```

```sparql
Queries and their number of join vertices. 
SELECT ?text ?jv
WHERE
{
?s <http://lsq.aksw.org/vocab#text> ?text .
?s <http://lsq.aksw.org/vocab#hasStructuralFeatures> ?sf .
?sf <http://lsq.aksw.org/vocab#joinVertexCount> ?jv. 
}
```
```sparql
Get all queries having Filters 

SELECT (count(DISTINCT ?text) as ?totalFilter)
WHERE { 
?s <http://lsq.aksw.org/vocab#text> ?text . 
?s <http://lsq.aksw.org/vocab#hasStructuralFeatures> ?sf .
?sf <http://lsq.aksw.org/vocab#usesFeature> <http://lsq.aksw.org/vocab#Filter> . 
}
```
```sparql
Total Solution modifier

SELECT
(count(DISTINCT ?text) as ?totalMod) 
WHERE {
?s <http://lsq.aksw.org/vocab#text> ?text .
?s <http://lsq.aksw.org/vocab#hasStructuralFeatures> ?sf . 
?sf <http://lsq.aksw.org/vocab#usesFeature> ?uf .
FILTER(?uf = <http://lsq.aksw.org/vocab#OrderBy> || ?uf = <http://lsq.aksw.org/vocab#Offset> || ?uf = <http://lsq.aksw.org/vocab#Limit>) }
```
```sparql
Total queries having SPARQL functions used. 

SELECT (count(DISTINCT ?text) as ?totalFunc)
WHERE {
?s <http://lsq.aksw.org/vocab#text> ?text . 
?s <http://lsq.aksw.org/vocab#hasStructuralFeatures> ?sf . 
?sf <http://lsq.aksw.org/vocab#usesFeature>  <http://lsq.aksw.org/vocab#Functions> 
}
```
```sparql
Count of property paths

PREFIX lsq: <http://lsq.aksw.org/vocab#> SELECT ?o (COUNT(*) AS ?ppathCount) 
{
?s lsq:usesFeature ?o . 
FILTER(?o IN (lsq:LinkPath, lsq:ReverseLinkPath, lsq:NegPropSetPath , lsq:InversePath, lsq:ModPath, lsq:FixedLengthPath, lsq:DistinctPath, lsq:MultiPath, lsq:ShortestPath, lsq:ZeroOrOnePath)) } GROUP BY ?o ORDER BY DESC(COUNT(*)
)
```
```sparql
Top objects ordered by query count	SELECT ?object COUNT(Distinct ?query) as ?queryCount
	{
	?s <http://lsq.aksw.org/vocab#text> ?query .
	?s <http://lsq.aksw.org/vocab#hasStructuralFeatures> ?sf .
	?sf <http://lsq.aksw.org/vocab#hasTP> ?tp .
	?tp <http://spinrdf.org/sp#object> ?object
	}
	GROUP BY ?object
	ORDER BY DESC(?queryCount)
```

The following result sets of SPARQL queries are based on the LSQ output of this query:
```sparql
PREFIX swc:  <http://data.semanticweb.org/ns/swc/ontology#>
SELECT * {
  ?obj a swc:SessionEvent ;
  ?prop  ?target
}
```

### Querying The Static Structure


```sparql
PREFIX lsq: <http://lsq.aksw.org/vocab#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

SELECT ?bgpLabel ?bgpNodeLabel ?subBgpLabel ?subTpLabel {
  ?query lsq:hasStructuralFeatures ?sf .

  ?sf         lsq:hasBgp     ?bgp .
  ?bgp        lsq:hasBgpNode ?bgpNode .
  ?bgpNode    lsq:hasSubBgp  ?subBgp .
  ?subBgp     lsq:hasTpInBgp ?subTpInBgp .
  ?subTpInBgp lsq:hasTp      ?subTp .

  ?bgp     rdfs:label ?bgpLabel .
  ?bgpNode rdfs:label ?bgpNodeLabel .
  ?subBgp  rdfs:label ?subBgpLabel .
  ?subTp   rdfs:label ?subTpLabel .

} ORDER BY ?bgpLabel ?bgpNodeLabel ?subBgpLabel ?subTpLabel

```

```
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
| bgpLabel                                                                                          | bgpNodeLabel | subBgpLabel                                                                                       | subTpLabel                                                                                                          |
==============================================================================================================================================================================================================================================================================================================================================
| "?obj  a      <http://data.semanticweb.org/ns/swc/ontology#SessionEvent> ;\n      ?prop  ?target" | "?obj"       | "?obj  a      <http://data.semanticweb.org/ns/swc/ontology#SessionEvent> ;\n      ?prop  ?target" | "?obj <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://data.semanticweb.org/ns/swc/ontology#SessionEvent>" |
| "?obj  a      <http://data.semanticweb.org/ns/swc/ontology#SessionEvent> ;\n      ?prop  ?target" | "?obj"       | "?obj  a      <http://data.semanticweb.org/ns/swc/ontology#SessionEvent> ;\n      ?prop  ?target" | "?obj ?prop ?target"                                                                                                |
| "?obj  a      <http://data.semanticweb.org/ns/swc/ontology#SessionEvent> ;\n      ?prop  ?target" | "?prop"      | "?obj  ?prop  ?target"                                                                            | "?obj ?prop ?target"                                                                                                |
| "?obj  a      <http://data.semanticweb.org/ns/swc/ontology#SessionEvent> ;\n      ?prop  ?target" | "?target"    | "?obj  ?prop  ?target"                                                                            | "?obj ?prop ?target"                                                                                                |
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

```


### Querying Over Executions
The following elements of SPARQL queries are evaluated in benchmark runs which in essence leads to statistical observations w.r.t. those elements.
The affected elements are: bgp, tpInBgp, tp and bgpNode.

The query is admittedly quite large but the essence is that from a given execution of a query (via lsq:hasLocalExec) all
corresponding executions of that query's constituent elements are unambiguously accessible.
Each execution is linked to the appropriate element via lsq:hasExec in reverse direction.
Note, that the rationale for the reverse link is that from an element all corresponding executions are reachable via forward links which is aimed at providing nicer Linked Data views.


```sparql
PREFIX lsq: <http://lsq.aksw.org/vocab#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

SELECT ?exp ?bgpLabel ?bgpNodeLabel ?subBgpLabel ?subTpLabel ?bgpSize  ?subTpSize ?subTpToBgpRatio {
  ?query          lsq:hasLocalExec   ?localExec .
  ?localExec      lsq:hasBgpExec     ?bgpExec .
  ?bgpExec        lsq:hasJoinVarExec ?bgpNodeExec .
  ?bgpNodeExec    lsq:hasSubBgpExec  ?subBgpExec .
  ?subBgpExec     lsq:hasTpInBgpExec ?subTpInBgpExec .
  ?subTpInBgpExec lsq:hasTpExec      ?subTpExec .

  # Links from the executions to the query's elements
  ?bgp     lsq:hasExec ?bgpExec     ; rdfs:label ?bgpLabel .
  ?bgpNode lsq:hasExec ?bgpNodeExec ; rdfs:label ?bgpNodeLabel .
  ?subBgp  lsq:hasExec ?subBgpExec  ; rdfs:label ?subBgpLabel .
  ?subTp   lsq:hasExec ?subTpExec   ; rdfs:label ?subTpLabel .

  ?localExec lsq:benchmarkRun ?exp .  

  # Get the measurements
  ?subBgpExec lsq:hasElementExec [ lsq:itemCount ?bgpSize   ] .
  ?subTpExec  lsq:hasElementExec [ lsq:itemCount ?subTpSize ] .

  ?subTpInBgpExec lsq:tpToBgpRatio ?subTpToBgpRatio .

  # Further useful triple patterns
  # ?query lsq:hash ?queryHash .
  # ?exp dct:identifier ?expId .

  # bgp/tp selectivities (may be absent if involved result sets exceeded benchmark limits)
  # OPTIONAL { ?subTpInBgpExec lsq:bgpRestrictedTpSel ?bgpRestrictedTpSel }

} ORDER BY ?exp ?bgpLabel ?bgpNodeLabel ?subBgpLabel
```

```
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
| exp                                                            | bgpLabel                                                | bgpNodeLabel | subBgpLabel                                             | subTpLabel                | bgpSize | subTpSize | subTpToBgpRatio |
=========================================================================================================================================================================================================================================================================
| lsqr:xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 | "?obj  a      swc:SessionEvent ;\n      ?prop  ?target" | "?obj"       | "?obj  a      swc:SessionEvent ;\n      ?prop  ?target" | "?obj a swc:SessionEvent" | 0       | 0         | 0               |
| lsqr:xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 | "?obj  a      swc:SessionEvent ;\n      ?prop  ?target" | "?obj"       | "?obj  a      swc:SessionEvent ;\n      ?prop  ?target" | "?obj ?prop ?target"      | 0       | 900       | 0               |
| lsqr:xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 | "?obj  a      swc:SessionEvent ;\n      ?prop  ?target" | "?prop"      | "?obj  ?prop  ?target"                                  | "?obj ?prop ?target"      | 900     | 900       | 1               |
| lsqr:xc-dbpedia.org-somedata_2020-07-25_at_05-08-2020_02:24:05 | "?obj  a      swc:SessionEvent ;\n      ?prop  ?target" | "?target"    | "?obj  ?prop  ?target"                                  | "?obj ?prop ?target"      | 900     | 900       | 1               |
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
```
