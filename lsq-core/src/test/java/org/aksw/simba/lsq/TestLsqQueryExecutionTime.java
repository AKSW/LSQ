package org.aksw.simba.lsq;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.file.CacheBackendFile;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.connection.SparqlQueryConnectionJsa;
import org.aksw.jena_sparql_api.utils.ModelDiff;
import org.aksw.jena_sparql_api.utils.model.ResourceInDataset;
import org.aksw.simba.lsq.core.LsqConfigImpl;
import org.aksw.simba.lsq.core.LsqProcessor;
import org.aksw.simba.lsq.core.LsqUtils;
import org.aksw.simba.lsq.core.Skolemize;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionModular;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Streams;

import io.reactivex.Flowable;


public class TestLsqQueryExecutionTime {

    /**
     * Ensure that there are is no obvious awkwardness in query execution times, such as
     * - errors in the measurement - e.g. Duration.getNanos vs Duration.toNanos
     * - caching artifacts
     *
     *
     * TODO Maybe the cleanest way to deal with query execution times is to do QueryExecution.unwrap(...) - however, there is no unwrap method as of 03/2018
     * @throws IOException
     *
     */
    @Test
    public void testLsqQueryExecutionTime() throws IOException {

        // TODO Make cache backend work on classpath resources...
        //Path tmpDir = Paths.get(first, more)
        //Path tmpDir = com.google.common.io.Files.createTempDir().toPath();
        boolean readonlyCache = true;
        //Path tmpDir = Paths.get("classpath:/lsq-tests-sparql-cache");
//		Path tmpDir = Paths.get("/home/raven/Projects/Eclipse/lsq-parent/lsq-core/src/test/resources/lsq-tests-sparql-cache");
        Path tmpDir = Paths.get("src/test/resources/lsq-tests-sparql-cache");


        //System.out.println("Cache temp dir: " + tmpDir);
        CacheBackend cacheBackend = new CacheBackendFile(tmpDir, Long.MAX_VALUE, false, readonlyCache, !readonlyCache);

        QueryExecutionFactory baseQef = FluentQueryExecutionFactory
                .alwaysFail("http://dbpedia.org/sparql", "http://dbpedia.org")
                //.http("http://dbpedia.org/sparql", "http://dbpedia.org")
                .config()
                    .withCache(cacheBackend)

                .end()
                .create();

        QueryExecutionFactory dataQef = FluentQueryExecutionFactory
                .from(baseQef)
                .create();

        QueryExecutionFactory benchmarkQef = FluentQueryExecutionFactory
            .from(baseQef)
            .config()
//				.withQueryTransform(q -> {
//					try {
//						Thread.sleep(1500);
//					} catch (InterruptedException e) {
//						throw new RuntimeException(e);
//					}
//					return q;
//				})
            .end()
            .create();

        RDFConnection benchmarkConn = new RDFConnectionModular(new SparqlQueryConnectionJsa(benchmarkQef), null, null);
        RDFConnection dataConn = new RDFConnectionModular(new SparqlQueryConnectionJsa(dataQef), null, null);

        //FluentRDFConnectionFn.

        LsqConfigImpl config = new LsqConfigImpl()
            .setInQueryLogFiles(Collections.singletonList("lsq-tests/triple-pattern-selectivity/tpsel01.sparql"))
            .setInQueryLogFormat("sparql")

            .setOutBaseIri("http://example.org/")
            .setDatasetLabel("mydata")

            .setBenchmarkConnection(benchmarkConn)
            .setDataConnection(dataConn)
            .setFetchDatasetSizeEnabled(true)
            .setRdfizerQueryLogRecordEnabled(true)
            .setRdfizerQueryEnabled(true)
            .setRdfizerQueryStructuralFeaturesEnabled(true)
            .setRdfizerQueryExecutionEnabled(true)
            .setDeterministicPseudoTimestamps(true)

            .setPrefixSources(Collections.emptyList())
            //.setc
            //.setBenchmarkEndpointDescription(new SparqlServiceReference("http://dbpedia.org/sparql"))
            ;


        LsqUtils.applyDefaults(config);
        Flowable<ResourceInDataset> stream = LsqUtils.createReader(config);
        LsqProcessor processor = LsqUtils.createProcessor(config);
        processor.setLegacyMode(true);

        //Path expectedResult =
        LsqQuery actualRes = stream
//				.doAfterNext(r -> RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE))
                .map(rid -> {
                    LsqQuery r = processor.applyForQueryOrWebLogRecord(rid);
                    return r;
                })
                .firstOrError()
                .blockingGet();

        Model actual = actualRes.getModel();
        Model expected = RDFDataMgr.loadModel("lsq-tests/triple-pattern-selectivity/tpsel01.ttl");


//      Test to check whether equals method works through potential proxy resource for LsqQuery
//		Statement stmt = expected.listStatements().next();
//		System.out.println("REF: " + stmt);
//		for(Statement cand : actual.listStatements().toSet()) {
//			System.out.println("  match? " + cand  + " - " + cand.equals(stmt));
//		}

        // Remove ignored properties; should only be used where property values are non-deterministic
        List<Property> ignoreProperties = Arrays.asList(LSQ.runTimeMs);

        for(Property p : ignoreProperties) {
            actual.listStatements(null, p, (RDFNode)null).toSet().forEach(actual::remove);
            expected.listStatements(null, p, (RDFNode)null).toSet().forEach(expected::remove);
        }


//		System.out.println("BEGIN OF ACTUAL");
//		RDFDataMgr.write(System.out, actual, RDFFormat.NTRIPLES);
//		System.out.println("END OF ACTUAL");

        ModelDiff diff = ModelDiff.create(expected, actual);

        if(!diff.isEmpty()) {
            System.err.println("Excessive: ---------------------");
            RDFDataMgr.write(System.err, diff.getAdded(), RDFFormat.TURTLE_PRETTY);
            System.err.println("Missing: ---------------------");
            RDFDataMgr.write(System.err, diff.getRemoved(), RDFFormat.TURTLE_PRETTY);
        }

        Assert.assertTrue(diff.isEmpty());

        // TODO Small test case for skolemization of lists; should go elsewhere
    }


    public void testSkolemization() {
        Resource r = ModelFactory.createDefaultModel().createResource("http://foo.bar/baz");
        RDFList l =
                r.getModel().createList()
                .with(RDFS.label)
                .with(RDFS.comment);


        r.addProperty(RDF.type, l);
        Skolemize.skolemize(r);
        RDFDataMgr.write(System.out, r.getModel(), RDFFormat.NTRIPLES);
        boolean containsBn = Streams.stream(GraphUtils.allNodes(r.getModel().getGraph())).anyMatch(Node::isBlank);
        //System.out.println("Contains bn: " + containsBn);
        // expected: containsBn == false
        Assert.assertFalse(containsBn);
    }
}
