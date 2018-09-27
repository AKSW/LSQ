package org.aksw.simba.lsq;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.file.CacheBackendFile;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionDecoratorBase;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * Sets a timeout of 1 second, whereas query execution will be delayed by 5.
 * 
 * @author Claus Stadler, Sep 26, 2018
 *
 */
public class TestLsqTimeout {

	protected QueryExecutionFactory dataQef;
	protected QueryExecutionFactory benchmarkQef;

	@Before
	public void init() {
		
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
				//.alwaysFail("http://dbpedia.org/sparql", "http://dbpedia.org")
				.from(ModelFactory.createDefaultModel())
				//.http("http://dbpedia.org/sparql", "http://dbpedia.org")
				.config()
					.withPostProcessor((Function<QueryExecution, QueryExecutionDecoratorBase<QueryExecution>>)qe -> new QueryExecutionDecoratorBase<QueryExecution>(qe) {
						Thread t;
						@Override public void beforeExec() {
							t = Thread.currentThread();
							try {
								Thread.sleep(60000);
							} catch (InterruptedException e) {
								throw new QueryCancelledException();								//throw new RuntimeException(e);
							}
						}
						@Override public void abort() {
							t.interrupt();
						}
					})
//					.withDelayFromNow(delayDuration, delayTimeUnit)
//					.withQueryTransform(q -> {
//						try {
//							Thread.sleep(60000);
//						} catch (InterruptedException e) {
//							throw new RuntimeException(e);
//						}
//						return q;
//					})					
					.withCache(cacheBackend)
					.withParser(SparqlQueryParserImpl.create())
					.withExogeneousTimeout()
				.end()
				.create();

		dataQef = FluentQueryExecutionFactory
				.from(baseQef)
				.create();
		
		benchmarkQef = FluentQueryExecutionFactory
			.from(baseQef)
			.create();
		
	}
	
	@Test(expected=QueryCancelledException.class)
	public void testLsqBenchmarkTimeout() {
		QueryExecution dqe = benchmarkQef.createQueryExecution("SELECT * { ?s ?p ?o }");
		dqe.setTimeout(100);
		ResultSetFormatter.consume(dqe.execSelect());
	}

	@Test(expected=QueryCancelledException.class)
	public void testLsqDataTimeout() {
		QueryExecution dqe = dataQef.createQueryExecution("SELECT * { ?s ?p ?o }");
		dqe.setTimeout(100);
		ResultSetFormatter.consume(dqe.execSelect());
	}

}
