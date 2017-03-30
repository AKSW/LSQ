package org.aksw.simba.lsq.cli.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.beast.vocabs.PROV;
import org.aksw.commons.util.exception.ExceptionUtilsAksw;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.fedx.jsa.FedXFactory;
import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryExceptionCache;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.cache.staging.CacheBackendMem;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmtQuery;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.ModelUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.simba.lsq.core.LSQARQ2SPIN;
import org.aksw.simba.lsq.core.QueryStatistics2;
import org.aksw.simba.lsq.core.Skolemize;
import org.aksw.simba.lsq.util.Mapper;
import org.aksw.simba.lsq.util.NestedResource;
import org.aksw.simba.lsq.util.SpinUtils;
import org.aksw.simba.lsq.util.WebLogParser;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.util.VarUtils;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.vocabulary.SP;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
/**
 * This is the main class used to RDFise query logs
 * @author Saleem
 *
 */
public class MainLSQ {

    private static final Logger logger = LoggerFactory.getLogger(MainLSQ.class);

    public static final OptionParser parser = new OptionParser();


//    public static final PrefixMapping lsqPrefixes;
//
//    static {
//        try {
//            ClassPathResource r = new ClassPathResource("lsq-prefixes.ttl");
//            Model m = ModelFactory.createDefaultModel();
//            m.read(r.getInputStream(), "http://example.org/base/", "turtle");
//            lsqPrefixes = m;
//        } catch(Exception e) {
//            throw new RuntimeException(e);
//        }
//    }


    public static void main(String[] args) throws Exception  {
        // Try to start - if something goes wrong print help
        // TODO Logic for when help is displayed could be improved
        //System.out.println("aoeuaoeuoaeu%2Fnstaeouhaoet%xx".replaceAll("\\%..", "-"));

//    	QueryExecution xxx = org.apache.jena.query.QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", "select * { ?s ?p ?o }");
//    	xxx.setTimeout(1, 1);
//    	ResultSetFormatter.consume(xxx.execSelect());

        try {
            run(args);
        } catch(Exception e) {
            logger.error("Error", e);
            parser.printHelpOn(System.err);
            throw new RuntimeException(e);
        }
    }

    /**
     * Nicely format a resource.
     * Used for logging.
     *
     * @param r
     * @return
     */
    public static String toString(Resource r) {
        Model m = ResourceUtils.reachableClosure(r);
        m.setNsPrefix("rdfs", RDFS.uri);
        m.setNsPrefix("lsq", LSQ.ns);
        String result = ModelUtils.toString(m, "TTL");
        return result;
    }

    public static void run(String[] args) throws Exception  {

        //RDFWriterRegistry.register(N3JenaWriter.n3WriterPlain, RDFFormat.TURTLE_BLOCKS);
        //RDFWriterRegistry.ir

        // Load the default web access log formats

        Map<String, Mapper> logFmtRegistry = WebLogParser.loadRegistry(RDFDataMgr.loadModel("default-log-formats.ttl"));

//    	{
//    		for(Entry<String, Mapper> e : logFmtRegistry.entrySet()) {
//    			String name = e.getKey();
//		    	Mapper mapper = e.getValue();
//		    	Resource r = ModelFactory.createDefaultModel().createResource();
//		    	try {
//		    		mapper.parse(r, "d4bec8a9ff6a49f36e2d44718c619461 - - [10/Apr/2016 03:00:00 +0200] \"GET /sparql?query=%0A++++++++++++select+%3Ftype+where+%7B%0A++++++++++++%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FUnder_Voluntary%3E+%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23type%3E+%3Ftype+.%0A++++++++++++%7D HTTP/1.1\" 200 115 \"-\" \"R\" \"-\"");
//		    	} catch(Exception x) {
//		    		continue;
//		    	}
//		    	System.out.println("Match: " + name);
//		    	RDFDataMgr.write(System.out, r.getModel(), RDFFormat.TURTLE);
//    		}
//    	}

        OptionSpec<File> inputOs = parser
                .acceptsAll(Arrays.asList("f", "file"), "File containing input data")
                .withRequiredArg()
                .ofType(File.class)
                ;

        OptionSpec<File> outputOs = parser
                .acceptsAll(Arrays.asList("o", "output"), "File where to store the output data.")
                .withRequiredArg()
                .ofType(File.class)
                ;

        OptionSpec<String> logFormatOs = parser
                .acceptsAll(Arrays.asList("m", "format"), "Format of the input data. Available options: " + logFmtRegistry.keySet())
                .withOptionalArg()
                .defaultsTo("combined")
                ;

        OptionSpec<String> outFormatOs = parser
                .acceptsAll(Arrays.asList("w", "outformat"), "Format for (w)riting out data. Available options: " + RDFWriterRegistry.registered())
                .withRequiredArg()
                .defaultsTo("Turtle/blocks")
                ;

        OptionSpec<String> rdfizerOs = parser
                .acceptsAll(Arrays.asList("r", "rdfizer"), "RDFizer selection: Any combination of the letters (e)xecution, (l)og and (q)uery")
                .withOptionalArg()
                .defaultsTo("elq")
                ;

        OptionSpec<String> endpointUrlOs = parser
                .acceptsAll(Arrays.asList("e", "endpoint"), "Local SPARQL service (endpoint) URL on which to execute queries")
                .withRequiredArg()
                .defaultsTo("http://localhost:8890/sparql")
                ;

        OptionSpec<String> graphUriOs = parser
                .acceptsAll(Arrays.asList("g", "graph"), "Local graph(s) from which to retrieve the data")
                .availableIf(endpointUrlOs)
                .withRequiredArg()
                ;

        OptionSpec<String> datasetLabelOs = parser
                .acceptsAll(Arrays.asList("l", "label"), "Label of the dataset, such as 'dbpedia' or 'lgd'. Will be used in URI generation")
                .withRequiredArg()
                .defaultsTo("mydata")
                ;

        OptionSpec<Long> headOs = parser
                .acceptsAll(Arrays.asList("h", "head"), "Only process n entries starting from the top")
                .withRequiredArg()
                .ofType(Long.class)
                ;

        OptionSpec<Long> datasetSizeOs = parser
                .acceptsAll(Arrays.asList("d", "dsize"), "Dataset size. Used in some computations. If not given, it will be queried (which might fail). Negative values disable dependent computations.")
                .withRequiredArg()
                .ofType(Long.class)
                ;

        OptionSpec<Long> timeoutInMsOs = parser
                .acceptsAll(Arrays.asList("t", "timeout"), "Timeout in milliseconds")
                .withRequiredArg()
                .ofType(Long.class)
                //.defaultsTo(60000l)
                //.defaultsTo(null)
                ;

        OptionSpec<String> baseUriOs = parser
                .acceptsAll(Arrays.asList("b", "base"), "Base URI for URI generation")
                .withRequiredArg()
                .defaultsTo(LSQ.defaultLsqrNs)
                ;

        OptionSpec<String> logEndpointUriOs = parser
                .acceptsAll(Arrays.asList("p", "public"), "Public endpoint URL - e.g. http://example.org/sparql")
                .withRequiredArg()
                //.defaultsTo("http://example.org/sparql")
                //.defaultsTo(LSQ.defaultLsqrNs + "default-environment");
                ;

        OptionSpec<String> expBaseUriOs = parser
                .acceptsAll(Arrays.asList("x", "experiment"), "URI of the experiment environment")
                .withRequiredArg()
                //.defaultsTo(LSQ.defaultLsqrNs)
                ;

        OptionSpec<String> fedEndpointsOs = parser
                .acceptsAll(Arrays.asList("fed"), "URIs of federated endpoints")
                .withRequiredArg();

        OptionSpec<File> fedEndpointsFileOs = parser
                .acceptsAll(Arrays.asList("fedf"), "URIs of federated endpoints")
                .withRequiredArg()
                .ofType(File.class);


        OptionSet options = parser.parse(args);

        // Write to file or sysout depending on arguments
        PrintStream out;
        File outFile = null;
        boolean[] outNeedsClosing = { true };
        if(options.has(outputOs)) {
            outFile = outputOs.value(options);
            out = new PrintStream(outFile);
        } else {
            out = System.out;
            outNeedsClosing[0] = false;
        }



        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if(outNeedsClosing[0]) {
                    logger.info("Shutdown hook: Flushing output");
                    out.flush();
                    out.close();
                }
            };
        });


        InputStream in;
        if(options.has(inputOs)) {
            File file = inputOs.value(options);
            file = file.getAbsoluteFile();
            in = new FileInputStream(file);
        } else {
            in = System.in;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        String datasetLabel = datasetLabelOs.value(options);
        String endpointUrl = endpointUrlOs.value(options);
        String baseUri = baseUriOs.value(options);
        String logEndpointUri = logEndpointUriOs.value(options) ;
        List<String> graph = graphUriOs.values(options);
        Long head = headOs.value(options);
        String rdfizer = rdfizerOs.value(options);
        Long timeoutInMs = timeoutInMsOs.value(options);

        boolean queryDatasetSize = !options.has(datasetSizeOs);

        Long datasetSize = options.has(datasetSizeOs) ? datasetSizeOs.value(options) : null;
        datasetSize = datasetSize == null ? null : (datasetSize < 0 ? null : datasetSize);

        boolean isExecutionEnabled = rdfizer.contains("e");

        String expBaseUri = expBaseUriOs.value(options);
        String logFormat = logFormatOs.value(options);
        String outFormatStr = outFormatOs.value(options);

        RDFFormat outFormat = RDFWriterRegistry.registered().stream().filter(f -> f.toString().equals(outFormatStr)).findFirst().orElse(null);
        if(outFormat == null) {
            throw new RuntimeException("No Jena writer found for name: " + outFormatStr);
        }

        expBaseUri = expBaseUri == null ? baseUri + datasetLabel : expBaseUri;

        List<String> fedEndpoints = new ArrayList<>();
        if(options.has(fedEndpointsFileOs)) {
            File fedEndpointsFile = fedEndpointsFileOs.value(options);
            Files.readLines(fedEndpointsFile, StandardCharsets.UTF_8).stream()
                    .map(String::trim)
                    .filter(line -> line.startsWith("#"))
                    .forEach(fedEndpoints::add);
        }

        if(options.has(fedEndpointsOs)) {
            List<String> tmp = fedEndpointsOs.values(options);
            fedEndpoints.addAll(tmp);
        }



//        These lines are just kept for reference in case we need something fancy
//        JOptCommandLinePropertySource clps = new JOptCommandLinePropertySource(options);
//        ApplicationContext ctx = SpringApplication.run(ConfigLSQ.class, args);

//        JenaSystem.init();
//        InitJenaCore.init();
//        ARQ.init();
//        SPINModuleRegistry.get().init();


        //DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //Calendar startTime = new GregorianCalendar();

        //Date date = startTime.getTime();
        //String prts []  = dateFormat.format(date).split(" ");


        // Note: We re-use the baseGeneratorRes in every query's model, hence its not bound to the specs model directly
        // However, with .inModel(model) we can create resources that are bound to a specific model from another resource
        //Resource baseGeneratorRes = ResourceFactory.createResource(baseUri + datasetLabel + "-" + prts[0]);

        Resource rawLogEndpointRes = logEndpointUri == null ? null : ResourceFactory.createResource(logEndpointUri);


        Model specs = ModelFactory.createDefaultModel();
        //Resource generatorRes = baseGeneratorRes.inModel(specs);


        // TODO Attempt to determine attributes automatically ; or merge this data from a file or something
//        Resource engineRes = specs.createResource()
//                .addProperty(LSQ.vendor, specs.createResource(LSQ.defaultLsqrNs + "Virtuoso"))
//                .addProperty(LSQ.version, "Virtuoso v.7.2")
//                .addProperty(LSQ.processor, "2.5GHz i7")
//                .addProperty(LSQ.ram,"8GB");
//        generatorRes
//            .addProperty(LSQ.engine, engineRes);

//        Resource datasetRes = specs.createResource();
//        generatorRes
//            .addLiteral(LSQ.dataset, datasetRes)
//            .addLiteral(PROV.hadPrimarySource, specs.createResource(endpointUrl))
//            .addLiteral(PROV.startedAtTime, startTime);

        Model logModel = ModelFactory.createDefaultModel();

        Stream<String> stream = reader.lines();

        if(head != null) {
            stream = stream.limit(head);
        }

        Mapper webLogParser = logFmtRegistry.get(logFormat);
        if(webLogParser == null) {
            throw new RuntimeException("No log format parser found for '" + logFormat + "'");
        }
        //WebLogParser webLogParser = new WebLogParser(WebLogParser.apacheLogEntryPattern);

        // TODO Use zipWithIndex in order to make the index part of the resource
        Stream<Resource> workloadResourceStream = stream
                .map(line -> {
                Resource r = logModel.createResource();
                r.addLiteral(RDFS.label, line);
                boolean parsed = webLogParser.parse(r, line) != 0;
                if(!parsed) {
                    r.addLiteral(LSQ.processingError, "Failed to parse log line");
                }

                return r;
            });


//        List<Resource> workloadResources = stream
//                .map(line -> {
//                Resource r = logModel.createResource();
//                webLogParser.parseEntry(line, r);
//                return r;
//            })
//            .collect(Collectors.toList());


        Cache<String, byte[]> queryCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .build();

        Cache<String, Exception> exceptionCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .build();


        QueryExecutionFactory countQef;
        QueryExecutionFactory baseDataQef;
        QueryExecutionFactory dataQef = null;

        if(isExecutionEnabled) {
            boolean isNormalMode = fedEndpoints.isEmpty();
            //boolean isFederatedMode = !isNormalMode;

            if(isNormalMode) {
                countQef =
                        FluentQueryExecutionFactory
                        .http(endpointUrl, graph)
                        .create();

                baseDataQef = FluentQueryExecutionFactory.http(endpointUrl, graph).create();

            } else {
                countQef = null;

                baseDataQef = FedXFactory.create(fedEndpoints);
            }

            dataQef =
                    FluentQueryExecutionFactory
                    //.http(endpointUrl, graph)
                    .from(baseDataQef)
                    .config()
                        .withPostProcessor(qe -> {
                            if(timeoutInMs != null) {
                                qe.setTimeout(0, timeoutInMs);
    //                            ((QueryEngineHTTP)((QueryExecutionHttpWrapper)qe).getDecoratee())
    //                            .setTimeout(timeoutInMs);
                            }
                        })
                        //.onTimeout((qef, queryStmt) -> )
                        .withCache(new CacheFrontendImpl(new CacheBackendMem(queryCache)))
                        .compose(qef ->  new QueryExecutionFactoryExceptionCache(qef, exceptionCache))
                        //)
    //                    .withRetry(3, 30, TimeUnit.SECONDS)
    //                    .withPagination(1000)
                    .end()
                    .create();

            if(queryDatasetSize) {
                logger.info("Counting triples in the endpoint ...");
                datasetSize = countQef == null ? null : QueryExecutionUtils.countQuery(QueryFactory.create("SELECT * { ?s ?p ?o }"), countQef);
            }
        }


//        System.out.println(queryToSubmissions.keySet().size());

//        logger.info("Number of distinct queries in log: "

        // This is an abstraction that can execute SPARQL queries


//            RiotLib.writeBase(out, base) ;
//            RiotLib.writePrefixes(out, prefixMap) ;
//            ShellGraph x = new ShellGraph(graph, null, null) ;
//            x.writeGraph() ;


        //int workloadSize = workloadResources.size();
        Long workloadSize = null;

        logger.info("About to process " + workloadSize + " queries");
        logger.info("Dataset size of " + endpointUrl + " / " + graph + ": " + datasetSize);

        //rdfizer.rdfizeLog(out, generatorRes, queryToSubmissions, dataQef, separator, localEndpoint, graph, acronym);


        //Calendar endTime = new GregorianCalendar();
        //specs.add(datasetRes, PROV.startedAtTime, specs.createTypedLiteral(endTime));

        //specs.write(out, "NTRIPLES");

        //SparqlQueryParser queryParser = SparqlQueryParserImpl.create(Syntax.syntaxARQ);
        SparqlStmtParser stmtParser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, true);


        //Set<Query> executedQueries = new HashSet<>();

        //.addLiteral(PROV.startedAtTime, start)
        //.addLiteral(PROV.endAtTime, end)
        SimpleDateFormat dt = new SimpleDateFormat("yyyyy-mm-dd_hh:mm:ss");

        Calendar expStart = Calendar.getInstance();
        //String expStartStr = dt.format(expStart.getTime());

        Model expModel = ModelFactory.createDefaultModel();
        NestedResource expBaseRes = new NestedResource(expModel.createResource(expBaseUri));

      //  Resource expRes = expBaseRes.nest("-" + expStartStr).get();
        Resource expRes = expBaseRes.get();   //we do not need to nest the expStartStr
        expRes
          //  .addProperty(PROV.wasAssociatedWith, expBaseRes.get())
            .addLiteral(PROV.startedAtTime, expStart);

        RDFDataMgr.write(out, expModel, outFormat);


//        myenv
//            engine aeouaoeu
//            dataset aeuaoeueoa
//
//
//       myevn-1-1-2016
//            basedOn myenv
//            startedAtTime
//            endAtTime

        // Small array hack in order to change the values while streaming
        //int logFailCount[] = new int[] {0};
        //long logEntryIndex[] = new long[] {0l};
        int logFailCount = 0;
        long logEntryIndex = 0l;
        int batchSize = 10;

        // TODO We should check beforehand whether there is a sufficient number of processable log entries
        // available in order to consider the workload a query log
        //for(Resource r : workloadResources) {
        //workloadResourceStream.forEach(r -> {
        Iterator<Resource> it = workloadResourceStream.iterator();
        while(it.hasNext()) {
            Resource r = it.next();

            // Extract the query and add it with the lsq:query property
            WebLogParser.extractQuery(r);

            // If the resource is null, we could not parse the log entry
            // therefore count this as an error

            boolean fail = false;
            boolean parsed = r.getProperty(LSQ.processingError) == null ? true : false;

            try {
                if(parsed) {
                    Optional<String> str = Optional.ofNullable(r.getProperty(LSQ.query))
                            .map(queryStmt -> queryStmt.getString());

                    //Model m = ResourceUtils.reachableClosure(r);
                    SparqlStmt stmt = str
                            .map(stmtParser)
                            .orElse(null);

                    if(stmt != null && stmt.isQuery()) {

                        SparqlStmtQuery queryStmt = stmt.getAsQueryStmt();

                        String queryStr;
                        Query query;
                        if(!queryStmt.isParsed()) {
                            query = null;
                            queryStr = queryStmt.getOriginalString();
                        } else {
                            query = queryStmt.getQuery();
                            queryStr = "" + queryStmt.getQuery();
                        }

//	                    long rnd = (new Random()).nextLong();
//	                    query = QueryFactory.create("SELECT COUNT(*) { ?s ?p ?o . ?o ?x ?y } Order By ?y Limit 10", Syntax.syntaxARQ);
//	                    queryStr = "" + query;

                        if(logEntryIndex % batchSize == 0) {
                            long batchEndTmp = logEntryIndex + batchSize;
                            long batchEnd = workloadSize == null ? batchEndTmp : Math.min(batchEndTmp, workloadSize);
                            logger.info("Processing query batch from " + logEntryIndex + " - "+ batchEnd); // + ": " + queryStr.replace("\n", " ").substr);
                        }


                        Model queryModel = ModelFactory.createDefaultModel();


                        Resource logEndpointRes = rawLogEndpointRes == null ? null : rawLogEndpointRes.inModel(queryModel);

                        //NestedResource baseRes = new NestedResource(generatorRes).nest(datasetLabel).nest("-");
                        NestedResource baseRes = new NestedResource(queryModel.createResource(baseUri));

                        String queryHash = StringUtils.md5Hash(queryStr).substring(0, 8);
                        NestedResource queryRes = baseRes.nest("q-" + queryHash);

                        Function<String, NestedResource> queryAspectFn = (aspect) -> baseRes.nest(aspect).nest("q-" + queryHash);

                        queryRes.get()
                            .addProperty(RDF.type, SP.Query)
                            //.addLiteral(LSQ.text, ("" + queryStr).replace("\n", " "));
                            .addLiteral(LSQ.text, ("" + queryStr).replace("\n", " "));


                        if(!queryStmt.isParsed()) {
                            String msg = queryStmt.getParseException().getMessage();
                            queryRes.get()
                                .addLiteral(LSQ.parseError, msg);
                        } else {
                            if(rdfizer.contains("q")) { // TODO Replace with function call
                                rdfizeQuery(queryRes.get(), queryAspectFn, query);
                            }
                        }


                        if(rdfizer.contains("l")) {

                            // Deal with log entry (remote execution)
                            String hashedIp = StringUtils.md5Hash("someSaltPrependedToTheIp" + r.getProperty(LSQ.host).getString()).substring(0, 16);

                            Resource agentRes = baseRes.nest("agent-" + hashedIp).get();


                            Literal timestampLiteral = r.getProperty(PROV.atTime).getObject().asLiteral();
                            Calendar timestamp = ((XSDDateTime)timestampLiteral.getValue()).asCalendar();
                            String timestampStr = dt.format(timestamp.getTime());
                            //String timestampStr = StringUtils.md5Hash("someSaltPrependedToTheIp" + r.getProperty(LSQ.host).getString()).substring(0, 16);

                            Resource queryExecRecRes = queryAspectFn.apply("re-" + datasetLabel + "-").nest("-" + hashedIp + "-" + timestampStr).get();

                            // Express that the query execution was recorded
                            // at some point in time by some user at some service
                            // according to some source (e.g. the log file)
                            queryRes.get()
                                .addProperty(LSQ.hasRemoteExecution, queryExecRecRes);

                            queryExecRecRes
                                //.addProperty(RDF.type, LSQ.)
                                .addLiteral(PROV.atTime, timestampLiteral.inModel(queryModel))
                                .addProperty(LSQ.wasAssociatedWith, agentRes)
                                ;

                            if(logEndpointRes != null) {
                                queryExecRecRes.addProperty(LSQ.endpoint, logEndpointRes); // TODO Make it possible to specify the dataset configuration that was used to execute the query
                            }

                        }


                        if(isExecutionEnabled) {
                            //boolean hasBeenExecuted = executedQueries.contains(query);

                            boolean hasBeenExecuted = false;
                            if(!hasBeenExecuted) {
                                //executedQueries.add(query);


                                Calendar now = Calendar.getInstance();
                                String nowStr = dt.format(now.getTime());
                                Resource queryExecRes = queryAspectFn.apply("le-" + datasetLabel + "-").nest("-" + nowStr).get();

                                queryExecRes
                                    .addProperty(PROV.wasGeneratedBy, expRes);


                                // TODO Switch between local / remote execution
                                if(query != null) {
                                    queryRes.get()
                                        .addProperty(LSQ.hasLocalExecution, queryExecRes);

                                    rdfizeQueryExecution(queryRes.get(), query, queryExecRes, dataQef, datasetSize);
                                }
                            }
                        }

                        // Post processing: Craft global IRIs for SPIN variables
                        Set<Statement> stmts = queryModel.listStatements(null, SP.varName, (RDFNode)null).toSet();
                        for(Statement st : stmts) {
                            Resource s = st.getSubject();
                            String varName = st.getLiteral().getString();
                            //String varResUri = baseRes.nest("var-").nest(varName).str();
                            String varResUri = queryRes.nest("-var-").nest(varName).str();
                            ResourceUtils.renameResource(s, varResUri);
                        }

                        // Post processing: Remove skolem identifiers
                        queryModel.removeAll(null, Skolemize.skolemId, null);


                        RDFDataMgr.write(out, queryModel, outFormat);

                        // TODO Frequent flushing may decrease performance
                        // out.flush();
                    } else {
                        logger.debug("Skipping non-sparql-query log entry #" + logEntryIndex);
                        logger.debug(toString(r));
                    }

                } else {
                    ++logFailCount;
                    double ratio = logEntryIndex == 0 ? 0.0 : logFailCount / logEntryIndex;
                    if(logEntryIndex == 10 && ratio > 0.8) {
                        fail = true;
                    }

                    logger.warn("Skipping non-parsable log entry #" + logEntryIndex);
                    logger.warn(toString(r));
                }

            } catch(Exception e) {
                logger.warn("Unexpected exception encountered at item " + logEntryIndex + " - ", e);
                logger.warn(toString(r));
            }

            if(fail) {
                throw new RuntimeException("Encountered too many non processable log entries. Probably not a log file.");
            }

            //.write(System.err, "TURTLE");
            ++logEntryIndex;
        }


        Model tmpModel = ModelFactory.createDefaultModel();
        expRes.inModel(tmpModel)
            .addLiteral(PROV.endAtTime, Calendar.getInstance());

        RDFDataMgr.write(out, tmpModel, outFormat); //RDFFormat.TURTLE_BLOCKS);


        out.flush();

        // If the output stream is based on a file then close it, but
        // don't close stdout
        if(outNeedsClosing[0]) {
            outNeedsClosing[0] = false;
            out.close();
            logger.info("Done. Output written to: " + outFile.getAbsolutePath());
        } else {
            logger.info("Done.");
        }

    }


    /**
     * RDFize Log
     * @param queryToSubmissions A map which store a query string (single line) as key and all the corresponding submissions as List. Where a submission is a combination
     * of User encrypted ip and the data,time of the query request. The I.P and the time is separated by a separator
     * @param publicEndpoint Public endpoint of the log
     * @param graph named Graph, can be null
     * @param outputFile The output RDF file
     * @param separator Submission separator. Explained above
     * @param acronym A Short acronym of the dataset log, e.g., DBpedia or SWDF
     * @param processor
     * @param ram
     * @param endpointVersion
     * @param ep
     * @param curTime
     * @throws IOException
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     * @throws ParseException
     */
    public static void rdfizeQueryExecutionOld(
            //OutputStream out,
            //Resource generatorRes,
            //NestedResource baseRes,
            Resource queryRes,
            Function<String, NestedResource> nsToBaseRes,
            Query query,
            //Map<String, Set<String>> queryToSubmissions,
            QueryExecutionFactory dataQef) {
            //String separator,
            //String localEndpoint, // TODO get rid of argument, and use dataQef for executing queries instead
            //String graph, // TODO get rid of argument, and use dataQef for executing queries instead
            //String acronym)  {

        logger.info("RDFization started...");
        long endpointSize = ServiceUtils.fetchInteger(dataQef.createQueryExecution("SELECT (COUNT(*) AS ?x) { ?s ?p ?o }"), Vars.x);


        // endpointSize = Selectivity.getEndpointTotalTriples(localEndpoint,
        // graph);
        long parseErrorCount = 0;

        // this.writePrefixes(acronym);
        // Resource executionRes =
        // model.createResource(lsqr:le-"+acronym+"-q"+queryHash);

        //for (String queryStr : queryToSubmissions.keySet()) {

        Model model = ModelFactory.createDefaultModel();

        // TODO The issue is that we want to have different 'classifiers' for a certain resource
        // e.g. node1235, way123, relation123, query123, etc
        //
        //model.createResource(LSQ.defaultLsqrNs + "le-" + datasetLabel + "-q" + queryHash);

        Resource localExecutionRes = nsToBaseRes.apply("le-").get();
        Resource remoteExecutionRes = nsToBaseRes.apply("re-").get();

        //Resource queryRes = nsToBaseRes.apply("").get();

        //Resource remoteExecutionRes = model.createResource(LSQ.defaultLsqrNs + "re-" + datasetLabel + "-q" + queryHash);
        String queryStr = query.toString();

        queryRes
            //.addProperty(PROV.wasGeneratedBy, baseRes.get())
            .addProperty(LSQ.text, queryStr.trim())
            .addProperty(LSQ.hasLocalExecution, localExecutionRes)
            .addProperty(LSQ.hasRemoteExecution, remoteExecutionRes);

// TODO runtime
//        localExecutionRes
//            .addLiteral(LSQ.runTimeMs, );

        //rdfizeQuery(new NestedResource(queryRes), query);

                    //queryStats = queryStats+"\nlsqr:q"+queryHash+" lsqv:hasLocalExecution lsqr:le-"+acronym+"-q"+queryHash+" . " ;

//            System.out.println(queryNo + " Started...");
//

//            try {
//                if (query.isDescribeType()) {
////                    this.RDFizeDescribe(query, localEndpoint, graph,
////                            queryToSubmissions.get(queryStr), separator);
//                } else if (query.isSelectType()) {
//                    //this.rdfizeQuery(model, itemRes, query, dataQef, queryToSubmissions.get(queryStr), separator);
//                } else if (query.isAskType()) {
////                    this.RDFizeASK(query, localEndpoint, graph,
////                            queryToSubmissions.get(queryStr), separator);
//                } else if (query.isConstructType()) {
////                    this.RDFizeConstruct(query, localEndpoint, graph,
////                            queryToSubmissions.get(queryStr), separator);
//                }
//            } catch (Exception ex) {
//                throw new RuntimeException("Unhandled exception: ", ex);
//            }


            //model.write(System.out, "TURTLE");



            //RDFDataMgr.write(out, model, RDFFormat.NTRIPLES);
//        }

        // TODO Track and report parse and execution errors
//        logger.info("Total Number of Queries with Parse Errors: "
//                + parseErrorCount);
//        logger.info("Total Number of Queries with Runtime Errors: "
//                + runtimeErrorCount);
    }


    public static void rdfizeQueryExecution(Resource queryRes, Query query, Resource queryExecRes, QueryExecutionFactory qef, Long datasetSize) {

        try {
    //        Stopwatch sw = Stopwatch.createStarted();
    //      long runtimeInMs = sw.stop().elapsed(TimeUnit.MILLISECONDS);



            Calendar start = Calendar.getInstance();
            //long resultSetSize = QueryExecutionUtils.countQuery(query, qef);

            QueryExecution qe = qef.createQueryExecution(query);
            long resultSetSize = QueryExecutionUtils.consume(qe);

            Calendar end = Calendar.getInstance();
            Duration duration = Duration.between(start.toInstant(), end.toInstant());


            queryExecRes
                .addLiteral(LSQ.resultSize, resultSetSize)
                .addLiteral(LSQ.runTimeMs, duration.getNano() / 1000000l)
                //.addLiteral(PROV.startedAtTime, start)
                //.addLiteral(PROV.endAtTime, end)
                ;

            SpinUtils.enrichModelWithTriplePatternExtensionSizes(queryRes, queryExecRes, qef);


            if(datasetSize != null) {

                Set<Resource> tpExecRess = SpinUtils.createTriplePatternExecutions(queryRes, queryExecRes);
                SpinUtils.enrichModelWithTriplePatternSelectivities(tpExecRess, qef, datasetSize);


                Multimap<Resource, org.topbraid.spin.model.Triple> bgpToTps = SpinUtils.indexBasicPatterns2(queryRes);
                SpinUtils.enrichModelWithBGPRestrictedTPSelectivities(qef, queryExecRes.getModel(), bgpToTps);

                // For the id part, we can index the structural bgps, tps, joinVars
                //

                // BGP restricted triple pattern selectivity
                // These selectivities can be related directly to the TP executions
                // - [[bgp-vars]] / [[tp-vars]]

                // Join restricted triple pattern selectivity
                // For these selectivities we need to introduce _observation_ resources for
                // (1) each (join) var in the bgp
                // (2) each (join) var in a tp
                // - [[join-bgp-var]] / [[tp-var]]

                // Note, that there are resources for structural information on (join) bgp-vars
                // but I suppose there is no structural information for tp-vars
                // So the latter does not have a correspondence - TODO: how are var-resources allocated by topbraid's spin?
                //
                // We also need an API for working with LSQ data, otherwise it seems to me its too
                // cumbersome - for this we need some use cases, such as

                // - getLatestObservation(bgpRes); but instead of java methods,
                // we would also benefit from more powerful navigation and filtering of resources:
                // bgpRes.as(ResourceEnh.class).in(LSQ.onBGP).orderBy(LSQ.date).first()
                // .startOrder().newItem().in(LSQ.date).endItem().endOrder()
                // - getBGPStats(bgpRes)


                // For each variable in the BGP create a new resource
                for(Entry<Resource, Collection<org.topbraid.spin.model.Triple>> e : bgpToTps.asMap().entrySet()) {

                    // Map each resource to the corresponding jena element
                    Map<org.topbraid.spin.model.Triple, Element> resToEl = e.getValue().stream()
                            .collect(Collectors.toMap(
                                    r -> r,
                                    r -> ElementUtils.createElement(SpinUtils.toJenaTriple(r))));


                    Set<Var> bgpVars = resToEl.values().stream()
                            .flatMap(el -> PatternVars.vars(el).stream())
                            .collect(Collectors.toSet());


                    //String queryId = "";
                    String bgpId = e.getKey().getProperty(Skolemize.skolemId).getString();

                    Resource bgpCtxRes = queryExecRes.getModel().createResource(queryExecRes.getURI() + "-" + bgpId);

                    Map<Var, Resource> varToBgpVar = bgpVars.stream()
                            .collect(Collectors.toMap(
                                    v -> v,
                                    v -> NestedResource.from(bgpCtxRes).nest("-var-").nest(v.getName()).get()));

                    // Link the var occurrence
                    varToBgpVar.values().forEach(v -> bgpCtxRes.addProperty(LSQ.hasVar, v));


                    // Obtain the selectivity for the variable in that tp
                    //for(e.getValue())
                    Map<Var, Long> varToCount = QueryStatistics2.fetchCountJoinVarGroup(qef, resToEl.values());


                    // Add the BGP var statistics
                    varToCount.forEach((v, c) -> {
                        Resource vr = varToBgpVar.get(v);

                        vr.addLiteral(LSQ.resultSize, c);
                    });
                        //

                        //vr.addLiteral(LSQ.tpSelectivity, o);


                    Map<org.topbraid.spin.model.Triple, Map<Var, Long>> elToVarToCount = QueryStatistics2.fetchCountJoinVarElement(qef, resToEl);

                    elToVarToCount.forEach((t, vToC) -> {
                        String tpId = e.getKey().getProperty(Skolemize.skolemId).getString();

                        String tpResBase = queryExecRes.getURI() + "-" + tpId;

                        vToC.forEach((v, c) -> {
                            Resource tpVarRes = queryExecRes.getModel().createResource(tpResBase + "-" + v.getName());

                            long bgpJoinVarCount = varToCount.get(v);

                            double selectivity = c == 0 ? 0d : bgpJoinVarCount / (double)c;
                            tpVarRes
                                .addLiteral(LSQ.resultSize, c)
                                .addLiteral(LSQ.tpSelectivity, selectivity);

                        });

                        //Resource tpRes = queryExecRes.getModel().createResource(queryExecRes.getURI() + "-" + tpId);


                    });

                    System.out.println(varToCount);
                    System.out.println(elToVarToCount);


                    /*
                    bgp hasTp tp1
                    tp1 hasJoinVar tp1-jv-x
                    tp1-jv-x hasEval e1-tp1-jv-x
                    e1-tp1-jv-x selectivity 0.5
                    e1 inExperiment/onDataset DBpedia




                    */
                }

                // For each triple in the BGP create a new resource


                // We need to create observation resources for each variable in each bgp
                for(Entry<Resource, org.topbraid.spin.model.Triple> e : bgpToTps.entries()) {
                    org.topbraid.spin.model.Triple t = e.getValue();
                    org.apache.jena.graph.Triple tr = SpinUtils.toJenaTriple(t);

                    Set<Var> vars;

                    // allocate iris for each variable in the bgp
                    queryExecRes.getModel().createResource(queryExecRes.getURI() + "");


                    //queryExecRes + tp-id + var




                    // create the



                }
                //bgpToTps = SpinUtils.indexBasicPatterns2(queryRes);



                // Now create the resources for stats on the join vars
                //Set<Resource> joinVarRess = SpinUtils.createJoinVarObservations(queryRes, queryExecRes);

                // And now compute selectivities of the join variables
                SpinUtils.enrichModelWithJoinRestrictedTPSelectivities(qef, queryExecRes.getModel(), bgpToTps);
            }



                //SpinUtils.enrichModelWithTriplePatternSelectivities(queryRes, queryExecRes, qef, datasetSize); //subModel, resultSetSize);

                //SpinUtils.enrichModelWithBGPRestrictedTPSelectivities(queryRes, queryExecRes, qef, totalTripleCount);


            //  queryStats = queryStats + " lsqv:meanTriplePatternSelectivity "+Selectivity.getMeanTriplePatternSelectivity(query.toString(),localEndpoint,graph,endpointSize)  +" ; \n ";
            //long resultSize = QueryExecutionUtils.countQuery(query, dataQef);
            //long resultSize = this.getQueryResultSize(queryNew.toString(), localEndpoint,"select");
        }
        catch(Exception e) {
            Throwable f = ExceptionUtilsAksw.unwrap(e, QueryExceptionHTTP.class).orElse(e);
            String msg = f.getMessage();
            msg = msg == null ? "" + ExceptionUtils.getStackTrace(f) : msg;
            queryExecRes.addLiteral(LSQ.executionError, msg);
            String queryStr = ("" + query).replace("\n", " ");
            logger.warn("Query execution exception [" + msg + "] for query " + queryStr, e);
        }
    }


    // QueryExecutionFactory dataQef
    public static void rdfizeQuery(Resource queryRes, Function<String, NestedResource> queryAspectFn, Query query) {

        //Resource execRes = queryAspectFn.apply("exec").nest("-execX").get();


        try {
            query = query.cloneQuery();
            query.getGraphURIs().clear();

            Resource spinRes = queryAspectFn.apply("spin-").get();
//          queryNo++;

        // .. generate the spin model ...
            //Model spinModel = queryRes.getModel();
            Model spinModel = ModelFactory.createDefaultModel();
          LSQARQ2SPIN arq2spin = new LSQARQ2SPIN(spinModel);
          Resource tmpSpinRes = arq2spin.createQuery(query, null);



//          System.out.println("TEST {");
//          SpinUtils.indexTriplePatterns2(tmpSpinRes.getModel()).forEach(System.out::println);
//          SpinUtils.itp(tmpSpinRes).forEach(System.out::println);
//          System.out.println("}");
//tmpSpinRes.as(org.topbraid.spin.model.Query.class).getWhereElements().forEach(e -> {
//    System.out.println("XXElement: " + e.asResource().getId() + ": " + e);
//});


          // ... and rename the blank node of the query
          ResourceUtils.renameResource(tmpSpinRes, spinRes.getURI());

          spinRes.getModel().add(spinModel);

          // ... and skolemize the rest
          Skolemize.skolemize(spinRes);

          queryRes
              .addProperty(LSQ.hasSpin, spinRes);





//          } catch (Exception ex) {
//              String msg = ex.getMessage();// ExceptionUtils.getFullStackTrace(ex);
//              queryRes.addLiteral(LSQ.runtimeError, msg);
//
//              // TODO Port the getRDFUserExceptions function
//              // String queryStats =
//              // this.getRDFUserExecutions(queryToSubmissions.get(queryStr),separator);
//              parseErrorCount++;
//          }

            //queryStats = queryStats+" lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash ;
            Resource featureRes = queryAspectFn.apply("sf-").get(); // model.createResource(LSQ.defaultLsqrNs + "sf-q" + "TODO");//lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash

            queryRes.addProperty(LSQ.hasStructuralFeatures, featureRes);


            // Add used features
            QueryStatistics2.enrichResourceWithQueryFeatures(featureRes, query);


//            Set<Resource> features = ElementVisitorFeatureExtractor.getFeatures(query);
//            features.forEach(f -> featureRes.addProperty(LSQ.usesFeature, f));

            // TODO These methods have to be ported
            //queryStats = queryStats+ QueryStatistics.getDirectQueryRelatedRDFizedStats(query.toString()); // Query type, total triple patterns, join vertices, mean join vertices degree
            //queryStats = queryStats+QueryStatistics.rdfizeTuples_JoinVertices(query.toString());

            SpinUtils.enrichWithHasTriplePattern(featureRes, spinRes);
            SpinUtils.enrichWithTriplePatternText(spinRes);
            //Selectivity2.enrichModelWithTriplePatternExtensionSizes(model, dataQef);

            //
            QueryStatistics2.getDirectQueryRelatedRDFizedStats(spinRes, featureRes);

            QueryStatistics2.enrichWithPropertyPaths(featureRes, query);


            // TODO Move to a util function
            Set<Resource> serviceUris = spinModel.listStatements(null, SP.serviceURI, (RDFNode)null)
                    .mapWith(stmt -> stmt.getObject().asResource()).toSet();

            for(Resource serviceUri : serviceUris) {
                featureRes.addProperty(LSQ.usesService, serviceUri);
            }




            //QueryStatistics2.enrichWithMentions(featureRes, query); //the mentions subjects, predicates and objects can be obtained from Spin


        } catch (Exception ex) {
            String msg = ExceptionUtils.getFullStackTrace(ex);//ex.getMessage();
            queryRes.addLiteral(LSQ.processingError, msg);
            logger.warn("Failed to process query " + query, ex);
        }

        // TODO Add getRDFUserExecutions
    }
}
