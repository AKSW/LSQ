package org.aksw.simba.lsq.cli.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import java.util.Map;
import java.util.Set;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.simba.lsq.core.LSQARQ2SPIN;
import org.aksw.simba.lsq.core.QueryStatistics2;
import org.aksw.simba.lsq.core.Selectivity2;
import org.aksw.simba.lsq.util.ApacheLogParserUtils;
import org.aksw.simba.lsq.util.ElementVisitorFeatureExtractor;
import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.simba.lsq.vocab.PROV;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.writer.TurtleWriter;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.topbraid.spin.system.SPINModuleRegistry;

import jena.schemagen.SchemagenOptions.OPT;
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


        // Note: We re-use the generatorRes in every query's model, hence its not bound to the specs model directly
        Resource generatorRes = ResourceFactory.createResource(LSQ.defaultLsqrNs + datasetLabel + "-" + prts[0]);

        Model specs = ModelFactory.createDefaultModel();

        // TODO Attempt to determine attributes automatically ; or merge this data from a file or something
        Resource engineRes = specs.createResource();
        generatorRes.addProperty(LSQ.engine, engineRes);
        engineRes.addProperty(LSQ.vendor, specs.createResource(LSQ.defaultLsqrNs + "Virtuoso"));
        engineRes.addProperty(LSQ.version, "Virtuoso v.7.2");
        engineRes.addProperty(LSQ.processor, "2.5GHz i7");
        engineRes.addProperty(LSQ.ram,"8GB");

        Resource datasetRes = specs.createResource();
        generatorRes.addLiteral(LSQ.dataset, datasetRes);
        datasetRes.addLiteral(PROV.hadPrimarySource, specs.createResource(endpointUrl));
        datasetRes.addLiteral(PROV.startedAtTime, startTime);


        Model logModel = ModelFactory.createDefaultModel();
        reader.lines()
            .map(line -> {
                Resource r = logModel.createResource();
                ApacheLogParserUtils.parseEntry(line, r);
                return r;
            });

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

        out.flush();

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
    public void rdfizeLog(
            OutputStream out,
            Resource generatorRes,
            Map<String, Set<String>> queryToSubmissions,
            QueryExecutionFactory dataQef,
            String separator,
            String localEndpoint, // TODO get rid of argument, and use dataQef for executing queries instead
            String graph, // TODO get rid of argument, and use dataQef for executing queries instead
            String acronym)  {

        logger.info("RDFization started...");
        long endpointSize = ServiceUtils.fetchInteger(dataQef.createQueryExecution("SELECT (COUNT(*) AS ?x) { ?s ?p ?o }"), Vars.x);

        // endpointSize = Selectivity.getEndpointTotalTriples(localEndpoint,
        // graph);
        long parseErrorCount = 0;

        // this.writePrefixes(acronym);
        // Resource executionRes =
        // model.createResource(lsqr:le-"+acronym+"-q"+queryHash);

        for (String queryStr : queryToSubmissions.keySet()) {

            Model model = ModelFactory.createDefaultModel();

            String queryHash = StringUtils.md5Hash(queryStr).substring(0, 8);

            Resource itemRes = model.createResource(LSQ.defaultLsqrNs + "q" + queryHash);

            itemRes.addProperty(PROV.wasGeneratedBy, generatorRes);
            itemRes.addProperty(LSQ.text, queryStr.trim());

            Resource localExecutionRes = model.createResource(LSQ.defaultLsqrNs + "le-" + acronym + "-q" + queryHash);
            itemRes.addProperty(LSQ.hasLocalExecution, localExecutionRes);

            Resource remoteExecutionRes = model.createResource(LSQ.defaultLsqrNs + "re-" + acronym + "-q" + queryHash);
            itemRes.addProperty(LSQ.hasRemoteExecution, remoteExecutionRes);

                    //queryStats = queryStats+"\nlsqr:q"+queryHash+" lsqv:hasLocalExecution lsqr:le-"+acronym+"-q"+queryHash+" . " ;

//            System.out.println(queryNo + " Started...");
//
//            queryNo++;
            Query query = new Query();
            try {
                // Parse the query ...
                query = QueryFactory.create(queryStr);

                // .. generate the spin model ...
                LSQARQ2SPIN arq2spin = new LSQARQ2SPIN(model);
                Resource queryRes = arq2spin.createQuery(query, null);

                // ... and rename the blank node of the query
                ResourceUtils.renameResource(queryRes, itemRes.getURI());

            } catch (Exception ex) {
                String msg = ex.getMessage();// ExceptionUtils.getFullStackTrace(ex);
                itemRes.addLiteral(LSQ.runtimeError, msg);

                // TODO Port the getRDFUserExceptions function
                // String queryStats =
                // this.getRDFUserExecutions(queryToSubmissions.get(queryStr),separator);
                parseErrorCount++;
            }
            try {
                if (query.isDescribeType()) {
//                    this.RDFizeDescribe(query, localEndpoint, graph,
//                            queryToSubmissions.get(queryStr), separator);
                } else if (query.isSelectType()) {
                    this.rdfizeQuery(model, itemRes, query, dataQef, queryToSubmissions.get(queryStr), separator);
                } else if (query.isAskType()) {
//                    this.RDFizeASK(query, localEndpoint, graph,
//                            queryToSubmissions.get(queryStr), separator);
                } else if (query.isConstructType()) {
//                    this.RDFizeConstruct(query, localEndpoint, graph,
//                            queryToSubmissions.get(queryStr), separator);
                }
            } catch (Exception ex) {
                throw new RuntimeException("Unhandled exception: ", ex);
            }


            model.write(System.out, "TURTLE");



            RDFDataMgr.write(out, model, RDFFormat.NTRIPLES);
        }

        // TODO Track and report parse and execution errors
//        logger.info("Total Number of Queries with Parse Errors: "
//                + parseErrorCount);
//        logger.info("Total Number of Queries with Runtime Errors: "
//                + runtimeErrorCount);
    }

    public void rdfizeQuery(Model model, Resource itemRes, Query query, QueryExecutionFactory dataQef, Set<String> submissions, String separator) {

        try {
            query = query.cloneQuery();
            query.getGraphURIs().clear();


            //queryStats = queryStats+" lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash ;
            Resource featureRes = model.createResource(LSQ.defaultLsqrNs + "sf-q" + "TODO");//lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash
            model.add(itemRes, LSQ.structuralFeatures, featureRes);

            QueryStatistics2.enrichResourceWithQueryFeatures(itemRes, query);


            // Add used features
            Set<Resource> features = ElementVisitorFeatureExtractor.getFeatures(query);
            features.forEach(f -> model.add(featureRes, LSQ.usesFeature, f));

            // TODO These methods have to be ported
            //queryStats = queryStats+ QueryStatistics.getDirectQueryRelatedRDFizedStats(query.toString()); // Query type, total triple patterns, join vertices, mean join vertices degree
            //queryStats = queryStats+QueryStatistics.rdfizeTuples_JoinVertices(query.toString());

            Selectivity2.enrichModelWithHasTriplePattern(model, itemRes);
            Selectivity2.enrichModelWithTriplePatternText(model);
            Selectivity2.enrichModelWithTriplePatternExtensionSizes(model, dataQef);


        //	queryStats = queryStats + " lsqv:meanTriplePatternSelectivity "+Selectivity.getMeanTriplePatternSelectivity(query.toString(),localEndpoint,graph,endpointSize)  +" ; \n ";
            long curTime = System.currentTimeMillis();
            long resultSize = QueryExecutionUtils.countQuery(query, dataQef);
            //long resultSize = this.getQueryResultSize(queryNew.toString(), localEndpoint,"select");
            long exeTime = System.currentTimeMillis() - curTime ;

            itemRes.addLiteral(LSQ.resultSize, resultSize);
            itemRes.addLiteral(LSQ.runTimeMs, exeTime);

        } catch (Exception ex) {
            String msg = ExceptionUtils.getFullStackTrace(ex);//ex.getMessage();
            itemRes.addLiteral(LSQ.runtimeError, msg);
        }

        // TODO Add getRDFUserExecutions
    }
}