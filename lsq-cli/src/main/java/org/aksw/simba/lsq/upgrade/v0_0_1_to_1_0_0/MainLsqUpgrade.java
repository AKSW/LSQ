package org.aksw.simba.lsq.upgrade.v0_0_1_to_1_0_0;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.server.utils.FactoryBeanSparqlServer;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class MainLsqUpgrade {

    private static final Logger logger = LoggerFactory.getLogger(MainLsqUpgrade.class);
//
//    public static String extractDatasetLabel(String filename) {
//        Pattern pattern = Pattern.compile("lsq.(\\w*)");
//
//        Matcher m = pattern.matcher(filename);
//        String result = null;
//        if(m.find()) {
//            result = m.group(1);
//        }
//
//        return result;
//    }
//
//    public static void main(String[] args) throws Exception {
//        File folder = new File("/home/raven/Projects/Eclipse/lsq-parent/unsorted-files/trash/upgrade-test/");
//
//        System.out.println(extractDatasetLabel("lsq-affymetrix-sorted.zip"));
//
//    }

//
//    public static void main(String[] args) throws Exception {
//
//    }

    public static void main(String[] args) throws Exception {
        //runUpgrade(args);
        runServer();
    }

    public static void runServer() throws Exception {
        QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://localhost:8950/sparql").create();

        Server server = FactoryBeanSparqlServer.newInstance().setSparqlServiceFactory(qef)
                .create();

        logger.info("Starting server");
        server.start();
        server.join();

    }


    public static void runUpgrade(String[] args) {
        //Model model = ModelFactory.createDefaultModel();

        String datasetUrl = args[0];
        if(datasetUrl == null) {
            throw new RuntimeException("URL / Filename of LSQ dataset to upgrade expected as only argument");
        }


        Model model = RDFDataMgr.loadModel(datasetUrl);
        //String datasetLabel = args[1];

        logger.info("Loaded data");


        //String datasetLabel;

        SparqlService ss = FluentSparqlService.from(model).create();

        //System.out.println(ServiceUtils.fetchInteger(ss.getQueryExecutionFactory().createQueryExecution("PREFIX lsq: <http://lsq.aksw.org/vocab#> SELECT (COUNT(*) AS ?x) { ?s lsq:hasRemoteExecution ?o  }"), Vars.x));


        ss.getUpdateExecutionFactory().createUpdateProcessor("PREFIX lsq: <http://lsq.aksw.org/vocab#> DELETE { ?s lsq:executionError ?o } INSERT {  ?s lsq:execError ?o } WHERE { ?s lsq:executionError ?o }").execute();
        logger.info("Updated.");


        ss.getUpdateExecutionFactory().createUpdateProcessor("PREFIX lsq: <http://lsq.aksw.org/vocab#> DELETE { ?s lsq:hasTriplePatternExecution ?o } INSERT {  ?s lsq:hasTPExec ?o } WHERE { ?s lsq:hasTriplePatternExecution ?o }").execute();
        ss.getUpdateExecutionFactory().createUpdateProcessor("PREFIX lsq: <http://lsq.aksw.org/vocab#> DELETE { ?s lsq:hasLocalExecution  ?o } INSERT {  ?s lsq:hasLocalExec ?o } WHERE { ?s lsq:hasLocalExecution ?o }").execute();
        ss.getUpdateExecutionFactory().createUpdateProcessor("PREFIX lsq: <http://lsq.aksw.org/vocab#> DELETE { ?s lsq:hasRemoteExecution ?o } INSERT {  ?s lsq:hasRemoteExec ?o } WHERE { ?s lsq:hasRemoteExecution ?o }").execute();

        ss.getUpdateExecutionFactory().createUpdateProcessor("PREFIX lsq: <http://lsq.aksw.org/vocab#> DELETE { ?s lsq:triplePatternSelectivity ?o } INSERT {  ?s lsq:tpSel ?o } WHERE { ?s lsq:triplePatternSelectivity ?o }").execute();
        ss.getUpdateExecutionFactory().createUpdateProcessor("PREFIX lsq: <http://lsq.aksw.org/vocab#> DELETE { ?s lsq:triplePatternResultSize ?o } INSERT {  ?s lsq:resultSize ?o } WHERE { ?s lsq:triplePatternResultSize ?o }").execute();

        ss.getUpdateExecutionFactory().createUpdateProcessor("PREFIX lsq: <http://lsq.aksw.org/vocab#> PREFIX sp: <http://spinrdf.org/sp#> DELETE { ?s a sp:Query } INSERT {  ?s a lsq:Query } WHERE { ?s a sp:Query }").execute();
        ss.getUpdateExecutionFactory().createUpdateProcessor("PREFIX lsq: <http://lsq.aksw.org/vocab#> PREFIX prov: <http://www.w3.org/ns/prov#> DELETE { ?s lsq:wasAssociatedWith ?o } INSERT { ?s prov:wasAssociatedWith ?o } WHERE { ?s lsq:wasAssociatedWith ?o }").execute();
        ss.getUpdateExecutionFactory().createUpdateProcessor("PREFIX lsq: <http://lsq.aksw.org/vocab#> DELETE { ?s lsq:hasTriplePattern ?o } INSERT {  ?s lsq:hasTP ?o } WHERE { ?s lsq:hasTriplePattern ?o }").execute();


        // lsq:hasSpin
        //"WITH <http://lsq.aksw.org/>",

        logger.info("running delete query...");
        ss.getUpdateExecutionFactory().createUpdateProcessor(String.join("\n",
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
            "PREFIX sp: <http://spinrdf.org/sp#>",
            "PREFIX lsq: <http://lsq.aksw.org/vocab#>",

            "DELETE { ?s ?p ?o }",
            "WHERE {",
//              "?x (lsq:hasStructuralFeatures|lsq:hasSpin)/((!rdf:type)*) ?s .",
            "?x lsq:hasStructuralFeatures/((!rdf:type)*) ?s .",
              "?s ?p ?o",
            "}")).execute();

        ss.getUpdateExecutionFactory().createUpdateProcessor("PREFIX lsq: <http://lsq.aksw.org/vocab#> DELETE WHERE { ?s lsq:hasStructuralFeatures ?o }").execute();

        logger.info("running delete query 2...");
        ss.getUpdateExecutionFactory().createUpdateProcessor(String.join("\n",
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
            "PREFIX sp: <http://spinrdf.org/sp#>",
            "PREFIX lsq: <http://lsq.aksw.org/vocab#>",

            "DELETE { ?s ?p ?o }",
            "WHERE {",
//              "?x (lsq:hasStructuralFeatures|lsq:hasSpin)/((!rdf:type)*) ?s .",
            "?x lsq:hasSpin/((!rdf:type)*) ?s .",
              "?s ?p ?o",
            "}")).execute();

        ss.getUpdateExecutionFactory().createUpdateProcessor("PREFIX lsq: <http://lsq.aksw.org/vocab#> DELETE WHERE { ?s lsq:hasSpin ?o }").execute();

        //      !(rdf:type, lsq:hasTPExec, lsq:hasBGPExec, lsq:hasJoinVarExec)*
//      "FILTER(?p ! In (lsq:hasTPExec))",
//        "BIND('http://lsq.aksw.org/res/' AS ?resourcePrefix)",
//        "BIND(str(?s) AS ?ss)",
//      "FILTER(STRSTARTS(?ss, ?resourcePrefix))",


        //System.out.println(ServiceUtils.fetchInteger(ss.getQueryExecutionFactory().createQueryExecution("PREFIX lsq: <http://lsq.aksw.org/vocab#> SELECT (COUNT(*) AS ?x) { ?s lsq:hasRemoteExecution ?o  }"), Vars.x));
//        System.out.println(ServiceUtils.fetchInteger(ss.getQueryExecutionFactory().createQueryExecution("PREFIX lsq: <http://lsq.aksw.org/vocab#> SELECT (COUNT(*) AS ?x) { ?s ?p ?o }"), Vars.x));

//new FileOutputStream("/tmp/output.ttl")
        RDFDataMgr.write(System.out, model, RDFFormat.NTRIPLES);

//        Server server = FactoryBeanSparqlServer.newInstance()
//            .setSparqlServiceFactory(ss.getQueryExecutionFactory())
//            .create();
//
//        logger.info("Starting server");
//        server.start();
//        server.join();
    }


}
