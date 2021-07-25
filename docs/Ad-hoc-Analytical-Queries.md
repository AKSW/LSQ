
* Number of query executions observed for number of unique queries

"The observation of 10 query executions has been made for 5 unique queries"

Plain SPARQL
```sparql
# query.rq
SELECT ?qec (SUM(?qc_contrib) AS ?qc) {
 { SELECT (COUNT(?qe) AS ?qec) (1 as ?qc_contrib) {
   GRAPH ?g { ?s <http://lsq.aksw.org/vocab#hasRemoteExec> ?qe }
 } GROUP BY ?g }
}
GROUP BY ?qec ORDER BY ?qec
```


With [SANSA](https://github.com/SANSA-Stack/SANSA-Stack) (local) cli tool: blazing fast but somewhat experimental (local means without spark-submit)
```bash
sansa trig -o tsv --rq query.rq dataset.trig
```

```sparql
# query.rq
SELECT ?qec (SUM(?qc_contrib) AS ?qc) {
  SERVICE <rdd:perPartition> {
   { SELECT (COUNT(?qe) AS ?qec) (1 as ?qc_contrib) {
     GRAPH ?g { ?s <http://lsq.aksw.org/vocab#hasRemoteExec> ?qe }
   } GROUP BY ?g }
  }
}
GROUP BY ?qec ORDER BY ?qec
```


With [rdf processing toolkit](https://github.com/SmartDataAnalytics/RdfProcessingToolkit)
Not suitable for large data but faster for working on small samples (e.g. `ngs head -n 1000`) where results can be computed in a few seconds such that the spark overhead is higher

```bash
ngs head -n 1000 genage-lsq2.trig.bz2 |\
ngs map -s 'SELECT (COUNT(?qe) AS ?qec) (1 as ?qc_contrib) { ?s <http://lsq.aksw.org/vocab#hasRemoteExec> ?qe }' |\
sbs map -o tsv -s 'SELECT ?qec (SUM(?qc_contrib) AS ?qc) {} GROUP BY ?qec ORDER BY ?qec'
```

 
* User agent to number of queries
```bash
ngs head -n 1000 bench-genage-lsq2.trig.bz2 |\
ngs map -s 'PREFIX lsq: <http://lsq.aksw.org/vocab#> SELECT ?agent (1 AS ?qc_contrib) { ?s lsq:hasRemoteExec ?re . OPTIONAL { ?re lsq:headers [ <http://example.org/header#User-agent> ?agent ] } }' |\
sbs map -o tsv -s 'SELECT ?agent (SUM(?qc_contrib) AS ?qc) {} GROUP BY ?agent ORDER BY ?qec'
```
