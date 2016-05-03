package org.aksw.simba.lsq.core;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.simba.benchmark.encryption.EncryptUtils;
import org.aksw.simba.benchmark.log.operations.DateConverter.DateParseException;
import org.aksw.simba.benchmark.log.operations.SesameLogReader;
import org.aksw.simba.benchmark.spin.Spin;
import org.aksw.simba.largerdfbench.util.QueryStatistics;
import org.aksw.simba.largerdfbench.util.QueryStatistics2;
import org.aksw.simba.largerdfbench.util.Selectivity;
import org.aksw.simba.largerdfbench.util.Selectivity2;
import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.simba.lsq.vocab.PROV;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.ResourceUtils;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.system.SPINModuleRegistry;
/**
 * This is the main class used to RDFise query logs
 * @author Saleem
 *
 */
public class LogRDFizer {

    private static final Logger logger = LoggerFactory.getLogger(LogRDFizer.class);


    public static BufferedWriter 	bw ;
    public  RepositoryConnection con = null;
    public static  BufferedWriter tobw= null;
    public static long queryNo = 1;
    public  static int  maxRunTime ;  //max query execution time in seconds
    public int runtimeErrorCount;
    public long endpointSize = 0;
    public static  String publicEndpoint ;
    public static String acronym  ; // a short acrnym for the query log e.g. SWDF for semantic web dog food
    public static long queryHash=0 ;  //hash of the query
    //public static String generatedBy ; // a URI showing the specs for the local experiements
    public static void main(String[] args) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, ParseException, DateParseException {


        SPINModuleRegistry.get().init();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar startTime = new GregorianCalendar();

        Date date = startTime.getTime();
        String prts []  = dateFormat.format(date).split(" ");


        // Note: We re-use the generatorRes in every query's model, hence its not bound to the specs model directly
        Resource generatorRes = ResourceFactory.createResource(LSQ.defaultLsqrNs + acronym + "-" + prts[0]);

        Model specs = ModelFactory.createDefaultModel();

        Resource engineRes = specs.createResource();
        specs.add(generatorRes, LSQ.engine, engineRes);
        specs.add(engineRes, LSQ.vendor, specs.createResource(LSQ.defaultLsqrNs + "Virtuoso"));
        specs.add(engineRes, LSQ.version, "Virtuoso v.7.2");
        specs.add(engineRes, LSQ.processor, "2.5GHz i7");
        specs.add(engineRes, LSQ.ram,"8GB");

        Resource datasetRes = specs.createResource();
        specs.add(generatorRes, LSQ.dataset, datasetRes);
        specs.add(datasetRes, PROV.hadPrimarySource, specs.createResource(publicEndpoint));
        specs.add(datasetRes, PROV.startedAtTime, specs.createTypedLiteral(startTime));


        String queryLogDir = "/home/raven/Projects/LSQ/SWDF-Test/";

        //String queryLogDir = "D:/QueryLogs/SWDF-Test/";  //dont forget last /
         // String queryLogDir = "/home/MuhammadSaleem/dbpedia351logs/";
        //  String queryLogDir = "D:/QueryLogs/USEWOD2014/data/LinkedGeoData/";
        // String queryLogDir = "D:/QueryLogs/RKBExplorer/";

         acronym = "SWDF" ; //  a short acronym of the dataset

         String localEndpoint = "http://localhost:8890/sparql";
        // String localEndpoint = "http://linkedgeodata.org/sparql";

        String graph = "http://aksw.org/benchmark"; //can be null
        //String graph = "http://linkedgeodata.org"; //can be null
        //String graph = null;

        String outputFile = "LSQ-SWDF-test.ttl";
        //String outputFile = "LinkedDBpedia351SQL.ttl";
        //String outputFile = "Linked-SQ-DBpedia-Fixed.ttl";

         publicEndpoint = "http://data.semanticweb.org/sparql";
        //String publicEndpoint = "http://dbpedia.org/sparql";
        //String publicEndpoint = "http://linkedgeodata.org/sparql";

        maxRunTime = 900;
        tobw = new BufferedWriter(new FileWriter("timeOutQueries.txt"));
        String separator = "- -";   // this is separator which separates  the agent ip and corresponding exe time. can be null if there is no user I.P provided in log
        //String separator = null;  //null is when IP is missing. like in BM

        SesameLogReader slr = new SesameLogReader();
        // DBpediaLogReader dblr = new DBpediaLogReader();
        //RKBExplorerLogReader rkblr = new RKBExplorerLogReader();
        //LinkedGeoDataLogReader lglr = new LinkedGeoDataLogReader();

        LogRDFizer rdfizer = new LogRDFizer();

        Map<String, Set<String>> queryToSubmissions = slr.getSesameQueryExecutions(queryLogDir);  // this map contains a query as key and their all submissions
    //	Map<String, Set<String>> queryToSubmissions = dblr.getVirtuosoQueryExecutions(queryLogDir);  // this map contains a query as key and their all submissions
    //	Map<String, Set<String>> queryToSubmissions = rkblr.getBritishMuseumQueryExecutions(queryLogDir);
    //	Map<String, Set<String>> queryToSubmissions = lglr.getVirtuosoQueryExecutions(queryLogDir);

        System.out.println(queryToSubmissions.keySet().size());

        System.out.println("Number of Distinct queries: " +  queryToSubmissions.keySet().size());

        OutputStream out = new FileOutputStream(outputFile);

        // This is an abstraction that can execute SPARQL queries
        QueryExecutionFactory dataQef =
                FluentQueryExecutionFactory
                .http(localEndpoint, graph)
                .create();

        rdfizer.rdfizeLog(out, generatorRes, queryToSubmissions, dataQef, separator, localEndpoint, graph, acronym);


        Calendar endTime = new GregorianCalendar();
        specs.add(datasetRes, PROV.startedAtTime, specs.createTypedLiteral(endTime));

        specs.write(out, "NTRIPLES");

        out.close();
        out.flush();

        System.out.println("Dataset stored at " + outputFile);
    }
    private static String getCurTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String prts []  = dateFormat.format(date).split(" ");
        String dateStr = prts[0];
        String timeStr = prts[1];
        String dateTimeStr = dateStr+"T"+timeStr+ "+01:00";
        return dateTimeStr;
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


            queryHash = queryStr.hashCode();

            Resource itemRes = model.createResource(LSQ.defaultLsqrNs + "q" + queryHash);



            model.add(itemRes, PROV.wasGeneratdBy, generatorRes);
            model.add(itemRes, LSQ.text, queryStr.trim());

            Resource localExecutionRes = model.createResource(LSQ.defaultLsqrNs + "le-" + acronym + "-q" + queryHash);
            model.add(itemRes, LSQ.hasLocalExecution, localExecutionRes);

            Resource remoteExecutionRes = model.createResource(LSQ.defaultLsqrNs + "re-" + acronym + "-q" + queryHash);
            model.add(itemRes, LSQ.hasRemoteExecution, remoteExecutionRes);

                    //queryStats = queryStats+"\nlsqr:q"+queryHash+" lsqv:hasLocalExecution lsqr:le-"+acronym+"-q"+queryHash+" . " ;

            System.out.println(queryNo + " Started...");

            queryNo++;
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
                model.add(itemRes, LSQ.runtimeError, msg);

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
                    this.RDFizeSelect(model, itemRes, query, dataQef, queryToSubmissions.get(queryStr), separator);
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

        logger.info("Total Number of Queries with Parse Errors: "
                + parseErrorCount);
        logger.info("Total Number of Queries with Runtime Errors: "
                + runtimeErrorCount);
    }

//    private static void writeSpecs(String ep, String endpointVersion, String ram, String processor, String publicEndpoint, String startTime) throws IOException {
//
//        String dateTimeStr = getCurTime();
    //	System.out.println(dateTimeStr);
//		curData = System
//                bw.write(generatedBy + " \nlsqv:engine \n    [ lsqv:vendor lsqr:"+ep+ " ; lsqv:version \"" + endpointVersion+"\" ; lsqv:processor \"" + processor+"\" ; lsqv:ram \"" + ram+"\"] ; \n");
//                bw.write("lsqv:dataset \n   [ prov:hadPrimarySource <"+publicEndpoint+"> ; \n  prov:atTime \""+dateTimeStr+"\"^^xsd:dateTimeStamp ] ;\n");
//                bw.write( "prov:startedAtTime \""+startTime+"\"^^xsd:dateTimeStamp ; \n");
//                bw.write( "prov:endAtTime \""+dateTimeStr+"\"^^xsd:dateTimeStamp . \n");
//		 lsqv:engine
//		   [ lsqv:vendor lsqr:Virtuoso  ; lsqv:version "Virtuoso v.123/1239" ] ;
//		 lsqv:dataset
//		   [ prov:hadPrimarySource <http:/data.semanticweb.org/linktodump> ;
//		     prov:atTime "2014-12-12T..."^^xsd:dateTimeStamp ] ;
//		 prov:startedAtTime "2015-12-12T..."^^xsd:dateTimeStamp ;
//		 prov:endedAtTime "2015-12-12T..."^^xsd:dateTimeStamp .
//
//    }

    /**
     * RDFized SELECT query
     * @param query Query
     * @param localEndpoint Local endpoint
     * @param graph Named Graph, can be null
     * @param submissions List of all submissions (I.P:ExecutionTime) of the given query
     * @param separator Separator string between I.P and execution time
     * @throws IOException
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws ParseException
     * @throws QueryEvaluationException
     */
    public void RDFizeSelect(Model model, Resource itemRes, Query query, QueryExecutionFactory dataQef, Set<String> submissions, String separator) throws IOException, RepositoryException, MalformedQueryException, ParseException, QueryEvaluationException {

        try {
            Query queryNew = SesameLogReader.removeNamedGraphs(query);


            //queryStats = queryStats+" lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash ;
            Resource featureRes = model.createResource(LSQ.defaultLsqrNs + "sf-q" + "TODO");//lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash
            model.add(itemRes, LSQ.structuralFeatures, featureRes);

            QueryStatistics2.enrichResourceWithQueryFeatures(itemRes, query);


            // Add used features
            Set<Resource> features = ElementVisitorFeature.getFeatures(query);
            features.forEach(f -> model.add(featureRes, LSQ.usesFeature, f));

            // TODO These methods have to be ported
            //queryStats = queryStats+ QueryStatistics.getDirectQueryRelatedRDFizedStats(query.toString()); // Query type, total triple patterns, join vertices, mean join vertices degree
            //queryStats = queryStats+QueryStatistics.rdfizeTuples_JoinVertices(query.toString());

            Selectivity2.enrichModelWithHasTriplePattern(model, itemRes);
            Selectivity2.enrichModelWithTriplePatternText(model);
            Selectivity2.enrichModelWithTriplePatternExtensionSizes(model, dataQef);


        //	queryStats = queryStats + " lsqv:meanTriplePatternSelectivity "+Selectivity.getMeanTriplePatternSelectivity(query.toString(),localEndpoint,graph,endpointSize)  +" ; \n ";
            long curTime = System.currentTimeMillis();
            long resultSize = QueryExecutionUtils.countQuery(queryNew, dataQef);
            //long resultSize = this.getQueryResultSize(queryNew.toString(), localEndpoint,"select");
            long exeTime = System.currentTimeMillis() - curTime ;

            model.add(itemRes, LSQ.resultSize, model.createTypedLiteral(resultSize));
            model.add(itemRes, LSQ.runTimeMs, model.createTypedLiteral(exeTime));

        } catch (Exception ex) {
            String msg = ExceptionUtils.getFullStackTrace(ex);//ex.getMessage();
            model.add(itemRes, LSQ.runtimeError, msg);
        }

//
//            String runtimeError = ex.getMessage().toString().replace("\"", "'").replaceAll("\n", " ").replace("\r", "");
//        bw.write(" lsqv:runtimeError \""+runtimeError+ "\" . ");
//        runtimeErrorCount++; }
//        queryStats = this.getRDFUserExecutions(submissions,separator);
//        bw.write(queryStats);
//        queryStats = this.getSpinRDFStats(query);
//        bw.write(queryStats);
    }
    /**
     * RDFized DESCRIBE query
     * @param query Query
     * @param localEndpoint Local endpoint
     * @param graph Named Graph, can be null
     * @param submissions List of all submissions (I.P:ExecutionTime) of the given query
     * @param separator Separator string between I.P and execution time
     * @throws IOException
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws ParseException
     * @throws QueryEvaluationException
     */
    public void RDFizeDescribe(Query query, String localEndpoint, String graph, Set<String> submissions, String separator) throws IOException, RepositoryException, MalformedQueryException, ParseException, QueryEvaluationException {
        String queryStats ="";
        try {
            Query queryNew = SesameLogReader.removeNamedGraphs(query);
            queryStats =queryStats+" lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash ;
            queryStats = queryStats+ QueryStatistics.getDirectQueryRelatedRDFizedStats(query.toString()); // Query type, total triple patterns, join vertices, mean join vertices degree
            queryStats =queryStats+"\nlsqr:q"+query.toString().hashCode()+" lsqv:hasLocalExecution lsqr:le-"+acronym+"-q"+queryHash+" . \n lsqr:le-"+acronym+"-q"+queryHash ;			queryStats = queryStats + " lsqv:meanTriplePatternSelectivity "+Selectivity.getMeanTriplePatternSelectivity(query.toString(),localEndpoint,graph,endpointSize)  +" ; \n ";
            long curTime = System.currentTimeMillis();
            long resultSize = this.getQueryResultSize(queryNew.toString(), localEndpoint,"describe");
            long exeTime = System.currentTimeMillis() - curTime ;
            queryStats = queryStats + " lsqv:resultSize "+resultSize  +" ; ";
            queryStats = queryStats+" lsqv:runTimeMs "+exeTime  +" . \n";
            queryStats = queryStats+QueryStatistics.getRDFizedQueryStats(query,localEndpoint,graph,endpointSize, queryStats);
            bw.write(queryStats);

        } catch (Exception ex) {String runtimeError = ex.getMessage().toString().replace("\"", "'").replaceAll("\n", " ").replace("\r", "");
        bw.write(" lsqv:runtimeError \""+runtimeError+ "\" . ");
        runtimeErrorCount++; }
        queryStats = QueryStatistics.rdfizeTuples_JoinVertices(query.toString());
        bw.write(queryStats);
        queryStats = this.getRDFUserExecutions(submissions,separator);
        bw.write(queryStats);
        queryStats = this.getSpinRDFStats(query);
        bw.write(queryStats);
    }
    /**
     * RDFized CONSTRUCT query
     * @param query Query
     * @param localEndpoint Local endpoint
     * @param graph Named Graph, can be null
     * @param submissions List of all submissions (I.P:ExecutionTime) of the given query
     * @param separator Separator string between I.P and execution time
     * @throws IOException
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws ParseException
     * @throws QueryEvaluationException
     */
    public void RDFizeConstruct(Query query, String localEndpoint, String graph, Set<String> submissions, String separator) throws IOException, RepositoryException, MalformedQueryException, ParseException, QueryEvaluationException {
        String queryStats ="";
        try {
            Query queryNew = SesameLogReader.removeNamedGraphs(query);
            queryStats =queryStats+" lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash ;
            queryStats = queryStats+ QueryStatistics.getDirectQueryRelatedRDFizedStats(query.toString()); // Query type, total triple patterns, join vertices, mean join vertices degree
            queryStats =queryStats+"\nlsqr:q"+query.toString().hashCode()+" lsqv:hasLocalExecution lsqr:le-"+acronym+"-q"+queryHash+" . \n lsqr:le-"+acronym+"-q"+queryHash ;			queryStats = queryStats + " lsqv:meanTriplePatternSelectivity "+Selectivity.getMeanTriplePatternSelectivity(query.toString(),localEndpoint,graph,endpointSize)  +" ; \n ";
            long curTime = System.currentTimeMillis();
            long resultSize = this.getQueryResultSize(queryNew.toString(), localEndpoint,"construct");
            long exeTime = System.currentTimeMillis() - curTime ;
            queryStats = queryStats + " lsqv:resultSize "+resultSize  +" ; ";
            queryStats = queryStats+" lsqv:runTimeMs "+exeTime  +" . \n";
            queryStats = queryStats+QueryStatistics.getRDFizedQueryStats(query,localEndpoint,graph,endpointSize, queryStats);
            bw.write(queryStats);

        } catch (Exception ex) {String runtimeError = ex.getMessage().toString().replace("\"", "'").replaceAll("\n", " ").replace("\r", "");
        bw.write(" lsqv:runtimeError \""+runtimeError+ "\" . ");
        runtimeErrorCount++; }
        queryStats = QueryStatistics.rdfizeTuples_JoinVertices(query.toString());
        bw.write(queryStats);
        queryStats = this.getRDFUserExecutions(submissions,separator);
        bw.write(queryStats);
        queryStats = this.getSpinRDFStats(query);
        bw.write(queryStats);
    }
    /**
     * RDFized ASK query
     * @param query Query
     * @param localEndpoint Local endpoint
     * @param graph Named Graph, can be null
     * @param submissions List of all submissions (I.P:ExecutionTime) of the given query
     * @param separator Separator string between I.P and execution time
     * @throws IOException
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws ParseException
     * @throws QueryEvaluationException
     */
    public void RDFizeASK(Query query, String localEndpoint, String graph, Set<String> submissions, String separator) throws IOException, RepositoryException, MalformedQueryException, ParseException, QueryEvaluationException {
        String queryStats ="";
        try {
            Query queryNew = SesameLogReader.removeNamedGraphs(query);
            queryStats =queryStats+" lsqv:structuralFeatures lsqr:sf-q"+queryHash+" . \n lsqr:sf-q"+queryHash ;
            queryStats = queryStats+ QueryStatistics.getDirectQueryRelatedRDFizedStats(query.toString()); // Query type, total triple patterns, join vertices, mean join vertices degree
            queryStats =queryStats+"\nlsqr:q"+query.toString().hashCode()+" lsqv:hasLocalExecution lsqr:le-"+acronym+"-q"+queryHash+" . \n lsqr:le-"+acronym+"-q"+queryHash ;
            queryStats = queryStats + " lsqv:meanTriplePatternSelectivity "+Selectivity.getMeanTriplePatternSelectivity(query.toString(),localEndpoint,graph,endpointSize)  +" ; \n ";
            long curTime = System.currentTimeMillis();
            long resultSize = this.getQueryResultSize(queryNew.toString(), localEndpoint,"ask");
            long exeTime = System.currentTimeMillis() - curTime ;
            queryStats = queryStats + " lsqv:resultSize "+resultSize  +" ; ";
            queryStats = queryStats+" lsqv:runTimeMs "+exeTime  +" . \n";
            queryStats = queryStats+QueryStatistics.getRDFizedQueryStats(query,localEndpoint,graph,endpointSize, queryStats);
            bw.write(queryStats);

        } catch (Exception ex) {String runtimeError = ex.getMessage().toString().replace("\"", "'").replaceAll("\n", " ").replace("\r", "");
        bw.write(" lsqv:runtimeError \""+runtimeError+ "\" . ");
        runtimeErrorCount++; }
        queryStats = QueryStatistics.rdfizeTuples_JoinVertices(query.toString());
        bw.write(queryStats);
        queryStats = this.getQueryTuples(query);
        queryStats = this.getRDFUserExecutions(submissions,separator);
        bw.write(queryStats);
        queryStats = this.getSpinRDFStats(query);
        bw.write(queryStats);
    }
    public String getQueryTuples(Query query) {

        return null;
    }
    /**
     * Get all executions (IP,Time) of the given query
     * @param query Query
     * @param submissions  Query submissions in form of IP:Time
     * @param separator String separator between IP:Time
     * @return Stats
     * @throws ParseException
     */
    public String getRDFUserExecutions(Set<String> submissions, String separator) throws ParseException {
        String queryStats = "\nlsqr:q"+(LogRDFizer.queryHash);
        queryStats = queryStats+ " lsqv:hasRemoteExecution ";
        int i = 0;
        for(String submission:submissions)
        {
            String prts [] = submission.split(separator);
            String timeStamp = prts[1].replace(":", "-").replace("+", "-");
            if(i<submissions.size()-1)
            {
                queryStats = queryStats + "lsqr:q"+queryHash+"-e-"+acronym+"-"+timeStamp+ " , ";
            }
            else
            {
                queryStats = queryStats + "lsqr:q"+queryHash+"-e-"+acronym+"-"+timeStamp+ " . \n ";
            }
            i++;
        }
        int j = 1;
        if(!(separator==null))  //i.e both I.P and Time is provided in log
        {
        for(String submission:submissions)
        {
            String prts [] = submission.split(separator);
            String timeStamp = prts[1].replace(":", "-").replace("+", "-");
            String txt=prts[0].replace(".", "useanystring") ; // of course we used different one
            String key="what is your key string?";  //of course we use different key in LSQ.
            txt=EncryptUtils.xorMessage( txt, key );
            String encoded=EncryptUtils.base64encode( txt );
            encoded = encoded.replace("=", "-");
            encoded = encoded.replace("+", "-");
               queryStats = queryStats + "lsqr:q"+queryHash+"-e-"+acronym+"-"+timeStamp+ " prov:wasAssociatedWith lsqr:A-"+encoded+"  ; prov:atTime \""+prts[1]+"\"^^xsd:dateTimeStamp ; lsqv:endpoint <" + publicEndpoint + "> . \n";

            j++;
        }
        }
        else  //only exe time is stored
        {
            for(String submission:submissions)
            {
                String timeStamp = submission.replace(":", "-").replace("+", "-");
                queryStats = queryStats + "lsqr:q"+queryHash+"-e-"+acronym+"-"+timeStamp+ " prov:wasAssociatedWith \""+submission+"\"^^xsd:dateTimeStamp ; lsqv:endpoint <" + publicEndpoint + "> . \n ";
                j++;
            }

        }
        return queryStats;

    }

    /**
     * Get Spin RDF stats of the qiven query
     * @param query Query
     * @return Spin Stas
     * @throws IOException
     */
    private String getSpinRDFStats(Query query) throws IOException {
        String queryStats = "";
        try {
            Spin sp = new Spin();
            String spinQuery = sp.getSpinRDF(query.toString(), "Turtle");
            String prefix = spinQuery.substring(0,spinQuery.indexOf("[")-1);
            String body = spinQuery.substring(spinQuery.indexOf("[")+1,spinQuery.lastIndexOf("]"));
            spinQuery = prefix+" lsqr:q"+queryHash+"  "+ body;
            //queryStats = queryStats+ "lsqr:q"+(LogRDFizer.queryNo-1);
            //queryStats = queryStats+" lsqv:spinQuery lsqr:q"+queryHash  +" . \n";
            queryStats = queryStats+spinQuery +" . \n";

        } catch (Exception ex) {
            String runtimeError = ex.getMessage().toString().replace("\"", "'").replaceAll("\n", " ").replace("\r", "");
            bw.write(" lsqr:q"+queryHash+" lsqv:spinError \""+runtimeError+ "\" . "); }
        return queryStats;
    }

    /**
     * Get result size of the given query
     * @param queryStr Query
     * @param localEndpoint Endpoint url where this query has to be executed
     * @param sesameQueryType Query type {SELECT, ASK, CONSTRUCT, DESCRIBE}
     * @return ResultSize
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws IOException
     */
    public long getQueryResultSize(String queryStr, String localEndpoint,String sesameQueryType) throws RepositoryException, MalformedQueryException, IOException
    {
        long totalSize = -1;
        this.initializeRepoConnection(localEndpoint);
        if(sesameQueryType.equals("select") || sesameQueryType.equals("ask") )
        {
            try {
                if (sesameQueryType.equals("select"))
                {
                    TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,queryStr );
                    //System.out.println(queryStr);
                    tupleQuery.setMaxQueryTime(maxRunTime);
                    TupleQueryResult res;
                    res = tupleQuery.evaluate();
                    //System.out.println(res);
                    totalSize = 0;
                    while(res.hasNext())
                    {
                        res.next();
                        totalSize++;
                    }
                }
                else
                {
                    BooleanQuery booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL,queryStr );
                    //System.out.println(queryStr);
                    booleanQuery.setMaxQueryTime(maxRunTime);
                    booleanQuery.evaluate();
                    //System.out.println(res);
                    totalSize = 1;

                }

            } catch (QueryEvaluationException ex) {
                String runtimeError = ex.getMessage().toString().replace("\"", "'").replaceAll("\n", " ").replace("\r", "");
                if(runtimeError.length()>1000)  //this is to avoid sometime too big errors
                    runtimeError = "Unknown runtime error";
                bw.write(" lsqv:runtimeError \""+runtimeError+ "\" ; ");
                runtimeErrorCount++;
            }
        }
        else
        {
            try {
                GraphQuery gq = con.prepareGraphQuery(QueryLanguage.SPARQL, queryStr);
                gq.setMaxQueryTime(maxRunTime);
                GraphQueryResult graphResult = gq.evaluate();
                totalSize = 0;
                while (graphResult.hasNext())
                {
                    graphResult.next();
                    totalSize++;
                }
            } catch (QueryEvaluationException ex) {
                String runtimeError = ex.getMessage().toString().replace("\"", "'").replaceAll("\n", " ").replace("\r", "");
                if(runtimeError.length()>1000)  //this is to avoid sometime too big errors
                    runtimeError = "Unknown runtime error";
                bw.write(" lsqv:runtimeError \""+runtimeError+ "\" ; ");
                runtimeErrorCount++;
            }

        }
        con.close();
        return totalSize;
    }
    /**
     * Write RDF Prefixes
     * @param acronym Acronym of the dataset e.g. DBpedia or SWDF
     * @throws IOException
     */
    public void writePrefixes(String acronym) throws IOException {
        bw.write("@prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n");
        bw.write("@prefix lsqr:<http://lsq.aksw.org/res/> . \n");
        //bw.write("@prefix lsqrd:<http://lsq.aksw.org/res/"+acronym+"-> . \n");
        bw.write("@prefix lsqv:<http://lsq.aksw.org/vocab#> . \n");
        bw.write("@prefix sp:<http://spinrdf.org/sp#> . \n");
        bw.write("@prefix void:<http://rdfs.org/ns/void#> . \n");
        bw.write("@prefix dct:<http://purl.org/dc/terms/> . \n");
        bw.write("@prefix prov:<http://www.w3.org/ns/prov#> . \n");
        bw.write("@prefix xsd:<http://www.w3.org/2001/XMLSchema#> . \n");
        bw.write("@prefix sd:<http://www.w3.org/ns/sparql-service-description#> . \n\n");


    }

    /**
     * Initialize repository for a SPARQL endpoint
     * @param endpointUrl Endpoint Url
     * @throws RepositoryException
     */
    public void initializeRepoConnection(String endpointUrl) throws RepositoryException {
        Repository repo = new SPARQLRepository(endpointUrl);
        repo.initialize();
        con = repo.getConnection();

    }

}