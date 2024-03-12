package org.aksw.simba.lsq.core.rx.io.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.LongStream;

import org.aksw.commons.io.util.StdIo;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.dataset.impl.RDFNodeInDatasetUtils;
import org.aksw.jenax.arq.dataset.impl.ResourceInDatasetImpl;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrRx;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParserImpl;
import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.aksw.jenax.stmt.util.SparqlStmtIterator;
import org.aksw.simba.lsq.core.ResourceParser;
import org.aksw.simba.lsq.model.RemoteExecution;
import org.aksw.simba.lsq.parser.Mapper;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;

public class LsqRxIo {

    private static final Logger logger = LoggerFactory.getLogger(LsqRxIo.class);


    public static Flowable<ResourceInDataset> createSparqlStream(Callable<InputStream> inSupp) {
        String str;
        try {
            try(InputStream in = inSupp.call()) {
                // If the buffer gets completely filled, our input is too large
                byte[] buffer = new byte[16 * 1024 * 1024];
                int n = IOUtils.read(in, buffer);
                if(n == buffer.length) {
                    throw new RuntimeException("Input is too large for sparql stream; reached limit of " + buffer.length + " bytes");
                }
                str = new String(buffer, 0, n, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Note: Non-query statements will cause an exception
        Flowable<ResourceInDataset> result =
                Flowable.fromIterable(() -> new SparqlStmtIterator(SparqlStmtParserImpl.create(Syntax.syntaxARQ, true), str))
//    			.map(SparqlStmt::getOriginalString)
                .map(SparqlStmt::getAsQueryStmt)
                .map(SparqlStmtQuery::getQuery)
                .map(Object::toString)
                .map(queryStr ->
                    ResourceInDatasetImpl.createAnonInDefaultGraph()
                        .mutateResource(r -> r.addLiteral(LSQ.query, queryStr)));

        return result;
    }


    public static Flowable<ResourceInDataset> createResourceStreamFromMapperRegistry(Callable<InputStream> in, Function<String, Mapper> fmtSupplier, String fmtName) {
        Mapper mapper = fmtSupplier.apply(fmtName);
        if(mapper == null) {
            throw new RuntimeException("No mapper found for '" + fmtName + "'");
        }

        //BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        //Stream<String> stream = reader.lines();

        Flowable<String> flow = Flowable.generate(
                () -> {
                    InputStream tmp = in.call();
                    Objects.requireNonNull(tmp, "An InputStream supplier supplied null");
                    return new BufferedReader(new InputStreamReader(tmp, StandardCharsets.UTF_8));
                },
                (reader, emitter) -> {
                    String line = reader.readLine();
                    if(line != null) {
                        emitter.onNext(line);
                    } else {
                        emitter.onComplete();
                    }
                },
                BufferedReader::close);

        Flowable<ResourceInDataset> result = flow
                .map(line -> {
                    ResourceInDataset r = ResourceInDatasetImpl.createAnonInDefaultGraph();//ModelFactory.createDefaultModel().createResource();
                    r.addLiteral(LSQ.logRecord, line);

                    boolean parsed;
                    try {
                        parsed = mapper.parse(r, line) != 0;
                        if(!parsed) {
                            r.addLiteral(LSQ.processingError, "Failed to parse log line (no detailed information available)");
                        }
                    } catch(Exception e) {
                        parsed = false;
                        r.addLiteral(LSQ.processingError, "Failed to parse log line: " + e);
                        // logger.warn("Parser error", e);
                    }

                    return r;
                })
                ;

        return result;
    }


    /**
     * Method that creates a reader for a specific inputResource under the give config.
     * The config's inputResources are ignored.
     *
     * @param config
     * @param inputResource
     * @return
     * @throws IOException
     */
    public static Flowable<Resource> createReader(
            String logSource,
            String logFormat,
            Map<String, ResourceParser> logFmtRegistry,
            Function<Resource, Resource> rdfizer
            ) throws IOException {

//		String filename;
//		if(logSource == null) {
//			filename = "stdin";
//		} else {
//			Path path = Paths.get(logSource);
//			filename = path.getFileName().toString();
//		}

        Callable<InputStream> inSupp = logSource == null
                ? () -> StdIo.openStdInWithCloseShield()
                : () -> RDFDataMgr.open(logSource); // Alteratively StreamMgr.open()

        Lang lang = logFormat == null
                ? null
                : RDFLanguages.nameToLang(logFormat);

        if(lang == null) {
            lang = RDFDataMgr.determineLang(logSource, null, null);
        }

        Flowable<Resource> result = null;


        // Check if we are dealing with RDF
        if(lang != null) {
            // If quad based, use streaming
            // otherwise partition by lsq.text property
            if(RDFLanguages.isQuads(lang)) {
                // TODO Stream as datasets first, then select any resource with LSQ.text
                logger.info("Quad-based format detected - assuming RDFized log as input");
                result = RDFDataMgrRx.createFlowableDatasets(inSupp, lang, null)
                        .flatMap(ds -> Flowable.fromIterable(
                                RDFNodeInDatasetUtils.listResourcesWithProperty(ds, LSQ.text).toList()));

//        		result = Streams.stream(RDFDataMgrRx.createFlowableResources(inSupp, lang, "")
//        			.blockingIterable()
//        			.iterator());
            } else if(RDFLanguages.isTriples(lang)){
                logger.info("Triple-based format detected - assuming RDFized log as input");
                Model model = RDFDataMgr.loadModel(logSource, lang);
                result = null;
                throw new RuntimeException("Triple based format not implemented");
                //result = Flowable.fromIterable(() -> model.listSubjectsWithProperty(LSQ.text))
            }
//            else {
//                throw new RuntimeException("Unknown RDF input format; neither triples nor quads");
//            }

        }

        String effectiveLogFormat = null;

        // If the result is still null, probe for log formats
        if(result == null) {
            // Probe for known RDF or know log format
            logger.info("Processing log source " + logSource);

            if(Strings.isNullOrEmpty(logFormat)) {
                List<Entry<String, Number>> formats = LsqProbeUtils.probeLogFormat(logFmtRegistry, logSource);
                if(formats.isEmpty()) {
                    throw new RuntimeException("Could not auto-detect a log format for " + logSource);
                }

//    				if(formats.size() != 1) {
//    					throw new RuntimeException("Expected probe to return exactly 1 log format for source " + logSource + ", got: " + formats);
//    				}
                effectiveLogFormat = formats.get(0).getKey();
                logger.info("Auto-selected format [" + effectiveLogFormat + "] among auto-detected candidates " + formats);
            } else {
                effectiveLogFormat = logFormat;
            }

            ResourceParser webLogParser = logFmtRegistry.get(effectiveLogFormat);

            //Mapper webLogParser = config.getLogFmtRegistry().get(logFormat);
            if(webLogParser == null) {
                throw new RuntimeException("No log format parser found for '" + logFormat + "'");
            }

            result = webLogParser.parse(() -> RDFDataMgr.open(logSource))
                    .map(r -> r); // Turn ResourceInDataset to plain Resource


            // The webLogParser yields resources (blank nodes) for the log entry
            // First add a sequence id attribute
            // Then invert the entry:

            result = result
                .zipWith(LongStream.iterate(1, x -> x + 1)::iterator, Maps::immutableEntry)
                // Add the zipped index to the resource
                .map(e -> {
                    Resource r = e.getKey();
                    Long idx = e.getValue();

                    RemoteExecution re = r.as(RemoteExecution.class);
                    re.setSequenceId(idx);

                    return r;
                })
                .flatMapMaybe(record -> {
                    Maybe<Resource> r;
                    try {
                        r = Maybe.fromOptional(Optional.ofNullable(rdfizer.apply(record)));
                        // r = Maybe.fromOptional(LsqRdfizer.rdfizeLogRecord(sparqlStmtParser, baseIri, hostHashSalt, serviceUrl, hashFn, record));
                    } catch (Exception e) {
                        logger.warn("Internal error; trying to continue", e);
                        r = Maybe.empty();
                    }
                    return r;
                });
        }


        return result;
    }



    public static Flowable<ResourceInDataset> createResourceStreamFromRdf(Callable<InputStream> in, Lang lang, String baseIRI) {

        Flowable<ResourceInDataset> result = RDFDataMgrRx.createFlowableTriples(in, lang, baseIRI)
                .filter(t -> t.getPredicate().equals(LSQ.text.asNode()))
                .map(t -> {
                    ResourceInDataset r = ResourceInDatasetImpl.createInDefaultGraph(t.getSubject());
                    r.addLiteral(
                            LSQ.query, t.getObject().getLiteralValue());
                    return r;
                });

        return result;
    }
}
