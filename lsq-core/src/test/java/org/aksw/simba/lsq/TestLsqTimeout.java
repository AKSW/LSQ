package org.aksw.simba.lsq;

/**
 * Sets a timeout of 1 second, whereas query execution will be delayed by 5.
 *
 * @author Claus Stadler, Sep 26, 2018
 *
 */
//public class TestLsqTimeout {
//
//    protected QueryExecutionFactory dataQef;
//    protected QueryExecutionFactory benchmarkQef;
//
//    @Before
//    public void init() {
//
//        // TODO Make cache backend work on classpath resources...
//        //Path tmpDir = Paths.get(first, more)
//        //Path tmpDir = com.google.common.io.Files.createTempDir().toPath();
//        boolean readonlyCache = true;
//        //Path tmpDir = Paths.get("classpath:/lsq-tests-sparql-cache");
////		Path tmpDir = Paths.get("/home/raven/Projects/Eclipse/lsq-parent/lsq-core/src/test/resources/lsq-tests-sparql-cache");
//        Path tmpDir = Paths.get("src/test/resources/lsq-tests-sparql-cache");
//
//
//        //System.out.println("Cache temp dir: " + tmpDir);
//        CacheBackend cacheBackend = new CacheBackendFile(tmpDir, Long.MAX_VALUE, false, readonlyCache, !readonlyCache);
//
//        QueryExecutionFactory baseQef = FluentQueryExecutionFactory
//                //.alwaysFail("http://dbpedia.org/sparql", "http://dbpedia.org")
//                .from(ModelFactory.createDefaultModel())
//                //.http("http://dbpedia.org/sparql", "http://dbpedia.org")
//                .config()
//                    .withPostTransformer((Function<QueryExecution, QueryExecutionDecoratorBase<QueryExecution>>)qe -> new QueryExecutionDecoratorBase<QueryExecution>(qe) {
//                        Thread t;
//                        @Override public void beforeExec() {
//                            t = Thread.currentThread();
//                            try {
//                                Thread.sleep(60000);
//                            } catch (InterruptedException e) {
//                                throw new QueryCancelledException();								//throw new RuntimeException(e);
//                            }
//                        }
//                        @Override public void abort() {
//                            t.interrupt();
//                        }
//                    })
////					.withDelayFromNow(delayDuration, delayTimeUnit)
////					.withQueryTransform(q -> {
////						try {
////							Thread.sleep(60000);
////						} catch (InterruptedException e) {
////							throw new RuntimeException(e);
////						}
////						return q;
////					})
//                    .withCache(cacheBackend)
//                    .withParser(SparqlQueryParserImpl.create())
//                    .withExogeneousTimeout()
//                .end()
//                .create();
//
//        dataQef = FluentQueryExecutionFactory
//                .from(baseQef)
//                .create();
//
//        benchmarkQef = FluentQueryExecutionFactory
//            .from(baseQef)
//            .create();
//
//    }
//
//    @Test(expected=QueryCancelledException.class)
//    public void testLsqBenchmarkTimeout() {
//        try(QueryExecution dqe = benchmarkQef.createQueryExecution("SELECT * { ?s ?p ?o }")) {
//            dqe.setTimeout(100);
//            ResultSetFormatter.consume(dqe.execSelect());
//        }
//    }
//
//    @Test(expected=QueryCancelledException.class)
//    public void testLsqDataTimeout() {
//        try(QueryExecution dqe = dataQef.createQueryExecution("SELECT * { ?s ?p ?o }")) {
//            dqe.setTimeout(100);
//            ResultSetFormatter.consume(dqe.execSelect());
//        }
//    }
//
//}
