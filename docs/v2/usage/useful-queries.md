---
parent: Usage
title: Useful Queries
nav_order: 6
---


### Pratical Queries For Working With LSQ


For reference, here is a depicition of the [LSQ data model](Data-Model)
![Depiction of the LSQ2 Data Model](https://github.com/AKSW/LSQ/raw/develop/docs/v2/images/lsq2-datamodel.png)

```sparql
# Get all SELECT queries from DBpedia log 
SELECT DISTINCT ?text from <http://lsq.aksw.org/dbpedia> WHERE
{
?s <http://lsq.aksw.org/vocab#text> ?text .
?s <http://lsq.aksw.org/vocab#hasSpin> ?spin .
?spin a <http://spinrdf.org/sp#Select> . 
}
```
```sparql
# Get all SELECT queries from Semantic Web Dog Food along with timestamps (the original execution time on the endpoint)
PREFIX lsqv: <http://lsq.aksw.org/vocab#> 
PREFIX prov: <http://www.w3.org/ns/prov#>
PREFIX sd: <http://www.w3.org/ns/sparql-service-description#>
SELECT Distinct ?text ?timeStamp From <http://lsq.aksw.org/swdf>
 WHERE 
{ 
?query lsqv:text ?text . 
?query lsqv:hasRemoteExec ?re .
?re prov:atTime ?timeStamp . 
?query lsqv:hasSpin ?spin .
?spin a <http://spinrdf.org/sp#Select> . 
} 
```

```sparql
# Get all queries having resultset greater than zero. 
SELECT DISTINCT ?text WHERE 
{
?s <http://lsq.aksw.org/vocab#text> ?text .
?s <http://lsq.aksw.org/vocab#hasRemoteExec> ?re . 
?s <http://lsq.aksw.org/vocab#hasLocalExec> ?le . 
?le <http://lsq.aksw.org/vocab#hasQueryExec> ?qe . 
?qe <http://lsq.aksw.org/vocab#resultCount> ?rs  . 
FILTER(?rs > 0)
}
```

```sparql
# Get all star shaped queries (There are four types of nodes in LSQ namely Star, Path, Hybrid, Sink). 
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
# Queries along with their resultset sizes and runtimes
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
# Queries with number of triple patterns
SELECT ?text ?tp 
WHERE { ?s <http://lsq.aksw.org/vocab#text> ?text .
?s <http://lsq.aksw.org/vocab#hasStructuralFeatures> ?sf .
?sf <http://lsq.aksw.org/vocab#tpCount> ?tp. 
}
```

```sparql
# Queries and their number of join vertices. 
SELECT ?text ?jv
WHERE
{
?s <http://lsq.aksw.org/vocab#text> ?text .
?s <http://lsq.aksw.org/vocab#hasStructuralFeatures> ?sf .
?sf <http://lsq.aksw.org/vocab#joinVertexCount> ?jv. 
}
```

```sparql
# Total queries having Filters 
SELECT (count(DISTINCT ?text) as ?totalFilter)
WHERE { 
?s <http://lsq.aksw.org/vocab#text> ?text . 
?s <http://lsq.aksw.org/vocab#hasStructuralFeatures> ?sf .
?sf <http://lsq.aksw.org/vocab#usesFeature> <http://lsq.aksw.org/vocab#Filter> . 
}
```

```sparql
# Total queries having Solution modifiers
SELECT (count(DISTINCT ?text) as ?totalMod) 
WHERE {
?s <http://lsq.aksw.org/vocab#text> ?text .
?s <http://lsq.aksw.org/vocab#hasStructuralFeatures> ?sf . 
?sf <http://lsq.aksw.org/vocab#usesFeature> ?uf .
FILTER(?uf = <http://lsq.aksw.org/vocab#OrderBy> || ?uf = <http://lsq.aksw.org/vocab#Offset> || ?uf = <http://lsq.aksw.org/vocab#Limit>) 
}
```

```sparql
# Total queries having SPARQL functions 
SELECT (count(DISTINCT ?text) as ?totalFunc)
WHERE {
?s <http://lsq.aksw.org/vocab#text> ?text . 
?s <http://lsq.aksw.org/vocab#hasStructuralFeatures> ?sf . 
?sf <http://lsq.aksw.org/vocab#usesFeature>  <http://lsq.aksw.org/vocab#Functions> 
}
```

```sparql
# Count of property paths
PREFIX lsqv: <http://lsq.aksw.org/vocab#> 
SELECT ?o (COUNT(*) AS ?ppathCount) 
{
?s lsqv:usesFeature ?o . 
FILTER(?o IN (lsqv:LinkPath, lsqv:ReverseLinkPath, lsqv:NegPropSetPath , lsqv:InversePath, lsqv:ModPath, lsqv:FixedLengthPath, lsqv:DistinctPath, lsqv:MultiPath, lsqv:ShortestPath, lsqv:ZeroOrOnePath)) } GROUP BY ?o ORDER BY DESC(COUNT(*)
)
```

```sparql
# Top objects ordered by query count	
SELECT ?object COUNT(Distinct ?query) as ?queryCount
{
?s <http://lsq.aksw.org/vocab#text> ?query .
?s <http://lsq.aksw.org/vocab#hasStructuralFeatures> ?sf .
?sf <http://lsq.aksw.org/vocab#hasTP> ?tp .
?tp <http://spinrdf.org/sp#object> ?object
}
GROUP BY ?object
ORDER BY DESC(?queryCount)
```

```sparql
# Various features of SPARQL queries
PREFIX lsqv: <http://lsq.aksw.org/vocab#>
SELECT  DISTINCT  ?qId  ?joinVertices ?tps ?rs ?rt ?meanJoinVertexDegree 
{
?qId  lsqv:text ?text .
?qId  lsqv:hasRemoteExec ?re . 
?qId  lsqv:hasLocalExec ?le . 
?qId  lsqv:hasStructuralFeatures ?sf .
?sf   lsqv:projectVarCount ?projVars.
?sf   lsqv:joinVertexCount ?joinVertices . 
?sf   lsqv:tpCount ?tps .
?sf   lsqv:joinVertexDegreeMean ?meanJoinVertexDegree . 
?sf   lsqv:usesFeature  lsq:Select  .  
?le   lsqv:hasQueryExec ?qe . 
?qe   lsqv:resultCount ?rs. 
?qe   lsqv:evalDuration ?rt. 
FILTER (?rs > 0 && ?rs < 20000000 && ?tps > 0)
}
LIMIT 1000000
```

```sparql
# find queries with dbo:Actor as an object in a triple pattern
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX lsqv: <http://lsq.aksw.org/vocab#>
PREFIX sp: <http://spinrdf.org/sp#>

SELECT DISTINCT ?text ?query
WHERE { 
  ?query lsqv:text ?text .
  ?query lsqv:hasStructuralFeatures/lsqv:hasBgp/lsqv:hasTpInBgp/lsqv:hasTp/sp:object dbo:Actor .
}
```

```sparql
# find queries with the actor keyword
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX lsqv: <http://lsq.aksw.org/vocab#>
PREFIX sp: <http://spinrdf.org/sp#>

SELECT DISTINCT ?text ?query
WHERE { 
  ?query lsqv:text ?text .
  ?text bif:contains "actor" .
}
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
PREFIX lsqv: <http://lsq.aksw.org/vocab#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

SELECT ?bgpLabel ?bgpNodeLabel ?subBgpLabel ?subTpLabel {
  ?query lsq:hasStructuralFeatures ?sf .

  ?sf         lsqv:hasBgp     ?bgp .
  ?bgp        lsqv:hasBgpNode ?bgpNode .
  ?bgpNode    lsqv:hasSubBgp  ?subBgp .
  ?subBgp     lsqv:hasTpInBgp ?subTpInBgp .
  ?subTpInBgp lsqv:hasTp      ?subTp .

  ?bgp     rdfs:label ?bgpLabel .
  ?bgpNode rdfs:label ?bgpNodeLabel .
  ?subBgp  rdfs:label ?subBgpLabel .
  ?subTp   rdfs:label ?subTpLabel .

} ORDER BY ?bgpLabel ?bgpNodeLabel ?subBgpLabel ?subTpLabel
```

#### Accessing the RDF terms and variables of a query's triple patterns (via the BGPs)
```sparql
PREFIX lsqv: <http://lsq.aksw.org/vocab#>
PREFIX sp: <http://spinrdf.org/sp#>

SELECT ?tpLabel ?s ?p ?o {
  { SELECT * { ?query lsqv:hasStructuralFeatures ?sf } LIMIT 1 }

  Graph ?g {
    ?sf         lsqv:hasBgp     ?bgp .
    ?bgp        lsqv:hasTpInBgp ?tpInBgp .
    ?tpInBgp    lsqv:hasTp      ?tp .

    ?bgp rdfs:label ?bgpLabel .
    ?tp  rdfs:label ?tpLabel .

    ?tp sp:subject ?s .
    ?tp sp:predicate ?p .
    ?tp sp:object ?o .
  }
}
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
PREFIX lsqv: <http://lsq.aksw.org/vocab#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

SELECT ?exp ?bgpLabel ?bgpNodeLabel ?subBgpLabel ?subTpLabel ?bgpSize  ?subTpSize ?subTpToBgpRatio {
  ?query          lsqv:hasLocalExec   ?localExec .
  ?localExec      lsqv:hasBgpExec     ?bgpExec .
  ?bgpExec        lsqv:hasJoinVarExec ?bgpNodeExec .
  ?bgpNodeExec    lsqv:hasSubBgpExec  ?subBgpExec .
  ?subBgpExec     lsqv:hasTpInBgpExec ?subTpInBgpExec .
  ?subTpInBgpExec lsqv:hasTpExec      ?subTpExec .

  # Links from the executions to the query's elements
  ?bgp     lsqv:hasExec ?bgpExec     ; rdfs:label ?bgpLabel .
  ?bgpNode lsqv:hasExec ?bgpNodeExec ; rdfs:label ?bgpNodeLabel .
  ?subBgp  lsqv:hasExec ?subBgpExec  ; rdfs:label ?subBgpLabel .
  ?subTp   lsqv:hasExec ?subTpExec   ; rdfs:label ?subTpLabel .

  ?localExec lsqv:benchmarkRun ?exp .  

  # Get the measurements
  ?subBgpExec lsqv:hasElementExec [ lsqv:resultCount ?bgpSize   ] .
  ?subTpExec  lsqv:hasElementExec [ lsqv:resultCount ?subTpSize ] .

  ?subTpInBgpExec lsqv:tpToBgpRatio ?subTpToBgpRatio .

  # Further useful triple patterns
  # ?query lsqv:hash ?queryHash .
  # ?exp dct:identifier ?expId .

  # bgp/tp selectivities (may be absent if involved result sets exceeded benchmark limits)
  # OPTIONAL { ?subTpInBgpExec lsqv:bgpRestrictedTpSel ?bgpRestrictedTpSel }

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
