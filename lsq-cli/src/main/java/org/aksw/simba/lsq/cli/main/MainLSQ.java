package org.aksw.simba.lsq.cli.main;

import java.io.IOException;
import java.util.Calendar;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.aksw.simba.lsq.core.LsqConfigImpl;
import org.aksw.simba.lsq.core.LsqProcessor;
import org.aksw.simba.lsq.core.LsqUtils;
import org.aksw.simba.lsq.util.NestedResource;
import org.aksw.simba.lsq.vocab.PROV;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.DatasetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main class of LSQ's command line interface (CLI)
 * used to RDFise query logs
 * 
 * @author Saleem
 * @author Claus Stadler
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
        LsqConfigImpl config = cliParser.parse(args);

        try {
            run(config);
        } catch(Exception e) {
            logger.error("Error", e);
            cliParser.getOptionParser().printHelpOn(System.err);
            throw new RuntimeException(e);
        }
    }

    public static void run(LsqConfigImpl config) throws Exception  {

        SparqlServiceReference ssr = config.getBenchmarkEndpointDescription();
        String datasetEndpointUrl = ssr.getServiceURL();
        DatasetDescription datasetDescription = ssr.getDatasetDescription();
        Long datasetSize = config.getDatasetSize();

        String expBaseIri = config.getExperimentIri();

//        Stream<Resource> logEntryStream;

        // The main setup work is done in LsqUtils following.
        // It follows a classic batch processing approach:
        // Create a reader, a processor and a writer
        Stream<Resource> itemReader = LsqUtils.createReader(config);
        LsqProcessor itemProcessor = LsqUtils.createProcessor(config);
        Sink<Resource> itemWriter = LsqUtils.createWriter(config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> itemReader.close()));

        datasetSize = itemProcessor.getDatasetSize();
        // Precounting the workload size is quite expensive
        // TODO Add a parameter + implementation do the counting anyway
        Long workloadSize = null;

        logger.info("About to process " + workloadSize + " queries");
        logger.info("Dataset size of " + datasetEndpointUrl + " / " + DatasetDescriptionUtils.toString(datasetDescription) + " - size: " + datasetSize);

        NestedResource expBaseRes = new NestedResource(ResourceFactory.createResource(expBaseIri));

      //  Resource expRes = expBaseRes.nest("-" + expStartStr).get();
        Resource expRes = expBaseRes.get();   //we do not need to nest the expStartStr

        // Report start / end times of the RDFization if requested
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
}
