package org.aksw.simba.lsq;

import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;

import org.aksw.commons.util.exception.ExceptionUtilsAksw;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;
import org.junit.Test;


public class TestConnectionLoss {
    /** The use of a concrete localhost URL is not ideal for testing;
     *  but somehow we need to test this case */
    public static final String connectionRefusedUrl = "http://localhost:1231/sparql";
    public static final String unknownHostUrl = "http://unknown-host.foobarx/sparql";

    /** Connecting to a close port on localhost should raise an exception */
    @Test(expected=QueryException.class)
    public void testConnectionRefusedSimple() {
        Query query = QueryFactory.create("SELECT * { ?s a ?t } LIMIT 1");
        try (QueryExecution qe = QueryExecutionHTTPBuilder.create().endpoint(connectionRefusedUrl).query(query).build()) {
            ResultSetFormatter.consume(qe.execSelect());
        }
    }

    /** The catch block should catch the connection refused exception */
    @Test
    public void testConnectionRefusedDetailed() {
        Query query = QueryFactory.create("SELECT * { ?s a ?t } LIMIT 1");
        try (QueryExecution qe = QueryExecutionHTTPBuilder.create().endpoint(connectionRefusedUrl).query(query).build()) {
            ResultSetFormatter.consume(qe.execSelect());
        } catch (Exception e) {
            ExceptionUtilsAksw.rethrowUnless(e, ExceptionUtilsAksw::isConnectionRefusedException);
        }
    }

    @Test(expected=QueryException.class)
    public void testUnknownHostSimple() {
        Query query = QueryFactory.create("SELECT * { ?s a ?t } LIMIT 1");
        try (QueryExecution qe = QueryExecutionHTTPBuilder.create().endpoint(connectionRefusedUrl).query(query).build()) {
            ResultSetFormatter.consume(qe.execSelect());
        }
    }

    /** The catch block should catch the connection refused exception */
    @Test
    public void testUnknownHostDetailed() {
        Query query = QueryFactory.create("SELECT * { ?s a ?t } LIMIT 1");
        try (QueryExecution qe = QueryExecutionHTTPBuilder.create().endpoint(unknownHostUrl).query(query).build()) {
            ResultSetFormatter.consume(qe.execSelect());
        } catch (Exception e) {
            ExceptionUtilsAksw.rethrowUnless(e,
                    ExceptionUtilsAksw.isRootCauseInstanceOf(UnknownHostException.class),
                    ExceptionUtilsAksw.isRootCauseInstanceOf(UnresolvedAddressException.class));
        }
    }

}
