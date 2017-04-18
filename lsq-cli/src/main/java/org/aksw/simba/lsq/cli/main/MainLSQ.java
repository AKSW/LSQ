package org.aksw.simba.lsq.cli.main;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.beast.vocabs.PROV;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.simba.lsq.util.NestedResource;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
/**
 * This is the main class used to RDFise query logs
 * @author Saleem
 *
 */
@SpringApplicationConfiguration
public class MainLSQ
{

    private static final Logger logger = LoggerFactory.getLogger(MainLSQ.class);

//    public static void main(String[] args) throws Exception  {
//        SpringApplication.run(LsqConfig.class, args);
//    }

    public static void main(String[] args) throws IOException {

        LsqCliParser cliParser = new LsqCliParser();
        LsqConfig config = cliParser.parse(args);

        try {
            run(config);
        } catch(Exception e) {
            logger.error("Error", e);
            cliParser.getOptionParser().printHelpOn(System.err);
            throw new RuntimeException(e);
        }
    }

    public static void run(LsqConfig config) throws Exception  {

        String datasetEndpointUrl = config.getDatasetEndpointIri();
        List<String> datasetDefaultGraphIris = config.getDatasetDefaultGraphIris();
        Long datasetSize = config.getDatasetSize();

        String expBaseIri = config.getExperimentIri();


        Stream<Resource> logEntryStream;


        Stream<Resource> itemReader = LsqCliParser.createReader(config);
        Function<Resource, Resource> itemProcessor = LsqCliParser.createProcessor(config);
        Sink<Resource> itemWriter = LsqCliParser.createWriter(config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> itemReader.close()));

        Long workloadSize = null;

        logger.info("About to process " + workloadSize + " queries");
        logger.info("Dataset size of " + datasetEndpointUrl + " / " + datasetDefaultGraphIris + " - size: " + datasetSize);

        Calendar expStart = Calendar.getInstance();

        NestedResource expBaseRes = new NestedResource(ResourceFactory.createResource(expBaseIri));

      //  Resource expRes = expBaseRes.nest("-" + expStartStr).get();
        Resource expRes = expBaseRes.get();   //we do not need to nest the expStartStr
        expRes
          //  .addProperty(PROV.wasAssociatedWith, expBaseRes.get())
            .addLiteral(PROV.startedAtTime, expStart);

        //RDFDataMgr.write(out, expModel, outFormat);

        itemWriter.send(expRes);


        itemReader
            .map(itemProcessor)
            .filter(x -> x != null)
            .forEach(itemWriter::send);

        Resource tmp = expRes.inModel(ModelFactory.createDefaultModel())
            .addLiteral(PROV.endAtTime, Calendar.getInstance());

        itemWriter.send(tmp);

        itemWriter.flush();
        itemWriter.close();
    }

    public void parseLsqConfig(String[] args) {



//        System.out.println(queryToSubmissions.keySet().size());

//        logger.info("Number of distinct queries in log: "

        // This is an abstraction that can execute SPARQL queries


//            RiotLib.writeBase(out, base) ;
//            RiotLib.writePrefixes(out, prefixMap) ;
//            ShellGraph x = new ShellGraph(graph, null, null) ;
//            x.writeGraph() ;


        //int workloadSize = workloadResources.size();

        //rdfizer.rdfizeLog(out, generatorRes, queryToSubmissions, dataQef, separator, localEndpoint, graph, acronym);


        //Calendar endTime = new GregorianCalendar();
        //specs.add(datasetRes, PROV.startedAtTime, specs.createTypedLiteral(endTime));

        //specs.write(out, "NTRIPLES");

        //SparqlQueryParser queryParser = SparqlQueryParserImpl.create(Syntax.syntaxARQ);


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
//        Iterator<Resource> it = workloadResourceStream.iterator();
//        while(it.hasNext()) {
//            Resource r = it.next();
//
//        }

    }


}
