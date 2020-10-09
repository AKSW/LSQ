package org.aksw.simba.lsq;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import org.aksw.jena_sparql_api.core.connection.RDFConnectionBuilder;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionWithExecFails;
import org.aksw.simba.lsq.core.LsqBenchmarkProcessor;
import org.aksw.simba.lsq.model.ExperimentConfig;
import org.aksw.simba.lsq.model.ExperimentRun;
import org.aksw.simba.lsq.model.LsqQuery;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.ext.com.google.common.collect.ImmutableMap;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.junit.Test;

public class TestBenchmarkDbCache {

    public static ExperimentRun createTestRun(ExperimentConfig cfg) {
        ExperimentRun run = cfg.getModel().createResource().as(ExperimentRun.class);
        run.setConfig(cfg).setIdentifier("test-run").setTimestamp(new XSDDateTime(new GregorianCalendar()));

        return run;
    }

    public static ExperimentConfig createTestConfig(Model model) {

        ExperimentConfig cfg = model.createResource("http://lsq.aksw.org/testConfig").as(ExperimentConfig.class);
        Long qt = null;
        Long ct = null;

        cfg
            .setIdentifier("test")
            // .setCreationDate(nowCal)
            //.setDataRef(dataRef)
            .setExecutionTimeoutForRetrieval(qt == null ? new BigDecimal(300) : new BigDecimal(qt).divide(new BigDecimal(1000)))
            .setConnectionTimeoutForRetrieval(ct == null ? new BigDecimal(60) : new BigDecimal(ct).divide(new BigDecimal(1000)))
            .setMaxResultCountForRetrieval(0l) // 1M
            .setMaxByteSizeForRetrieval(0l) // limit only by count
            .setMaxResultCountForSerialization(0l) // limit by byte size
            .setMaxByteSizeForSerialization(0l) // 1MB
            .setExecutionTimeoutForCounting(qt == null ? new BigDecimal(300) : new BigDecimal(qt).divide(new BigDecimal(1000)))
            .setConnectionTimeoutForCounting(ct == null ? new BigDecimal(60) : new BigDecimal(ct).divide(new BigDecimal(1000)))
            .setMaxCount(1000000000l)
            .setMaxCountAffectsTp(false)
            .setUserAgent("lsq.aksw.org")
            .benchmarkSecondaryQueries(true)
            .setDatasetSize(1000l)
            .setDatasetLabel("testDataset")
            .setDatasetIri("http://lsq.aks.org/dataset/testDataset")
            .setBaseIri("http://lsq.aksw.org/")
            ;

        return cfg;
    }

    /**
     * Test whether queries are correctly cached in the database
     *
     */
    @Test
    public void testBenchmarkDbCache() {
        Model cfgModel = ModelFactory.createDefaultModel();
        ExperimentConfig cfg = createTestConfig(cfgModel);
        ExperimentRun run = createTestRun(cfg);

        try(RDFConnection indexConn = RDFConnectionBuilder.start().defaultDataset().getConnection()) {
            try(RDFConnection tmpBenchConn = RDFConnectionBuilder.start().defaultDataset().getConnection()) {
                SparqlQueryConnection benchConn = new SparqlQueryConnectionWithExecFails(tmpBenchConn,
                    ImmutableMap.<Query, Throwable>builder()
                        .put(QueryFactory.create("SELECT (COUNT(*) AS ?c_1) { ?a ?b ?c }"),
                                new RuntimeException("purposely failed"))
                        .build()::get
                );

                LsqBenchmarkProcessor processor = new LsqBenchmarkProcessor(cfg, run, benchConn, indexConn);

                List<LsqQuery> lsqQueries = Arrays.asList(
                        ModelFactory.createDefaultModel().createResource().as(LsqQuery.class)
                            .setQueryAndHash(QueryFactory.create("SELECT * { { ?a ?b ?c . ?x ?y ?z } UNION { ?a ?b ?c } }")),
                        ModelFactory.createDefaultModel().createResource().as(LsqQuery.class)
                            .setQueryAndHash(QueryFactory.create("SELECT * { ?a ?b ?c } LIMIT 10"))
                );

                for (LsqQuery lsqQuery : lsqQueries) {
                    lsqQuery = processor.process(lsqQuery);
                }
            }
        }
    }
}
