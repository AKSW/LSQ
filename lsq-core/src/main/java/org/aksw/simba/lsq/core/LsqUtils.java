package org.aksw.simba.lsq.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

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
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtIterator;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParser;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmtQuery;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.aksw.jena_sparql_api.utils.DatasetUtils;
import org.aksw.jena_sparql_api.utils.model.ResourceInDataset;
import org.aksw.jena_sparql_api.utils.model.ResourceInDatasetImpl;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.model.RemoteExecution;
import org.aksw.simba.lsq.parser.Mapper;
import org.aksw.simba.lsq.parser.WebLogParser;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.hash.Hashing;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.tika.io.CloseShieldInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

public class LsqUtils {
	private static final Logger logger = LoggerFactory.getLogger(LsqUtils.class);


	public static List<Entry<String, Number>> probeLogFormat(String resource) {
		//FileSystemResourceLoader loader = new FileSystemResourceLoader();		
		Map<String, ResourceParser> registry = LsqUtils.createDefaultLogFmtRegistry();
		
		List<Entry<String, Number>> result = probeLogFormat(registry, resource);
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
	
	public static List<Entry<String, Number>> probeLogFormat(Map<String, ResourceParser> registry, String resource) {
		
		Multimap<? extends Number, String> report = probeLogFormatCore(registry, resource);
		
		List<Entry<String, Number>> result = report.entries().stream()
			.filter(e -> e.getKey().doubleValue() != 0)
			.map(e -> Maps.immutableEntry(e.getValue(), (Number)e.getKey()))
//			.limit(2)
			//.map(Entry::getValue)
			//.flatMap(Collection::stream)
			.collect(Collectors.toList());

		return result;
	}

	/**
	 * Return formats sorted by weight
	 * Higher weight = better format; more properties could be parsed with that format
	 * 
	 * @param registry
	 * @param loader
	 * @param filename
	 * @return
	 */
	public static Multimap<Double, String> probeLogFormatCore(Map<String, ResourceParser> registry, String filename) {
		//org.springframework.core.io.Resource resource = loader.getResource(filename);
		// SparqlStmtUtils.openInputStream(filenameOrURI)
		
		// succcessCountToFormat
		Multimap<Double, String> result = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
		
		for(Entry<String, ResourceParser> entry : registry.entrySet()) {
			String formatName = entry.getKey();
			
//			if(formatName.equals("wikidata")) {
//				System.out.println("here");
//			}
			
			ResourceParser fn = entry.getValue();

			// Try-catch block because fn.parse may throw an exception before the flowable is created
			// For example, a format may attempt ot read the input stream into a buffer
			List<ResourceInDataset> baseItems;
			try {
				baseItems = fn.parse(() -> SparqlStmtUtils.openInputStream(filename))
						.limit(1000)
						.toList()
						.onErrorReturn(x -> Collections.emptyList())
						.blockingGet();
			} catch(Exception e) {
				baseItems = Collections.emptyList();
				logger.debug("Probing against format " + formatName + " raised exception", e);
			}

			int availableItems = baseItems.size();

			//List<Resource> items =
			List<Resource> parsedItems = baseItems.stream()
					.filter(r -> !r.hasProperty(LSQ.processingError))
					.collect(Collectors.toList());
					//.collect(Collectors.toList());

			long parsedItemCount = parsedItems.size();

			double avgImmediatePropertyCount = parsedItems.stream()
					.mapToInt(r -> r.listProperties().toList().size())
					.average().orElse(0);

			// Weight is the average number of properties multiplied by the
			// fraction of successfully parsed items
			double parsedFraction = availableItems == 0 ? 0 : (parsedItemCount / (double)availableItems); 
			double weight = parsedFraction * avgImmediatePropertyCount;
			
			result.put(weight, formatName);
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

    public static Map<String, ResourceParser> createDefaultLogFmtRegistry() {
        Map<String, ResourceParser> result = new HashMap<>();

        // Load line based log formats
        LsqUtils.wrap(result, WebLogParser.loadRegistry(RDFDataMgr.loadModel("default-log-formats.ttl")));

        // Add custom RDF based log format(s)
        result.put("rdf", in -> LsqUtils.createResourceStreamFromRdf(in, Lang.NTRIPLES, "http://example.org/"));

        // Add multi-line sparql format
        result.put("sparql", in -> LsqUtils.createSparqlStream(in));
        
        return result;
    }
    
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
    					.applyOnResource(r -> r.addLiteral(LSQ.query, queryStr)));
    	
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


    public static Flowable<ResourceInDataset> createResourceStreamFromMapperRegistry(Callable<InputStream> in, Function<String, Mapper> fmtSupplier, String fmtName) {
        Mapper mapper = fmtSupplier.apply(fmtName);
        if(mapper == null) {
            throw new RuntimeException("No mapper found for '" + fmtName + "'");
        }

        //BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        //Stream<String> stream = reader.lines();

        Flowable<String> flow = Flowable.generate(
        		() -> new BufferedReader(new InputStreamReader(in.call(), StandardCharsets.UTF_8)),
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


    public static Map<String, ResourceParser> wrap(Map<String, ResourceParser> result, Map<String, Mapper> webLogParserRegistry) {
        Map<String, ResourceParser> tmp = result == null
                ? new HashMap<>()
                : result;

        webLogParserRegistry.forEach((name, mapper) -> {
        	ResourceParser fn = inSupp -> createResourceStreamFromMapperRegistry(inSupp, webLogParserRegistry::get, name);
            tmp.put(name, fn);
        });

        return tmp;
    }


    public static Flowable<ResourceInDataset> createReader(LsqConfigImpl config) throws IOException {

    	List<String> inputResources = config.getInQueryLogFiles();
		logger.info("Input resources: " + inputResources);

//		for(String str : inputResources) {
//			PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(str);
//			//pathMatcher.
//			Files.walkFileTree(start, visitor)
//			
//		}
//		
    	Flowable<ResourceInDataset> result = Flowable.fromIterable(inputResources)
    			.flatMap(inputResource -> {
					try {
						return createReader(config, inputResource);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
    	
    	return result;
    }
    
    

    public static Flowable<ResourceInDataset> createReader(LsqConfigImpl config, String inputResource) throws IOException {
        Long itemLimit = config.getItemLimit();
        String logFormat = config.getInQueryLogFormat();
        String baseIri = config.outBaseIri;
        //String datasetUrl = config.iri;
        String hostHashSalt = "";
        String serviceUrl = null;

        Function<String, SparqlStmt> sparqlParser = SparqlStmtParserImpl.create(new PrefixMappingImpl());

        Map<String, ResourceParser> logFmtRegistry = config.getLogFmtRegistry();
        Flowable<ResourceInDataset> result = createReader(
        		inputResource, sparqlParser, logFormat, logFmtRegistry, baseIri, hostHashSalt, serviceUrl);

        if(itemLimit != null) {
            result = result.limit(itemLimit);
        }

        
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
    public static Flowable<ResourceInDataset> createReader(
    		String logSource,
    		Function<String, SparqlStmt> sparqlStmtParser,
    		String logFormat,
    		Map<String, ResourceParser> logFmtRegistry,
    		String baseIri,
    		String hostHashSalt,
    		String serviceUrl
    		) throws IOException {

//		String filename;
//		if(logSource == null) {
//			filename = "stdin";
//		} else {
//			Path path = Paths.get(logSource);
//			filename = path.getFileName().toString();
//		}

        Callable<InputStream> inSupp = logSource == null
        		? () -> new CloseShieldInputStream(System.in)
        		: () -> RDFDataMgr.open(logSource); // Alteratively StreamMgr.open()

        Lang lang = logFormat == null
        		? null
        		: RDFLanguages.nameToLang(logFormat);
 
        if(lang == null) {
        	lang = RDFDataMgr.determineLang(logSource, null, null);
        }

        Flowable<ResourceInDataset> result = null;

        
		// Check if we are dealing with RDF
        if(lang != null) {
        	// If quad based, use streaming
        	// otherwise partition by lsq.text property
        	if(RDFLanguages.isQuads(lang)) {
        		
        		// TODO Stream as datasets first, then select any resource with LSQ.text
        		logger.info("Quad-based format detected - assuming RDFized log as input");
        		result = RDFDataMgrRx.createFlowableDatasets(inSupp, lang, null)
        				.flatMap(ds -> Flowable.fromIterable(
        						DatasetUtils.listResourcesWithProperty(ds, LSQ.text).toList()));
        		
//        		result = Streams.stream(RDFDataMgrRx.createFlowableResources(inSupp, lang, "")
//        			.blockingIterable()
//        			.iterator());
        	} else if(RDFLanguages.isTriples(lang)){ 
        		logger.info("Triple-based format detected - assuming RDFized log as input");
        		Model model = RDFDataMgr.loadModel(logSource, lang);
        		result = null;
        		throw new RuntimeException("Triple based format not implemented");
        		//result = Flowable.fromIterable(() -> model.listSubjectsWithProperty(LSQ.text))
        	} else {
        		throw new RuntimeException("Unknown RDF input format; neither triples nor quads");
        	}

        } 
        
		String effectiveLogFormat = null;

        // If the result is still null, probe for log formats
        if(result == null) {
            // Probe for known RDF or know log format
        	logger.info("Processing log source " + logSource);
        	
    		if(Strings.isNullOrEmpty(logFormat)) {
    			List<Entry<String, Number>> formats = LsqUtils.probeLogFormat(logFmtRegistry, logSource);
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

            result = webLogParser.parse(() -> RDFDataMgr.open(logSource));
            
            
            // The webLogParser yields resources (blank nodes) for the log entry
            // First add a sequence id attribute
            // Then invert the entry: 
            
	        result = result
	        	.zipWith(LongStream.iterate(1, x -> x + 1)::iterator, Maps::immutableEntry)
	        	// Add the zipped index to the resource
	        	.map(e -> {
	        		ResourceInDataset r = e.getKey();
	        		Long idx = e.getValue();
	        		
	        		RemoteExecution re = r.as(RemoteExecution.class);
	        		re.setSequenceId(idx);
	        		
	        		return r;
	        	})
	        	//.zipWith(, zipper)
	        	//.doOnNext()	
	        	.flatMapMaybe(x -> {
	        		RemoteExecution re = x.as(RemoteExecution.class);

	        		
//	        		// Long seqId = re.getSequenceId();
//	        		
//	        		Number presentSeqId = ResourceUtils.getLiteralPropertyValue(x, LSQ.sequenceId, Number.class);
//
//	        		long seqId;
//	        		if(presentSeqId != null) {
//	        			seqId = presentSeqId.longValue();
//	        		} else {
//	        			seqId = nextId[0]++;
//		        		x.addLiteral(LSQ.sequenceId, seqId);
//	        		}
//
	        		// If we cannot obtain a query from the log record, we omit the entry
	        		Maybe<ResourceInDataset> r = Maybe.empty();

	        		// Invert; map from query to log entry
	        		ResourceInDataset qq = x.wrapCreate(Model::createResource);
	        		LsqQuery q = qq.as(LsqQuery.class);
	        		q.getRemoteExecutions(Resource.class).add(x);
//        			String graphAndResourceIri = "urn:lsq:" + filename + "-" + seqId;
//        			ResourceInDataset xx;
//        			
//        			if(x.isAnon()) {
//        				xx = ResourceInDatasetImpl.renameResource(x, graphAndResourceIri);
//        				xx = ResourceInDatasetImpl.renameGraph(xx, graphAndResourceIri);
//        			} else {
//        				xx = x;
//        			}
//        			String graphAndResourceIri = "urn:lsq:query:sha256:" + filename + "-" + seqId;
        			
	        	//try {
	        		SparqlStmtQuery parsedQuery = getParsedQuery(x, sparqlStmtParser);	        		
	        		if(parsedQuery != null) {	        			
	        			String str = parsedQuery.isParsed()
	        					? parsedQuery.getQuery().toString()
	        					: parsedQuery.getOriginalString();

	        			String queryHash = Hashing.sha256().hashString(str, StandardCharsets.UTF_8).toString();
	        			q.setText(str);
	        			q.setHash(queryHash);
	        			
	        			Throwable t = parsedQuery.getParseException();
	        			if(t != null) {
	        				q.setParseError(t.toString());
	        			}
	        			
	        			// String graphAndResourceIri = "urn:lsq:query:sha256:" + hash;
	        			String graphAndResourceIri = baseIri + "q-" + queryHash;
	        			// Note: We could also leave the resource as a blank node
	        			// The only important part is to have a named graph with the query hash
	        			// in order to merge records about equivalent queries
        				qq = ResourceInDatasetImpl.renameResource(qq, graphAndResourceIri);

        				
        				/*
        				 * Generate IRI for log record
        				 */

    	        		String host = re.getHost();
    	        		if(host != null) {
    	        	        String hostHash = Hashing.sha256().hashString(hostHashSalt + host, StandardCharsets.UTF_8).toString();
    	        	        re.setHostHash(hostHash);
    	        	        re.setHost(null);
    	        	        
    	        	        Calendar timestamp = re.getTimestamp();
    	        	        
    	        	        long seqId = re.getSequenceId();
    	        	        // TODO If there is a timestamp then use it
    	        	        // Otherwise, use sourceFileName + sequenceId
    	        	        String logEntryId = hostHash + "_" + (timestamp != null
    	        	        		? timestamp.toInstant().toString()
    	        	        		: seqId);
    	        	        		
   	        	        
    	        			String reIri = baseIri + "re-" + logEntryId;
    	        			org.apache.jena.util.ResourceUtils.renameResource(re, reIri);
    	        		}
        				
        				
        				qq = ResourceInDatasetImpl.renameGraph(qq, graphAndResourceIri);

        				
        				r = Maybe.just(qq);
	        		}

	        		
	        		//LsqUtils.postProcessSparqlStmt(x, sparqlStmtParser);
//	        	} catch(Exception e) {
//	                qq.addLiteral(LSQ.processingError, e.toString());
//	        	}

	        	// Remove text and query properties, as LSQ.text is
	        	// the polished one
	        	// xx.removeAll(LSQ.query);
	        	// xx.removeAll(RDFS.label);

	        	return r;
	        });
	        //.filter(x -> x != null);

//    		result = result
//    			.map(ResourceInDataset::getDataset)
//    			.compose(MainCliNamedGraphStream.createMapper("lsq-invert-rdfized-log.sparql"))
//    			.flatMap(ds -> Flowable.fromIterable(ResourceInDatasetImpl.selectByProperty(ds, LSQ.text)));
            
        }

        
        
        
        // effective log format is now non-null
//        if(effectiveLogFormat == null) {
//            throw new RuntimeException("Could not obtain effective log format for '" + logFormat + "'");
//        }

    	
    	
//    	Callable<InputStream> inSupp;
//    	if(inputResource != null) {
//        	// TODO We could make the resource loader part of the config
//    		ResourceLoader loader = new DefaultResourceLoader();
//    		org.springframework.core.io.Resource resource = loader.getResource(inputResource);
//    		
//    		// Retry with prepending file:
//    		if(!resource.exists()) {
//    			Path path = Paths.get(inputResource);
//    			path = path.toAbsolutePath();
//    			path = path.normalize();
//    			logger.info("Attempting to open: [" + path + "]");
//    			resource = new FileSystemResource(path.toFile());
//    		}
    		
//            File inputFile = new File(inputResource);
//            inputFile = inputFile.getAbsoluteFile();
//            inSupp = resource::getInputStream;
//        } else {
//            inSupp = () -> new CloseShieldInputStream(System.in);
//        }
        



        // RDF Input overrides any log format

        
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

//	public static <T extends Resource> Flowable<T> postProcessStream(Flowable<T> result) {
//		// Enrich potentially missing information
//        result = Streams.mapWithIndex(result, (r, i) -> {
//        	if(!r.hasProperty(LSQ.host)) {
//        		// TODO Potentially make host configurable
//        		r.addLiteral(LSQ.host, "localhost");
//        	}
//
//// Note: Avoid instantiating new dates as this breaks determinacy
////        	if(!r.hasProperty(PROV.atTime)) {
////        		r.addLiteral(PROV.atTime, new GregorianCalendar());
////        	}
//        	
//        	if(!r.hasProperty(LSQ.sequenceId)) {
//        		r.addLiteral(LSQ.sequenceId, i);
//        	}
//        	
//        	return r;
//        }).onClose(() -> {
//			try {
//				in.close();
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		});
//		return result;
//	}
//	

	/**
	 * Extends a list of prefix sources by adding a snapshot of prefix.cc
	 * 
	 * @param prefixSources
	 * @return
	 */
	public static Iterable<String> prependDefaultPrefixSources(Iterable<String> prefixSources) {
	    Iterable<String> sources = Iterables.concat(
	    		Collections.singleton("rdf-prefixes/prefix.cc.2019-12-17.jsonld"),
	    		Optional.ofNullable(prefixSources).orElse(Collections.emptyList()));
	    return sources;
	}

	public static SparqlStmtParser createSparqlParser(Iterable<String> prefixSources) {
        PrefixMapping prefixMapping = new PrefixMappingImpl();
        for(String source : prefixSources) {
        	PrefixMapping tmp = RDFDataMgr.loadModel(source);
        	prefixMapping.setNsPrefixes(tmp);
        }
        
        SparqlStmtParser result = SparqlStmtParserImpl.create(
        		Syntax.syntaxARQ, prefixMapping, true);

        return result;
	}

	
	/**
	 * If a log entry could be processed without error, it is assumed that there is a
	 * LSQ.query property available.
	 * LSQ.query is a raw query, i.e. it may not be parsable as is because namespaces may be implicit
	 * 
	 * 
	 * @param r A log entry resource
	 * @param sparqlStmtParser
	 */
    public static SparqlStmtQuery getParsedQuery(Resource r, Function<String, SparqlStmt> sparqlStmtParser) {

    	SparqlStmtQuery result = null; // the parsed query string - if possible
        // logger.debug(RDFDataMgr.write(out, dataset, lang););

        // Extract the raw query string and add it with the lsq:query property to the log entry resource
        WebLogParser.extractRawQueryString(r);

        // If the resource is null, we could not parse the log entry
        // therefore count this as an error

        boolean logLineSuccessfullyParsed = r.getProperty(LSQ.processingError) == null;

        if(logLineSuccessfullyParsed) {
            Optional<String> str = Optional.ofNullable(r.getProperty(LSQ.query))
                    .map(queryStmt -> queryStmt.getString());

            //Model m = ResourceUtils.reachableClosure(r);
            SparqlStmt stmt = str
                    .map(sparqlStmtParser)
                    .orElse(null);
            
            if(stmt != null && stmt.isQuery()) { 
            	if(stmt.isParsed()) {
	                SparqlStmtUtils.optimizePrefixes(stmt);	
	                SparqlStmtQuery queryStmt = stmt.getAsQueryStmt();
	
	                //result = queryStmt.getQuery();
	                //String queryStr = Objects.toString(query);
	                //r.addLiteral(LSQ.text, queryStr);
            	}
            	
            	result = stmt.getAsQueryStmt();
            }
        }
        
        return result;
    }

    public static LsqProcessor createProcessor(LsqConfigImpl config) {

        LsqProcessor result = new LsqProcessor();

//        Function<String, SparqlStmt> sparqlStmtParser = config.getSparqlStmtParser();
        //SparqlParserConfig sparqlParserConfig = SparqlParserConfig.create().create(S, prologue)
//        sparqlStmtParser = sparqlStmtParser != null ? sparqlStmtParser : SparqlStmtParserImpl.create(Syntax.syntaxARQ, PrefixMapping2.Extended, true);

        // By default, make a snapshot of prefix.cc prefixes available
        // CollectionUtils.emptyIfNull would be nice to have here
        Function<String, SparqlStmt> sparqlStmtParser = createSparqlParser(config.getPrefixSources());


        String benchmarkEndpoint = config.getBenchmarkEndpoint();
        SparqlServiceReference benchmarkEndpointDescription = benchmarkEndpoint == null
        		? null
        		: new SparqlServiceReference(benchmarkEndpoint, config.getBenchmarkDs());
        
        Long datasetSize = config.getDatasetSize();
        //String localDatasetEndpointUrl = config.getLocalDatasetEndpointUrl()
        //List<String> datasetDefaultGraphIris = config.getDatasetDefaultGraphIris();
        boolean isFetchDatasetSizeEnabled = config.isFetchDatasetSizeEnabled();

        boolean isRdfizerQueryExecutionEnabled = config.isRdfizerQueryExecutionEnabled();
        List<String> fedEndpoints = config.getFederationEndpoints();
        //String benchmarkEndpointUrl = benchmarkEndpointDescription.getServiceURL();
        Long queryTimeoutInMs = config.getBenchmarkQueryExecutionTimeoutInMs();
        String baseIri = config.getOutBaseIri();


        //SparqlServiceReference datasetEndpointDescription = config.getDatasetEndpointDescription();
        String datasetEndpointUri = config.getDatasetEndpoint(); //datasetEndpointDescription == null ? null : datasetEndpointDescription.getServiceURL();

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
