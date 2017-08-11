package org.aksw.simba.lsq.cli.main;

import java.io.IOException;
import java.util.Calendar;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.beast.vocabs.PROV;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.simba.lsq.util.NestedResource;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.DatasetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * This is the main class used to RDFise query logs
 * @author Saleem
 *
 */
//@SpringApplicationConfiguration
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

        SparqlServiceReference ssr = config.getDatasetEndpointDescription();
        String datasetEndpointUrl = ssr.getServiceURL();
        DatasetDescription datasetDescription = ssr.getDatasetDescription();
        Long datasetSize = config.getDatasetSize();

        String expBaseIri = config.getExperimentIri();

//        Stream<Resource> logEntryStream;

        Stream<Resource> itemReader = LsqCliParser.createReader(config);
        Function<Resource, Resource> itemProcessor = LsqCliParser.createProcessor(config);
        Sink<Resource> itemWriter = LsqCliParser.createWriter(config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> itemReader.close()));

        Long workloadSize = null;

        logger.info("About to process " + workloadSize + " queries");
        logger.info("Dataset size of " + datasetEndpointUrl + " / " + DatasetDescriptionUtils.toString(datasetDescription) + " - size: " + datasetSize);

        NestedResource expBaseRes = new NestedResource(ResourceFactory.createResource(expBaseIri));

      //  Resource expRes = expBaseRes.nest("-" + expStartStr).get();
        Resource expRes = expBaseRes.get();   //we do not need to nest the expStartStr

        if(config.isEmitProcessMetadata()) {
            itemWriter.send(
                   expRes.inModel(ModelFactory.createDefaultModel())
                       //  .addProperty(PROV.wasAssociatedWith, expBaseRes.get())
                       .addLiteral(PROV.startedAtTime, Calendar.getInstance())
            );
        }

        //RDFDataMgr.write(out, expModel, outFormat);

        itemReader
            .map(itemProcessor)
            .filter(x -> x != null)
            .forEach(itemWriter::send);

        if(config.isEmitProcessMetadata()) {
            itemWriter.send(
                    expRes.inModel(ModelFactory.createDefaultModel())
                        //  .addProperty(PROV.wasAssociatedWith, expBaseRes.get())
                    .addLiteral(PROV.endAtTime, Calendar.getInstance())
            );
        }

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
