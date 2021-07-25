### Pratical Queries For Working With LSQ


For reference, here is a depicition of the [LSQ data model](Data-Model)
![Depiction of the LSQ2 Data Model](https://github.com/AKSW/LSQ/blob/develop/lsq-docs/lsq2-datamodel.svg)


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