package org.aksw.simba.lsq.enricher.benchmark.core;

import org.aksw.jenax.arq.util.exec.query.QueryExecutionUtils;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

public class QueryExecUtils {
    public static long fetchDatasetSize(QueryExecutionFactoryQuery qef) {
        Query countQuery = QueryFactory.create("SELECT (COUNT(*) AS ?c) { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }");
        return QueryExecutionUtils.fetchNumber(qef::createQueryExecution, countQuery, "c").longValue();
    }
}
