package org.aksw.simba.lsq.cli.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmtQuery;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.simba.lsq.core.LSQARQ2SPIN;
import org.aksw.simba.lsq.core.QueryStatistics2;
import org.aksw.simba.lsq.util.ApacheLogParserUtils;
import org.aksw.simba.lsq.util.ElementVisitorFeatureExtractor;
import org.aksw.simba.lsq.util.NestedResource;
import org.aksw.simba.lsq.util.SpinUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.simba.lsq.vocab.PROV;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.topbraid.spin.system.SPINModuleRegistry;

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


    public static final PrefixMapping lsqPrefixes;

    static {
        try {
            ClassPathResource r = new ClassPathResource("lsq-prefixes.ttl");
            Model m = ModelFactory.createDefaultModel();
            m.read(r.getInputStream(), "http://example.org/base/", "turtle");
            lsqPrefixes = m;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws Exception  {
        // Try to start - if something goes wrong print help
        // TODO Logic for when help is displayed could be improved
        try {
            run(args);
        } catch(Exception e) {
            logger.error("Error", e);
            parser.printHelpOn(System.err);
        }
    }

    public static void run(String[] args) throws Exception  {

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

        OptionSpec<String> formatOs = parser
                .acceptsAll(Arrays.asList("m", "format"), "Format of the input data. Apache log COMBINED format assumed by default.")
                .withOptionalArg()
                .defaultsTo("apache")
                ;

        OptionSpec<String> endpointUrlOs = parser
                .acceptsAll(Arrays.asList("e", "endpoint"), "SPARQL endpoint URL")
                .withRequiredArg()
                .defaultsTo("http://localhost:8890/sparql")
                ;

        OptionSpec<String> graphUriOs = parser
                .acceptsAll(Arrays.asList("g", "graph"), "Graph(s) from which to retrieve the data")
                .withRequiredArg()
                ;

        OptionSpec<String> datasetLabelOs = parser
                .acceptsAll(Arrays.asList("l", "label"), "Label of the dataset, such as 'dbpedia' or 'lgd'. Will be used in URI generation")
                .withRequiredArg()
                .defaultsTo("mydata")
                ;

        OptionSpec<String> baseUriOs = parser
                .acceptsAll(Arrays.asList("b", "base"), "Base URI for URI generation")
                .withRequiredArg()
                .defaultsTo("http://example.org/resource/")
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
            in = new FileInputStream(file);
        } else {
            in = System.in;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        String datasetLabel = datasetLabelOs.value(options);
        String endpointUrl = endpointUrlOs.value(options);
        List<String> graph = graphUriOs.values(options);

//        These lines are just kept for reference in case we need something fancy
//        JOptCommandLinePropertySource clps = new JOptCommandLinePropertySource(options);
//        ApplicationContext ctx = SpringApplication.run(ConfigLSQ.class, args);


        SPINModuleRegistry.get().init();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar startTime = new GregorianCalendar();

        Date date = startTime.getTime();
        String prts []  = dateFormat.format(date).split(" ");


        // Note: We re-use the baseGeneratorRes in every query's model, hence its not bound to the specs model directly
        // However, with .inModel(model) we can create resources that are bound to a specific model from another resource
        Resource baseGeneratorRes = ResourceFactory.createResource(LSQ.defaultLsqrNs + datasetLabel + "-" + prts[0]);


        Model specs = ModelFactory.createDefaultModel();
        Resource generatorRes = baseGeneratorRes.inModel(specs);


        // TODO Attempt to determine attributes automatically ; or merge this data from a file or something
        Resource engineRes = specs.createResource()
                .addProperty(LSQ.vendor, specs.createResource(LSQ.defaultLsqrNs + "Virtuoso"))
                .addProperty(LSQ.version, "Virtuoso v.7.2")
                .addProperty(LSQ.processor, "2.5GHz i7")
                .addProperty(LSQ.ram,"8GB");

        generatorRes
            .addProperty(LSQ.engine, engineRes);


        Resource datasetRes = specs.createResource();
        generatorRes
            .addLiteral(LSQ.dataset, datasetRes)
            .addLiteral(PROV.hadPrimarySource, specs.createResource(endpointUrl))
            .addLiteral(PROV.startedAtTime, startTime);


        Model logModel = ModelFactory.createDefaultModel();
        List<Resource> workloadResources = reader.lines()
                .map(line -> {
                Resource r = logModel.createResource();
                ApacheLogParserUtils.parseEntry(line, r);
                return r;
            })
            .collect(Collectors.toList());

//        System.out.println(queryToSubmissions.keySet().size());

//        logger.info("Number of distinct queries in log: "

        // This is an abstraction that can execute SPARQL queries
        QueryExecutionFactory dataQef =
                FluentQueryExecutionFactory
                .http(endpointUrl, graph)
                .create();

//            RiotLib.writeBase(out, base) ;
//            RiotLib.writePrefixes(out, prefixMap) ;
//            ShellGraph x = new ShellGraph(graph, null, null) ;
//            x.writeGraph() ;


        //rdfizer.rdfizeLog(out, generatorRes, queryToSubmissions, dataQef, separator, localEndpoint, graph, acronym);


        Calendar endTime = new GregorianCalendar();
        specs.add(datasetRes, PROV.startedAtTime, specs.createTypedLiteral(endTime));

        specs.write(out, "NTRIPLES");

        //SparqlQueryParser queryParser = SparqlQueryParserImpl.create(Syntax.syntaxARQ);
        SparqlStmtParser stmtParser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, true);


        for(Resource r : workloadResources) {
            logger.info("Processing: " + r);
            //Model m = ResourceUtils.reachableClosure(r);
            SparqlStmt stmt = Optional.ofNullable(r.getProperty(LSQ.query))
                .map(queryStmt -> queryStmt.getString())
                .map(stmtParser)
                .orElse(null);

            if(stmt != null && stmt.isQuery()) {
                SparqlStmtQuery queryStmt = stmt.getAsQueryStmt();
                Query query = stmt.getAsQueryStmt().getQuery();

                Model queryModel = ModelFactory.createDefaultModel();

                //NestedResource baseRes = new NestedResource(generatorRes).nest(datasetLabel).nest("-");
                NestedResource baseRes = new NestedResource(queryModel.createResource(LSQ.defaultLsqrNs));

                String queryHash = StringUtils.md5Hash(query.toString()).substring(0, 8);
                NestedResource queryRes = baseRes.nest("q" + queryHash);


                //Resource queryRes = model.createResource(LSQ.defaultLsqrNs + "q" + queryHash);

                // The query resource represents the query and only depends on the (normalized) query string

                // Function to create resources that correspond to aspects of a query
//                Function<String, NestedResource> nsToQueryRes = (ns) -> baseRes.nest(ns).nest("q" + queryHash);
//                NestedResource queryRes = baseRes.nest("q" + queryHash);

                // Function for creating resources that correspond to aspects of the query
                // Pattern: http://base.org/res/{aspect}-q{queryHash}
                Function<String, NestedResource> queryAspectFn = (aspect) -> baseRes.nest(aspect).nest("q" + queryHash);


                // Create a resource with a certain namespace
//                Function<String, NestedResource> nsToDataQueryRes = (ns) -> baseRes.nest(ns).nest(datasetLabel + "-").nest("q" + queryHash);

                rdfizeQuery(queryRes.get(), queryAspectFn, query);

                System.out.println("STATUS OF " + queryRes.get());
                queryRes.get().getModel().write(System.out, "TURTLE");
            }

            //.write(System.err, "TURTLE");
        }



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
    public static void rdfizeQueryExecution(
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



    // QueryExecutionFactory dataQef
    public static void rdfizeQuery(Resource queryRes, Function<String, NestedResource> queryAspectFn, Query query) {

        Resource execRes = queryAspectFn.apply("exec").nest("-execX").get();

        try {
            query = query.cloneQuery();
            query.getGraphURIs().clear();

//          queryNo++;

        // .. generate the spin model ...
            Model spinModel = queryRes.getModel();
          LSQARQ2SPIN arq2spin = new LSQARQ2SPIN(spinModel);
          Resource tmpQueryRes = arq2spin.createQuery(query, null);

          // ... and rename the blank node of the query
          ResourceUtils.renameResource(tmpQueryRes, queryRes.getURI());


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

            queryRes.addProperty(LSQ.structuralFeatures, featureRes);


            QueryStatistics2.enrichResourceWithQueryFeatures(queryRes, query);


            // Add used features
            Set<Resource> features = ElementVisitorFeatureExtractor.getFeatures(query);
            features.forEach(f -> featureRes.addProperty(LSQ.usesFeature, f));

            // TODO These methods have to be ported
            //queryStats = queryStats+ QueryStatistics.getDirectQueryRelatedRDFizedStats(query.toString()); // Query type, total triple patterns, join vertices, mean join vertices degree
            //queryStats = queryStats+QueryStatistics.rdfizeTuples_JoinVertices(query.toString());

            SpinUtils.enrichModelWithHasTriplePattern(queryRes);
            SpinUtils.enrichModelWithTriplePatternText(queryRes);
            //Selectivity2.enrichModelWithTriplePatternExtensionSizes(model, dataQef);
            QueryStatistics2.getDirectQueryRelatedRDFizedStats(queryRes);

        //	queryStats = queryStats + " lsqv:meanTriplePatternSelectivity "+Selectivity.getMeanTriplePatternSelectivity(query.toString(),localEndpoint,graph,endpointSize)  +" ; \n ";
            long curTime = System.currentTimeMillis();
            //long resultSize = QueryExecutionUtils.countQuery(query, dataQef);
            //long resultSize = this.getQueryResultSize(queryNew.toString(), localEndpoint,"select");
            long resultSize = 0;
            long exeTime = System.currentTimeMillis() - curTime ;

            queryRes
                .addLiteral(LSQ.resultSize, resultSize)
                .addLiteral(LSQ.runTimeMs, exeTime);

        } catch (Exception ex) {
            String msg = ExceptionUtils.getFullStackTrace(ex);//ex.getMessage();
            execRes.addLiteral(LSQ.runtimeError, msg);
        }

        // TODO Add getRDFUserExecutions
    }
}