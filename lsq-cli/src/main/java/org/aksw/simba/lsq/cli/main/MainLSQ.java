package org.aksw.simba.lsq.cli.main;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.beast.vocabs.PROV;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.simba.lsq.util.Mapper;
import org.aksw.simba.lsq.util.NestedResource;
import org.aksw.simba.lsq.util.WebLogParser;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
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

    public static void main(String[] args) throws Exception  {
        SpringApplication.run(LsqConfig.class, args);
    }

    public static void config(String[] args) {
        Map<String, Mapper> logFmtRegistry = WebLogParser.loadRegistry(RDFDataMgr.loadModel("default-log-formats.ttl"));

        LsqCliParser cliParser = new LsqCliParser(logFmtRegistry);

        LsqConfig config = cliParser.parse(args);


        Stream<Resource> logEntryStream;

        try {
            run(args);
        } catch(Exception e) {
            logger.error("Error", e);
            cliParser.getOptionParser().printHelpOn(System.err);
            throw new RuntimeException(e);
        }
    }

    public static void run(
            Stream<Resource> itemReader,
            Function<Resource, Resource> itemProcessor,
            Consumer<Resource> itemWriter) throws Exception  {
        itemReader
            .map(itemProcessor)
            .filter(x -> x != null)
            .forEach(itemWriter::accept);
            //.forEach(r -> RDFDataMgr.write(out, queryModel, outFormat));
    }

    public LsqConfig parseLsqConfig(String[] args) {



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


}
