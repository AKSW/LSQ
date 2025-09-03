package org.aksw.simba.lsq;

import static org.junit.Assert.assertNotEquals;

import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.query.Query;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.junit.Test;

public class TestLsqQueries {
//    @Test
//    public void test() {
//        DatasetGraph dsg = RDFDataMgr.loadDatasetGraph("logs/issue54.log.trig");
//        Query query = SparqlStmtMgr.loadQuery("get-query-log.rq");
//        Table table = QueryExec.dataset(dsg).query(query).table();
//        assertNotEquals(table.size(), 0);
//    }
}
