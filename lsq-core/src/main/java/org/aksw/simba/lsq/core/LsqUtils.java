package org.aksw.simba.lsq.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.util.bean.PropertyUtils;
import org.aksw.fedx.jsa.FedXFactory;
import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryExceptionCache;
import org.aksw.jena_sparql_api.cache.staging.CacheBackendMem;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.delay.extra.Delayer;
import org.aksw.jena_sparql_api.delay.extra.DelayerDefault;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtIterator;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmtQuery;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.aksw.simba.lsq.parser.Mapper;
import org.aksw.simba.lsq.parser.WebLogParser;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Streams;
import com.google.common.collect.TreeMultimap;

public class LsqUtils {
	private static final Logger logger = LoggerFactory.getLogger(LsqUtils.class);


	public static List<String> probeLogFormat(String resource) {
		ResourceLoader loader = new FileSystemResourceLoader();		
		Map<String, Function<InputStream, Stream<Resource>>> registry = LsqUtils.createDefaultLogFmtRegistry();
		
		List<String> result = probeLogFormat(registry, loader, resource);
//		Multimap<Long, String> report = probeLogFormatCore(registry, loader, resource);
//		
//		List<String> result = Streams.stream(report.asMap().entrySet().iterator())
//			.filter(e -> e.getKey() != 0)
////			.limit(2)
//			.map(Entry::getValue)
//			.flatMap(Collection::stream)
//			.collect(Collectors.toList());

		return result;
	}

	public static List<String> probeLogFormat(Map<String, Function<InputStream, Stream<Resource>>> registry, ResourceLoader loader, String resource) {
		
		Multimap<Long, String> report = probeLogFormatCore(registry, loader, resource);
		
		List<String> result = Streams.stream(report.asMap().entrySet().iterator())
			.filter(e -> e.getKey() != 0)
//			.limit(2)
			.map(Entry::getValue)
			.flatMap(Collection::stream)
			.collect(Collectors.toList());

		return result;
	}

	public static Multimap<Long, String> probeLogFormatCore(Map<String, Function<InputStream, Stream<Resource>>> registry, ResourceLoader loader, String filename) {
		org.springframework.core.io.Resource resource = loader.getResource(filename);
		
		// succcessCountToFormat
		Multimap<Long, String> result = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
		
		for(Entry<String, Function<InputStream, Stream<Resource>>> entry : registry.entrySet()) {
			String formatName = entry.getKey();
			
//			if(formatName.equals("wikidata")) {
//				System.out.println("here");
//			}
			
			Function<InputStream, Stream<Resource>> fn = entry.getValue();
			
			try(InputStream in = resource.getInputStream()) {
				//List<Resource> items =
				long count = fn.apply(in)
						.limit(1000)
						.filter(r -> !r.hasProperty(LSQ.processingError))
						.count();
						//.collect(Collectors.toList());
				
				result.put(count, formatName);
			} catch(Exception e) {
				// Ignore
			}
		}

		return result;
	}

	public static void applyDefaults(LsqConfigImpl config) {
		// Set the default log format registry if no other has been set
		PropertyUtils.applyIfAbsent(config::setLogFmtRegistry, config::getLogFmtRegistry, LsqUtils::createDefaultLogFmtRegistry);

		PropertyUtils.applyIfAbsent(config::setExperimentIri, config::getExperimentIri, () -> "http://example.org/unnamed-experiment");
		// If one connection has been set, use it for the other as well
		//PropertyUtils.applyIfAbsent(config::setBenchmarkConnection, config::getBenchmarkConnection, config::getDataConnection);
		//PropertyUtils.applyIfAbsent(config::setDataConnection, config::getDataConnection, config::getBenchmarkConnection);
	}

    public static Map<String, Function<InputStream, Stream<Resource>>> createDefaultLogFmtRegistry() {
        Map<String, Function<InputStream, Stream<Resource>>> result = new HashMap<>();

        // Load line based log formats
        LsqUtils.wrap(result, WebLogParser.loadRegistry(RDFDataMgr.loadModel("default-log-formats.ttl")));

        // Add custom RDF based log format(s)
        result.put("rdf", (in) -> LsqUtils.createResourceStreamFromRdf(in, Lang.NTRIPLES, "http://example.org/"));

        // Add multi-line sparql format
        result.put("sparql", (in) -> LsqUtils.createSparqlStream(in));
        
        return result;
    }
    
    public static Stream<Resource> createSparqlStream(InputStream in) {
    	String str;
		try {
			str = IOUtils.toString(in, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Note: Non-query statements will cause an exception
    	Stream<Resource> result = 
    			Streams.stream(new SparqlStmtIterator(SparqlStmtParserImpl.create(Syntax.syntaxARQ, true), str))
//    			.map(SparqlStmt::getOriginalString)
    			.map(SparqlStmt::getAsQueryStmt)
    			.map(SparqlStmtQuery::getQuery)
    			.map(Object::toString)
    			.map(queryStr -> ModelFactory.createDefaultModel().createResource().addLiteral(LSQ.query, queryStr)
    			);
    	
    	return result;
    }

	
    public static Sink<Resource> createWriter(LsqConfigImpl config) throws FileNotFoundException {
        String outRdfFormat = config.getOutRdfFormat();
        File outFile = config.getOutFile();

        RDFFormat rdfFormat = StringUtils.isEmpty(outRdfFormat)
                ? RDFFormat.TURTLE_BLOCKS
                :RDFWriterRegistry.registered().stream().filter(f -> f.toString().equalsIgnoreCase(outRdfFormat)).findFirst().orElse(null);
                        // : RDFWriterRegistry.getFormatForJenaWriter(outRdfFormat);
        if(rdfFormat == null) {
            throw new RuntimeException("No rdf format found for " + outRdfFormat);
        }

        PrintStream out;
        boolean doClose;

        if(outFile == null) {
            out = System.out;
            doClose = false;
        } else {
            out = new PrintStream(outFile);
            doClose = true;
        }

        Sink<Resource> result = new SinkIO<>(out, doClose, (o, r) -> RDFDataMgr.write(out, r.getModel(), rdfFormat));
        return result;
    }


    public static Stream<Resource> createResourceStreamFromMapperRegistry(InputStream in, Function<String, Mapper> fmtSupplier, String fmtName) {
        Mapper mapper = fmtSupplier.apply(fmtName);
        if(mapper == null) {
            throw new RuntimeException("No mapper found for '" + fmtName + "'");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        Stream<String> stream = reader.lines();

        Stream<Resource> result = stream
                .map(line -> {
                    Resource r = ModelFactory.createDefaultModel().createResource();
                    r.addLiteral(RDFS.label, line);

                    boolean parsed;
                    try {
                        parsed = mapper.parse(r, line) != 0;
                    } catch(Exception e) {
                        parsed = false;
                        logger.warn("Parser error", e);
                    }

                    if(!parsed) {
                        r.addLiteral(LSQ.processingError, "Failed to parse log line");
                    }

                    return r;
                })
                ;

        return result;
    }


    public static Stream<Resource> createResourceStreamFromRdf(InputStream in, Lang lang, String baseIRI) {
        Iterator<Triple> it = RDFDataMgr.createIteratorTriples(in, lang, baseIRI);

        // Filter out triples that do not have the right predicateS
        Stream<Triple> s = Streams.stream(it)
                .filter(t -> t.getPredicate().equals(LSQ.text.asNode()));

        Stream<Resource> result = s.map(t -> ModelUtils.convertGraphNodeToRDFNode(t.getSubject(), ModelFactory.createDefaultModel()).asResource()
                    .addLiteral(LSQ.query, t.getObject().getLiteralValue()));

        return result;
    }


    public static Map<String, Function<InputStream, Stream<Resource>>> wrap(Map<String, Function<InputStream, Stream<Resource>>> result, Map<String, Mapper> webLogParserRegistry) {
        Map<String, Function<InputStream, Stream<Resource>>> tmp = result == null
                ? new HashMap<>()
                : result;

        webLogParserRegistry.forEach((name, mapper) -> {
            Function<InputStream, Stream<Resource>> fn = in -> createResourceStreamFromMapperRegistry(in, webLogParserRegistry::get, name);
            tmp.put(name, fn);
        });

        return tmp;
    }


    public static Stream<Resource> createReader(LsqConfigImpl config) throws IOException {

    	List<String> inputResources = config.getInQueryLogFiles();
		logger.info("Input resources: " + inputResources);

//		for(String str : inputResources) {
//			PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(str);
//			//pathMatcher.
//			Files.walkFileTree(start, visitor)
//			
//		}
//		
    	Stream<Resource> result = inputResources.stream()
    			.flatMap(inputResource -> {
					try {
						return createReader(config, inputResource);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
    	
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
    public static Stream<Resource> createReader(LsqConfigImpl config, String inputResource) throws IOException {
        InputStream in;
        if(inputResource != null) {
        	// TODO We could make the resource loader part of the config
    		ResourceLoader loader = new DefaultResourceLoader();
    		org.springframework.core.io.Resource resource = loader.getResource(inputResource);
    		
    		// Retry with prepending file:
    		if(!resource.exists()) {
    			Path path = Paths.get(inputResource);
    			path = path.toAbsolutePath();
    			path = path.normalize();
    			logger.info("Attempting to open: [" + path + "]");
    			resource = new FileSystemResource(path.toFile());
    		}
    		
//            File inputFile = new File(inputResource);
//            inputFile = inputFile.getAbsoluteFile();
            in = resource.getInputStream();
        } else {
            in = System.in;
        }
        
        boolean doClose = in != System.in;


        Long firstItemOffset = config.getFirstItemOffset();
        String logFormat = config.getInQueryLogFormat();

        Function<InputStream, Stream<Resource>> webLogParser = config.getLogFmtRegistry().get(logFormat);

        //Mapper webLogParser = config.getLogFmtRegistry().get(logFormat);
        if(webLogParser == null) {
            throw new RuntimeException("No log format parser found for '" + logFormat + "'");
        }

//        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

//        Stream<String> stream = reader.lines();

        Stream<Resource> result = webLogParser.apply(in);
        if(firstItemOffset != null) {
            result = result.limit(firstItemOffset);
        }

        result = postProcessStream(result, in, doClose);
        
        //Model logModel = ModelFactory.createDefaultModel();


        //WebLogParser webLogParser = new WebLogParser(WebLogParser.apacheLogEntryPattern);

        // TODO Use zipWithIndex in order to make the index part of the resource
//        Stream<Resource> result = stream
//            .map(line -> {
//                Resource r = ModelFactory.createDefaultModel().createResource();
//                r.addLiteral(RDFS.label, line);
//
//                boolean parsed;
//                try {
//                    parsed = webLogParser.parse(r, line) != 0;
//                } catch(Exception e) {
//                    parsed = false;
//                    logger.warn("Parser error", e);
//                }
//
//                if(!parsed) {
//                    r.addLiteral(LSQ.processingError, "Failed to parse log line");
//                }
//
//                return r;
//            })
//            ;
//            .onClose(() -> {
//                try { reader.close(); } catch (IOException e) { throw new RuntimeException(e); }
//            });

//        result.onClose(() -> {
//            try {
//                reader.close();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });

//        result.onClose(() ->
//            if(outNeedsClosing[0]) {
//                logger.info("Shutdown hook: Flushing output");
//                out.flush();
//                out.close();
//            }
//        });

        return result;
    }

	public static Stream<Resource> postProcessStream(Stream<Resource> result, InputStream in, boolean doClose) {
		// Enrich potentially missing information
        result = Streams.mapWithIndex(result, (r, i) -> {
        	if(!r.hasProperty(LSQ.host)) {
        		// TODO Potentially make host configurable
        		r.addLiteral(LSQ.host, "localhost");
        	}

// Note: Avoid instantiating new dates as this breaks determinacy
//        	if(!r.hasProperty(PROV.atTime)) {
//        		r.addLiteral(PROV.atTime, new GregorianCalendar());
//        	}
        	
        	if(!r.hasProperty(LSQ.sequenceId)) {
        		r.addLiteral(LSQ.sequenceId, i);
        	}
        	
        	return r;
        }).onClose(() -> {
			try {
				if(doClose) {
					in.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		return result;
	}
	
	public static SparqlStmtParser createSparqlParser(List<String> prefixSources) {
        Iterable<String> sources = Iterables.concat(
        		Collections.singleton("rdf-prefixes/prefix.cc.2019-12-17.jsonld"),
        		Optional.ofNullable(prefixSources).orElse(Collections.emptyList())); 

        PrefixMapping prefixMapping = new PrefixMappingImpl();
        for(String source : sources) {
        	PrefixMapping tmp = RDFDataMgr.loadModel(source);
        	prefixMapping.setNsPrefixes(tmp);
        }
        
        SparqlStmtParser result = SparqlStmtParserImpl.create(
        		Syntax.syntaxARQ, prefixMapping, true);

        return result;
	}

	
    public static void postProcessSparqlStmt(Resource r, Function<String, SparqlStmt> sparqlStmtParser) {

        // logger.debug(RDFDataMgr.write(out, dataset, lang););

        // Extract the query and add it with the lsq:query property
        WebLogParser.extractQuery(r);

        // If the resource is null, we could not parse the log entry
        // therefore count this as an error

        boolean parsed = r.getProperty(LSQ.processingError) == null ? true : false;

        if(parsed) {
            Optional<String> str = Optional.ofNullable(r.getProperty(LSQ.query))
                    .map(queryStmt -> queryStmt.getString());

            //Model m = ResourceUtils.reachableClosure(r);
            SparqlStmt stmt = str
                    .map(sparqlStmtParser)
                    .orElse(null);
            
            if(stmt != null && stmt.isQuery()) {

                SparqlStmtUtils.optimizePrefixes(stmt);

                SparqlStmtQuery queryStmt = stmt.getAsQueryStmt();

                Query query = queryStmt.getQuery();
                String queryStr = "" + queryStmt.getQuery();
                r.addLiteral(LSQ.text, queryStr);
            }
        }
    }

    public static LsqProcessor createProcessor(LsqConfigImpl config) {

        LsqProcessor result = new LsqProcessor();

//        Function<String, SparqlStmt> sparqlStmtParser = config.getSparqlStmtParser();
        //SparqlParserConfig sparqlParserConfig = SparqlParserConfig.create().create(S, prologue)
//        sparqlStmtParser = sparqlStmtParser != null ? sparqlStmtParser : SparqlStmtParserImpl.create(Syntax.syntaxARQ, PrefixMapping2.Extended, true);

        // By default, make a snapshot of prefix.cc prefixes available
        // CollectionUtils.emptyIfNull would be nice to have here
        Function<String, SparqlStmt> sparqlStmtParser = createSparqlParser(config.getPrefixSources());


        SparqlServiceReference benchmarkEndpointDescription = config.getBenchmarkEndpointDescription();
        Long datasetSize = config.getDatasetSize();
        //String localDatasetEndpointUrl = config.getLocalDatasetEndpointUrl()
        //List<String> datasetDefaultGraphIris = config.getDatasetDefaultGraphIris();
        boolean isFetchDatasetSizeEnabled = config.isFetchDatasetSizeEnabled();

        boolean isRdfizerQueryExecutionEnabled = config.isRdfizerQueryExecutionEnabled();
        List<String> fedEndpoints = config.getFederationEndpoints();
        //String benchmarkEndpointUrl = benchmarkEndpointDescription.getServiceURL();
        Long queryTimeoutInMs = config.getBenchmarkQueryExecutionTimeoutInMs();
        String baseIri = config.getOutBaseIri();


        SparqlServiceReference datasetEndpointDescription = config.getDatasetEndpointDescription();
        String datasetEndpointUri = datasetEndpointDescription == null ? null : datasetEndpointDescription.getServiceURL();

        //Resource datasetEndpointRes = datasetEndpointUrl == null ? null : ResourceFactory.createResource(datasetEndpointUrl);


        Cache<String, byte[]> queryCache = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .build();

        Cache<String, Exception> exceptionCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .build();

        Cache<String, Object> seenQueryCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .build();
        
        // How to deal with recurrent queries in the log?
        // TODO QueryExecutionTime cache
        // I suppose we just skip further remote executions in that case 

        // Characteristics of used qefs:
        // baseBenchmarkQef: non-caching, no or long timeouts
        // benchmarkQef: non-caching, short timeouts
        // countQef: caching, long timeouts
        // dataQef: caching, timeout
        
        QueryExecutionFactory baseBenchmarkQef = config.getBenchmarkConnection() == null ? null : new QueryExecutionFactorySparqlQueryConnection(config.getBenchmarkConnection());        
        QueryExecutionFactory benchmarkQef = null;
        
        QueryExecutionFactory countQef = config.getDataConnection() == null ? null : new QueryExecutionFactorySparqlQueryConnection(config.getDataConnection());
        QueryExecutionFactory cachedDataQef = null;

        Function<String, Query> sparqlParser = SparqlQueryParserImpl.create();
        
        Long delayInMs = config.getDelayInMs();
        Delayer delayer = delayInMs == null || delayInMs.equals(0) ? null : new DelayerDefault(delayInMs);

        // Function used mainly to skip benchmark execution of queries that have already been seen 
//        Function<String, Boolean> isQueryCached = (queryStr) ->
//        	queryCache.getIfPresent(queryStr) != null || exceptionCache.getIfPresent(queryStr) != null;
        
        
        if(isRdfizerQueryExecutionEnabled) {
            boolean isNormalMode = fedEndpoints.isEmpty();
            //boolean isFederatedMode = !isNormalMode;

            if(isNormalMode) {
            	if(baseBenchmarkQef == null) {
	                baseBenchmarkQef = FluentQueryExecutionFactory
	                		.http(benchmarkEndpointDescription)
	                		.create();
            	}
            	
//                countQef = baseBenchmarkQef;
//                        FluentQueryExecutionFactory
//                        .http(benchmarkEndpointDescription)
//                        .create();

            } else {
                //countQef = null;

                baseBenchmarkQef = FedXFactory.create(fedEndpoints);
            }
            
            benchmarkQef =
                    FluentQueryExecutionFactory
                    //.http(endpointUrl, graph)
                    .from(baseBenchmarkQef)
                    .config()
                        .withParser(sparqlParser)
                        .withPostProcessor(qe -> {
                            if(queryTimeoutInMs != null) {
                                qe.setTimeout(queryTimeoutInMs, queryTimeoutInMs);
    //                            ((QueryEngineHTTP)((QueryExecutionHttpWrapper)qe).getDecoratee())
    //                            .setTimeout(timeoutInMs);
                            }
                        })
                        //.onTimeout((qef, queryStmt) -> )
                        //.withCache(new CacheFrontendImpl(new CacheBackendMem(queryCache)))
                        //.compose(qef ->  new QueryExecutionFactoryExceptionCache(qef, exceptionCache))
                        //)
    //                    .withRetry(3, 30, TimeUnit.SECONDS)
    //                    .withPagination(1000)
                    .end()
                    .create();
            
            if(countQef == null) {

                countQef = FluentQueryExecutionFactory
                        .from(baseBenchmarkQef)
                        .config()
                            .withParser(sparqlParser)
//                            .withPostProcessor(qe -> {
//                                if(queryTimeoutInMs != null) {
//                                    qe.setTimeout(queryTimeoutInMs, queryTimeoutInMs);
//        //                            ((QueryEngineHTTP)((QueryExecutionHttpWrapper)qe).getDecoratee())
//        //                            .setTimeout(timeoutInMs);
//                                }
//                            })
                            .withDelay(delayer)
                            .withCache(new CacheBackendMem(queryCache))
                            .compose(qef ->  new QueryExecutionFactoryExceptionCache(qef, exceptionCache))
                        .end()
                        .create();	
            }
            
            
            // The cached qef is used for querys needed for statistics
            // In this case, the performance is not benchmarked
            cachedDataQef =
                    FluentQueryExecutionFactory
                    //.http(endpointUrl, graph)
                    .from(countQef)
                    .config()
                        .withPostProcessor(qe -> {
                            if(queryTimeoutInMs != null) {
                                qe.setTimeout(queryTimeoutInMs, queryTimeoutInMs);
                            }
                        })
                    .end()
                    .create();

//            for(int i = 0; i < 1000; ++i) {
//                int x = i % 10;
//                String qs = "Select count(*) { ?s" + i + " ?p ?o }";
//                QueryExecution qe = dataQef.createQueryExecution(qs);
//                System.out.println("loop " + i + ": " + ResultSetFormatter.asText(qe.execSelect()));
//                qe.close();
//            }

            if(isFetchDatasetSizeEnabled) {
                logger.info("Counting triples in the endpoint ...");
                datasetSize = countQef == null ? null : QueryExecutionUtils.countQuery(QueryFactory.create("SELECT * { ?s ?p ?o }"), countQef);
            }
        }
        
        
        result.setDelayer(delayer);

        result.setDatasetLabel(config.getDatasetLabel());
        result.setRdfizerQueryStructuralFeaturesEnabled(config.isRdfizerQueryStructuralFeaturesEnabled());
        result.setRdfizerQueryLogRecordEnabled(config.isRdfizerQueryLogRecordEnabled());
        result.setRdfizerQueryExecutionEnabled(config.isRdfizerQueryExecutionEnabled());
        //result.setQueryExecutionRemote(config.isQueryExecutionRemote());
        //result.setDoLocalExecution(config.isRd);

        result.setBaseUri(baseIri);
        result.setDataQef(cachedDataQef);
        result.setBenchmarkQef(benchmarkQef);
        result.setDatasetEndpointUri(datasetEndpointUri);
        result.setDatasetSize(datasetSize);
        result.setStmtParser(sparqlStmtParser);
        result.setExpRes(ResourceFactory.createResource(config.getExperimentIri()));

        result.setReuseLogIri(config.isReuseLogIri());
        result.setQueryIdPattern(config.getQueryIdPattern());
        result.setUseDeterministicPseudoTimestamps(config.isDeterministicPseudoTimestamps());

        
        result.setSeenQueryCache(seenQueryCache);
        
        return result;
    }
}
