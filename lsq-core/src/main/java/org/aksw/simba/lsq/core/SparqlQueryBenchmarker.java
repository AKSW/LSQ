package org.aksw.simba.lsq.core;

import org.aksw.simba.lsq.model.QueryExec;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public interface SparqlQueryBenchmarker {
    void benchmark(SparqlQueryConnection conn, Query query, QueryExec result);
}
