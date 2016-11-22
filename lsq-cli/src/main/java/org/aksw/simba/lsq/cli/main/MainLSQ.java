package org.aksw.simba.lsq.cli.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.cache.staging.CacheBackendMem;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmtQuery;
import org.aksw.jena_sparql_api.utils.ModelUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.simba.lsq.core.LSQARQ2SPIN;
import org.aksw.simba.lsq.core.QueryStatistics2;
import org.aksw.simba.lsq.core.Skolemize;
import org.aksw.simba.lsq.util.NestedResource;
import org.aksw.simba.lsq.util.SpinUtils;
import org.aksw.simba.lsq.util.WebLogParser;
import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.simba.lsq.vocab.PROV;
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
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.vocabulary.SP;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

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
                .acceptsAll(Arrays.asList("m", "format"), "Format of the input data. Available options: " + WebLogParser.getFormatRegistry().keySet())
                .withOptionalArg()
                .defaultsTo("apache")
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




        OptionSet options = parser.parse(args);

        // Write to file or sysout depending on arguments
        PrintStream out;
        File outFile = null;
        if(options.has(outputOs)) {
            outFile = outputOs.value(options);
            out = new PrintStream(outFile);
        } else {
            out = System.out;
        }

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
        String expBaseUri = expBaseUriOs.value(options);
        String logFormat = logFormatOs.value(options);
        String outFormatStr = outFormatOs.value(options);

        RDFFormat outFormat = RDFWriterRegistry.registered().stream().filter(f -> f.toString().equals(outFormatStr)).findFirst().orElse(null);
        if(outFormat == null) {
            throw new RuntimeException("No Jena writer found for name: " + outFormatStr);
        }

        expBaseUri = expBaseUri == null ? baseUri + datasetLabel : expBaseUri;


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

        WebLogParser webLogParser = WebLogParser.getFormatRegistry().get(logFormat);
        if(webLogParser == null) {
            throw new RuntimeException("No log format parser found for '" + logFormat + "'");
        }
        //WebLogParser webLogParser = new WebLogParser(WebLogParser.apacheLogEntryPattern);

        // TODO Use zipWithIndex in order to make the index part of the resource
        Stream<Resource> workloadResourceStream = stream
                .map(line -> {
                Resource r = logModel.createResource();
                r.addLiteral(RDFS.label, line);
                boolean parsed = webLogParser.parseEntry(line, r);
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






//        System.out.println(queryToSubmissions.keySet().size());

//        logger.info("Number of distinct queries in log: "

        // This is an abstraction that can execute SPARQL queries
        QueryExecutionFactory countQef =
                FluentQueryExecutionFactory
                .http(endpointUrl, graph)
                .create();

        Cache<String, byte[]> queryCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .build();


        QueryExecutionFactory dataQef =
                FluentQueryExecutionFactory
                .http(endpointUrl, graph)
                .config()
                    .withPostProcessor(qe -> {
                        if(timeoutInMs != null) {
                            ((QueryEngineHTTP)((QueryExecutionHttpWrapper)qe).getDecoratee())
                            .setTimeout(timeoutInMs);
                        }
                    })
                    .withCache(new CacheFrontendImpl(new CacheBackendMem(queryCache)))
//                    .withRetry(3, 30, TimeUnit.SECONDS)
//                    .withPagination(1000)
                .end()
                .create();

//            RiotLib.writeBase(out, base) ;
//            RiotLib.writePrefixes(out, prefixMap) ;
//            ShellGraph x = new ShellGraph(graph, null, null) ;
//            x.writeGraph() ;

        logger.info("Counting triples in the endpoint ...");
        long datasetSize = QueryExecutionUtils.countQuery(QueryFactory.create("SELECT * { ?s ?p ?o }"), countQef);

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


        Set<Query> executedQueries = new HashSet<>();

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
        int logFailCount[] = new int[] {0};
        long logEntryIndex[] = new long[] {0l};
        int batchSize = 10;

        // TODO We should check beforehand whether there is a sufficient number of processable log entries
        // available in order to consider the workload a query log
        //for(Resource r : workloadResources) {
        workloadResourceStream.forEach(r -> {
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

	                    if(logEntryIndex[0] % batchSize == 0) {
	                        long batchEndTmp = logEntryIndex[0] + batchSize;
	                        long batchEnd = workloadSize == null ? batchEndTmp : Math.min(batchEndTmp, workloadSize);
	                        logger.info("Processing query batch from " + logEntryIndex[0] + " - "+ batchEnd); // + ": " + queryStr.replace("\n", " ").substr);
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


	                    if(rdfizer.contains("e")) {
	                        boolean hasBeenExecuted = executedQueries.contains(query);

	                        if(!hasBeenExecuted) {
	                            executedQueries.add(query);


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


	                    RDFDataMgr.write(out, queryModel, outFormat);

	                    // TODO Frequent flushing may decrease performance
	                    // out.flush();
	            	} else {
	            		logger.debug("Skipping non-sparql-query log entry #" + logEntryIndex[0]);
	                    logger.debug(toString(r));
	            	}

                } else {
                    ++logFailCount[0];
                    double ratio = logEntryIndex[0] == 0 ? 0.0 : logFailCount[0] / logEntryIndex[0];
                    if(logEntryIndex[0] == 10 && ratio > 0.8) {
                        fail = true;
                    }

                    logger.warn("Skipping non-parsable log entry #" + logEntryIndex[0]);
                    logger.warn(toString(r));
                }

            } catch(Exception e) {
                logger.warn("Unexpected exception encountered at item " + logEntryIndex[0] + " - ", e);
                logger.warn(toString(r));
            }

            if(fail) {
                throw new RuntimeException("Encountered too many non processable log entries. Probably not a log file.");
            }

            //.write(System.err, "TURTLE");
            ++logEntryIndex[0];
        });


        Model tmpModel = ModelFactory.createDefaultModel();
        expRes.inModel(tmpModel)
            .addLiteral(PROV.endAtTime, Calendar.getInstance());

        RDFDataMgr.write(out, tmpModel, outFormat); //RDFFormat.TURTLE_BLOCKS);


        out.flush();

        // If the output stream is based on a file then close it, but
        // don't close stdout
        if(outFile != null) {
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


    public static void rdfizeQueryExecution(Resource queryRes, Query query, Resource queryExecRes, QueryExecutionFactory qef, long datasetSize) {

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


            SpinUtils.enrichModelWithTriplePatternSelectivities(queryRes, queryExecRes, qef, datasetSize); //subModel, resultSetSize);

            //  queryStats = queryStats + " lsqv:meanTriplePatternSelectivity "+Selectivity.getMeanTriplePatternSelectivity(query.toString(),localEndpoint,graph,endpointSize)  +" ; \n ";
            //long resultSize = QueryExecutionUtils.countQuery(query, dataQef);
            //long resultSize = this.getQueryResultSize(queryNew.toString(), localEndpoint,"select");
        }
        catch(Exception e) {
            String msg = e.getMessage();
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
