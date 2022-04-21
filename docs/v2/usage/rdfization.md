---
parent: Usage
title: RDFization
nav_order: 30
---


Topics
* [RDFization](#rdfization)
* [Fine Tuning](#fine-tuning)
* [Controlling Sorting](#controlling-sorting)

# RDFization
RDFization of query logs with LSQ yields **query-centric** semantic records.
Query-centric means, that every mention of a query is linked to information such as the request.
Log files are usually 'request-centric', i.e. each log record (typically stored in a line) corresponds to a certain request.

For each IRI of a query there are named graphs with the same IRI that group all triples related to a query together.
This approach allows for easy merging of all named graphs with the same IRI in order to provide more comprehensive information about a query.
The ID assignment (skolemization) procedure of LSQ is aimed at yielding consistent local IDs (based on hashing appropriate parts of the RDF graph) from which global IRIs are derived.

The RDFization procedure turns every log record into a *named graph* with query-centric representation thereof.
**LSQ by default sorts the named graphs** such that all quads of log records related to the same query are consecutive in the output.

> :warning: **Large log files will take long to sort. No output will be written unless sorting completes. Use `--no-merge` to prevent sorting and get immediate output**

The identity of a request record is determined by two attributes: The **timestamp** at which the request was made and the **URL to the service** it was made. The service URL is usually not part of the log files and must therefore be provided as input to the RDFization procedure.

Note that service URL and timestamp indirectly refer to the the dataset the requests made made to. In practice, concrete identifiers for the exact set of triples that make up an RDF dataset are not (yet) commonly used. However, our chosen approach enables one to easily perform a post processing step to infer dataset identifiers where appropriate.

> :warning: **For privacy, host names and IPs will be hashed with new randomly generated salt on each invocation of LSQ. Use `--no-hash` to disable or `--salt` to provide a specific salt**


## Data Example
This example demonstrates simple RDFization of a log file. As mentioned in the previous section, the service URL and the timestamp (from the log entries) are used to craft IRIs which indirectly describe the dataset the requests were executed on.


For example, give a file `example.log` with content
```
x.x.x.x - - [16/May/2014:00:30:13 +0100] "GET /sparql?query=DESCRIBE+%3Chttp%3A%2F%2Fdata.semanticweb.org%2Fconference%2Fekaw%2F2012%2Fpaper%2Fdemos%2F8%3E HTTP/1.0" 200 3456 "-" "-"
```

```bash
➜  lsq rdfize --endpoint http://www.example.org/sparql example.log
```

```
<http://lsq.aksw.org/q-bd9db20673d74c226f82e5bd26108fab2f2ccd3d97af4fac0cf694420a32dea6> {
    <http://lsq.aksw.org/re-www.example.org-sparql_2014-05-15T23:30:13Z>
            <http://lsq.aksw.org/vocab#endpoint>
                    <http://www.example.org/sparql> ;
            <http://lsq.aksw.org/vocab#headers>
                    [ <http://example.org/header#Referer>
                              "-" ;
                      <http://example.org/header#User-agent>
                              "-"
                    ] ;
            <http://lsq.aksw.org/vocab#hostHash>
                    "2f17cd84e3776f903d6b2b6192e7987d98cf7e6a9ebc9b4e73d230383ed5588a" ;
            <http://lsq.aksw.org/vocab#logRecord>
                    "x.x.x.x - - [16/May/2014:00:30:13 +0100] \"GET /sparql?query=DESCRIBE+%3Chttp%3A%2F%2Fdata.semanticweb.org%2Fconference%2Fekaw%2F2012%2Fpaper%2Fdemos%2F8%3E HTTP/1.0\" 200 3456 \"-\" \"-\"" ;
            <http://lsq.aksw.org/vocab#numResponseBytes>
                    "3456"^^<http://www.w3.org/2001/XMLSchema#int> ;
            <http://lsq.aksw.org/vocab#protocol>
                    "HTTP/1.0" ;
            <http://lsq.aksw.org/vocab#query>
                    "DESCRIBE <http://data.semanticweb.org/conference/ekaw/2012/paper/demos/8>" ;
            <http://lsq.aksw.org/vocab#sequenceId>
                    "4"^^<http://www.w3.org/2001/XMLSchema#long> ;
            <http://lsq.aksw.org/vocab#statusCode>
                    "200"^^<http://www.w3.org/2001/XMLSchema#int> ;
            <http://lsq.aksw.org/vocab#uri>
                    "/sparql?query=DESCRIBE+%3Chttp%3A%2F%2Fdata.semanticweb.org%2Fconference%2Fekaw%2F2012%2Fpaper%2Fdemos%2F8%3E" ;
            <http://lsq.aksw.org/vocab#user>
                    "-" ;
            <http://lsq.aksw.org/vocab#verb>
                    "GET" ;
            <http://www.w3.org/ns/prov#atTime>
                    "2014-05-15T23:30:13Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> .
    
    <http://lsq.aksw.org/q-bd9db20673d74c226f82e5bd26108fab2f2ccd3d97af4fac0cf694420a32dea6>
            <http://lsq.aksw.org/vocab#hasRemoteExec>
                    <http://lsq.aksw.org/re-www.example.org-sparql_2014-05-15T23:30:13Z> ;
            <http://lsq.aksw.org/vocab#hash>
                    "bd9db20673d74c226f82e5bd26108fab2f2ccd3d97af4fac0cf694420a32dea6" ;
            <http://lsq.aksw.org/vocab#text>
                    "DESCRIBE <http://data.semanticweb.org/conference/ekaw/2012/paper/demos/8>\n" .
}

```

> :wrench: **This quad-based output (trig format in this example) allows you to easy build clean bash pipes using our ngs tool:**

```bash
# Restrict lsq output to only the first 5 named graphs using ngs
➜  lsq rdfize --endpoint http://www.example.org/sparql example.log | ngs head -n 5
```


## Fine-Tuning

* Typically a full RDFization of the input log records is not needed.
  The `--slim` reduces the amount of information for log records. The (relative) request URI is retained for future reference but fields like the HTTP verb (POST/GET) are discarded.

```
➜  lsq rdfize --slim --endpoint http://www.example.org/sparql example.log
```

```
    <http://lsq.aksw.org/re-www.example.org-sparql_2014-05-15T23:30:13Z>
            <http://lsq.aksw.org/vocab#endpoint>
                    <http://www.example.org/sparql> ;
            <http://lsq.aksw.org/vocab#hostHash>
                    "2f17cd84e3776f903d6b2b6192e7987d98cf7e6a9ebc9b4e73d230383ed5588a" ;
            <http://lsq.aksw.org/vocab#uri>
                    "/sparql?query=DESCRIBE+%3Chttp%3A%2F%2Fdata.semanticweb.org%2Fconference%2Fekaw%2F2012%2Fpaper%2Fdemos%2F8%3E" ;
            <http://www.w3.org/ns/prov#atTime>
                    "2014-05-15T23:30:13Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> .

    <http://lsq.aksw.org/q-bd9db20673d74c226f82e5bd26108fab2f2ccd3d97af4fac0cf694420a32dea6>
            # ... same as above
```

## Controlling Sorting
For sorting LSQ relies on the "named graph stream" code from the [Sparql-Integerate](https://github.com/QROWD/SparqlIntegrate) project.
It features a sort operator that delegates to the UNIX `/usr/bin/sort` command.
`lsq rdfize` provides the following options:
```
-S, --buffer-size=<bufferSize> e.g. 4G or 1024M
-T, --temporary-directory=<dir> defaults to /tmp
```

For more features we recommend to install the sparql-integrate which features the ngs (named graph stream) command.



### Sorting with NGS
A previously RDFized LSQ query log created using --no-merge can be sorted and merged anytime using the ngs tool:
```
ngs sort --merge non-sorted-rdfized-lsq-log.trig
```

Merge means that two separate sets of consecutive quads (appearing anywhere in the file) with the same name will be merged into a single set after sorting caused those sets to be become consecutive.

