package org.aksw.simba.lsq.cli.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.fedx.jsa.FedXFactory;
import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryExceptionCache;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.cache.staging.CacheBackendMem;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.stmt.SparqlStmtParserImpl;
import org.aksw.simba.lsq.core.LsqProcessor;
import org.aksw.simba.lsq.parser.Mapper;
import org.aksw.simba.lsq.parser.WebLogParser;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Streams;
import com.google.common.io.Files;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class LsqCliParser {

    private static final Logger logger = LoggerFactory.getLogger(LsqCliParser.class);


    protected OptionParser parser = new OptionParser();

    //protected Map<String, Mapper> logFmtRegistry;
    protected Map<String, Function<InputStream, Stream<Resource>>> logFmtRegistry;


    protected OptionSpec<File> inputOs;
    protected OptionSpec<File> outputOs;
    protected OptionSpec<String> logFormatOs;
    protected OptionSpec<String> outFormatOs;
    protected OptionSpec<String> rdfizerOs;
    protected OptionSpec<String> benchmarkEndpointUrlOs;
    protected OptionSpec<String> graphUriOs;
    protected OptionSpec<String> datasetLabelOs;
    protected OptionSpec<Long> headOs;
    protected OptionSpec<Long> datasetSizeOs;
    protected OptionSpec<Long> timeoutInMsOs;
    protected OptionSpec<String> baseUriOs;
    protected OptionSpec<Boolean> logIriAsBaseIriOs;
    protected OptionSpec<String> datasetEndpointUriOs;
    protected OptionSpec<String> expBaseUriOs;
    protected OptionSpec<String> fedEndpointsOs;
    protected OptionSpec<File> fedEndpointsFileOs;

    public OptionParser getOptionParser() {
        return parser;
    }

    public LsqCliParser() {
        this(createDefaultLogFmtRegistry());
    }


    public static Map<String, Function<InputStream, Stream<Resource>>> createDefaultLogFmtRegistry() {
        Map<String, Function<InputStream, Stream<Resource>>> result = new HashMap<>();

        // Load line based log formats
        wrap(result, WebLogParser.loadRegistry(RDFDataMgr.loadModel("default-log-formats.ttl")));

        // Add custom RDF based log format(s)
        result.put("rdf", (in) -> LsqCliParser.createResourceStreamFromRdf(in, Lang.NTRIPLES, "http://example.org/"));

        return result;
    }

    public LsqCliParser(Map<String, Function<InputStream, Stream<Resource>>> logFmtRegistry) {
        this.parser = new OptionParser();

        this.logFmtRegistry = logFmtRegistry;

        initOptionSpecs();
    }

    public void initOptionSpecs() {

        inputOs = parser
                .acceptsAll(Arrays.asList("f", "file"), "File containing input data")
                .withRequiredArg()
                .ofType(File.class)
                ;

        outputOs = parser
                .acceptsAll(Arrays.asList("o", "output"), "File where to store the output data.")
                .withRequiredArg()
                .ofType(File.class)
                ;

        logFormatOs = parser
                .acceptsAll(Arrays.asList("m", "format"), "Format of the input data. Available options: " + logFmtRegistry.keySet())
                .withOptionalArg()
                .defaultsTo("combined")
                ;

        outFormatOs = parser
                .acceptsAll(Arrays.asList("w", "outformat"), "Format for (w)riting out data. Available options: " + RDFWriterRegistry.registered())
                .withRequiredArg()
                .defaultsTo("Turtle/blocks")
                ;

        rdfizerOs = parser
                .acceptsAll(Arrays.asList("r", "rdfizer"), "RDFizer selection: Any combination of the letters (e)xecution, (l)og, (q)uery and (p)rocess metadata")
                .withOptionalArg()
                .defaultsTo("elq")
                ;

        benchmarkEndpointUrlOs = parser
                .acceptsAll(Arrays.asList("e", "endpoint"), "Local SPARQL service (endpoint) URL on which to execute queries")
                .withRequiredArg()
                .defaultsTo("http://localhost:8890/sparql")
                ;

        graphUriOs = parser
                .acceptsAll(Arrays.asList("g", "graph"), "Local graph(s) from which to retrieve the data")
                .availableIf(benchmarkEndpointUrlOs)
                .withRequiredArg()
                ;

        datasetLabelOs = parser
                .acceptsAll(Arrays.asList("l", "label"), "Label of the dataset, such as 'dbpedia' or 'lgd'. Will be used in URI generation")
                .withRequiredArg()
                .defaultsTo("mydata")
                ;

        headOs = parser
                .acceptsAll(Arrays.asList("h", "head"), "Only process n entries starting from the top")
                .withRequiredArg()
                .ofType(Long.class)
                ;

        datasetSizeOs = parser
                .acceptsAll(Arrays.asList("d", "dsize"), "Dataset size. Used in some computations. If not given, it will be queried (which might fail). Negative values disable dependent computations.")
                .withRequiredArg()
                .ofType(Long.class)
                ;

        timeoutInMsOs = parser
                .acceptsAll(Arrays.asList("t", "timeout"), "Timeout in milliseconds")
                .withRequiredArg()
                .ofType(Long.class)
                //.defaultsTo(60000l)
                //.defaultsTo(null)
                ;

        baseUriOs = parser
                .acceptsAll(Arrays.asList("b", "base"), "Base URI for URI generation")
                .withRequiredArg()
                .defaultsTo(LSQ.defaultLsqrNs)
                ;

        logIriAsBaseIriOs = parser
                .acceptsAll(Arrays.asList("i", "logirisasbase"), "Use IRIs in RDF query logs as the base IRIs")
                .withRequiredArg()
                .ofType(Boolean.class)
                .defaultsTo(false)
                ;

        datasetEndpointUriOs = parser
                .acceptsAll(Arrays.asList("p", "public"), "Public endpoint URL - e.g. http://example.org/sparql")
                .withRequiredArg()
                //.defaultsTo("http://example.org/sparql")
                //.defaultsTo(LSQ.defaultLsqrNs + "default-environment");
                ;

        expBaseUriOs = parser
                .acceptsAll(Arrays.asList("x", "experiment"), "URI of the experiment environment")
                .withRequiredArg()
                //.defaultsTo(LSQ.defaultLsqrNs)
                ;

        fedEndpointsOs = parser
                .acceptsAll(Arrays.asList("fed"), "URIs of federated endpoints")
                .withRequiredArg();

        fedEndpointsFileOs = parser
                .acceptsAll(Arrays.asList("fedf"), "URIs of federated endpoints")
                .withRequiredArg()
                .ofType(File.class);

//        reuseLogIri = parser
//                .acceptsAll(Arrays.asList("b", "base"), "Base URI for URI generation")
//                .withRequiredArg()
//                .defaultsTo(LSQ.defaultLsqrNs)
//                ;

    }


    public LsqConfig parse(String[] args) throws IOException {

        OptionSet options = parser.parse(args);


        String datasetLabel = datasetLabelOs.value(options);
        String baseUri = baseUriOs.value(options);
        Long head = headOs.value(options);
        String rdfizer = rdfizerOs.value(options);

        boolean fetchDatasetSize = !options.has(datasetSizeOs);

        Long datasetSize = options.has(datasetSizeOs) ? datasetSizeOs.value(options) : null;
        datasetSize = datasetSize == null ? null : (datasetSize < 0 ? null : datasetSize);

        String expBaseUri = expBaseUriOs.value(options);
        String outFormatStr = outFormatOs.value(options);

        RDFFormat outFormat = RDFWriterRegistry.registered().stream().filter(f -> f.toString().equals(outFormatStr)).findFirst().orElse(null);
        if(outFormat == null) {
            throw new RuntimeException("No Jena writer found for name: " + outFormatStr);
        }

        expBaseUri = expBaseUri == null ? baseUri + datasetLabel : expBaseUri;

        List<String> fedEndpoints = new ArrayList<>();
        if(options.has(fedEndpointsFileOs)) {
            File fedEndpointsFile = fedEndpointsFileOs.value(options);
            Files.readLines(fedEndpointsFile, StandardCharsets.UTF_8).stream()
                    .map(String::trim)
                    .filter(line -> line.startsWith("#"))
                    .forEach(fedEndpoints::add);
        }

        if(options.has(fedEndpointsOs)) {
            List<String> tmp = fedEndpointsOs.values(options);
            fedEndpoints.addAll(tmp);
        }


        // TODO Messed up endpoint urls of the dataset distribution and that of the local executions... - need to fix
        String benchmarkEndpointUrl = benchmarkEndpointUrlOs.value(options);
        List<String> benchmarkDefaultGraphIris = graphUriOs.values(options);
        DatasetDescription benchmarkDatasetDescription = DatasetDescription.create(benchmarkDefaultGraphIris, Collections.emptyList());
        SparqlServiceReference benchmarkEndpointDescription = new SparqlServiceReference(benchmarkEndpointUrl, benchmarkDatasetDescription);


        String datasetEndpointUri = datasetEndpointUriOs.value(options);
        List<String> datasetDefaultGraphIris = graphUriOs.values(options);

        DatasetDescription datasetDescription = DatasetDescription.create(datasetDefaultGraphIris, Collections.emptyList());
        SparqlServiceReference datasetEndpointDescription = new SparqlServiceReference(datasetEndpointUri, datasetDescription);



//        Map<String, Function<InputStream, Stream<Resource>>> inputFormatRegistry = new HashMap();
//        wrap(inputFormatRegistry, )


        LsqConfig config = new LsqConfig();



        config.setLogFmtRegistry(logFmtRegistry);
        config.setOutBaseIri(baseUri);
        config.setExperimentIri(expBaseUri);

        config.setInQueryLogFile(inputOs.value(options));
        config.setInQueryLogFormat(logFormatOs.value(options));

        // By default, reuse log iris if the format is rdf; unless it is explicitly overridden ...
        boolean reuseLogIris = !options.has(logIriAsBaseIriOs)
                ? logFormatOs.value(options).equals("rdf")
                : logIriAsBaseIriOs.value(options);

        config.setReuseLogIri(reuseLogIris);



        config.setFetchDatasetSizeEnabled(config.isFetchDatasetSizeEnabled());

        config.setDatasetLabel(datasetLabel);
        config.setDatasetEndpointDescription(datasetEndpointDescription);

        config.setDatasetSize(datasetSize);

        config.setBenchmarkEndpointDescription(benchmarkEndpointDescription);
        config.setBenchmarkQueryExecutionTimeoutInMs(timeoutInMsOs.value(options));
        config.setFirstItemOffset(head);

        config.setFederationEndpoints(fedEndpoints);

        config.setRdfizerQueryStructuralFeaturesEnabled(rdfizer.contains("q"));
        config.setRdfizerQueryLogRecordEnabled(rdfizer.contains("l"));
        config.setRdfizerQueryExecutionEnabled(rdfizer.contains("e"));
        config.setEmitProcessMetadata(rdfizer.contains("p"));

        config.setOutFile(outputOs.value(options));

        return config;
    }


    public static Sink<Resource> createWriter(LsqConfig config) throws FileNotFoundException {
        String outRdfFormat = config.getOutRdfFormat();
        File outFile = config.getOutFile();

        RDFFormat rdfFormat = StringUtils.isEmpty(outRdfFormat) ? RDFFormat.TURTLE_BLOCKS : RDFWriterRegistry.getFormatForJenaWriter(outRdfFormat);
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
            Function<InputStream, Stream<Resource>> fn = (in) -> createResourceStreamFromMapperRegistry(in, webLogParserRegistry::get, name);
            tmp.put(name, fn);
        });

        return tmp;
    }


    public static Stream<Resource> createReader(LsqConfig config) throws FileNotFoundException {

        File inputFile = config.getInQueryLogFile();
        InputStream in;
        if(inputFile != null) {
            inputFile = inputFile.getAbsoluteFile();
            in = new FileInputStream(inputFile);
        } else {
            in = System.in;
        }


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

    public static LsqProcessor createProcessor(LsqConfig config) {

        LsqProcessor result = new LsqProcessor();

        Function<String, SparqlStmt> sparqlStmtParser = config.getSparqlStmtParser();
        sparqlStmtParser = sparqlStmtParser != null ? sparqlStmtParser : SparqlStmtParserImpl.create(Syntax.syntaxARQ, true);


        SparqlServiceReference benchmarkEndpointDescription = config.getDatasetEndpointDescription();
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


        QueryExecutionFactory countQef;
        QueryExecutionFactory baseDataQef;
        QueryExecutionFactory dataQef = null;

        if(isRdfizerQueryExecutionEnabled) {
            boolean isNormalMode = fedEndpoints.isEmpty();
            //boolean isFederatedMode = !isNormalMode;

            if(isNormalMode) {
                countQef =
                        FluentQueryExecutionFactory
                        .http(benchmarkEndpointDescription)
                        .create();

                baseDataQef = FluentQueryExecutionFactory.http(benchmarkEndpointDescription).create();

            } else {
                countQef = null;

                baseDataQef = FedXFactory.create(fedEndpoints);
            }

            dataQef =
                    FluentQueryExecutionFactory
                    //.http(endpointUrl, graph)
                    .from(baseDataQef)
                    .config()
                        .withParser(SparqlQueryParserImpl.create())
                        .withPostProcessor(qe -> {
                            if(queryTimeoutInMs != null) {
                                qe.setTimeout(0, queryTimeoutInMs);
    //                            ((QueryEngineHTTP)((QueryExecutionHttpWrapper)qe).getDecoratee())
    //                            .setTimeout(timeoutInMs);
                            }
                        })
                        //.onTimeout((qef, queryStmt) -> )
                        .withCache(new CacheFrontendImpl(new CacheBackendMem(queryCache)))
                        .compose(qef ->  new QueryExecutionFactoryExceptionCache(qef, exceptionCache))
                        //)
    //                    .withRetry(3, 30, TimeUnit.SECONDS)
    //                    .withPagination(1000)
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

        result.setDatasetLabel(config.getDatasetLabel());
        result.setRdfizerQueryStructuralFeaturesEnabled(config.isRdfizerQueryStructuralFeaturesEnabled());
        result.setRdfizerQueryLogRecordEnabled(config.isRdfizerQueryLogRecordEnabled());
        result.setRdfizerQueryExecutionEnabled(config.isRdfizerQueryExecutionEnabled());
        //result.setQueryExecutionRemote(config.isQueryExecutionRemote());
        //result.setDoLocalExecution(config.isRd);

        result.setBaseUri(baseIri);
        result.setDataQef(dataQef);
        result.setDatasetEndpointUri(datasetEndpointUri);
        result.setDatasetSize(datasetSize);
        result.setStmtParser(sparqlStmtParser);
        result.setExpRes(ResourceFactory.createResource(config.getExperimentIri()));

        result.setReuseLogIri(config.isReuseLogIri());

        return result;
    }
}




//config.setDa
//config.setLog



//These lines are just kept for reference in case we need something fancy
//JOptCommandLinePropertySource clps = new JOptCommandLinePropertySource(options);
//ApplicationContext ctx = SpringApplication.run(ConfigLSQ.class, args);

//JenaSystem.init();
//InitJenaCore.init();
//ARQ.init();
//SPINModuleRegistry.get().init();


//DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//Calendar startTime = new GregorianCalendar();

//Date date = startTime.getTime();
//String prts []  = dateFormat.format(date).split(" ");


// Note: We re-use the baseGeneratorRes in every query's model, hence its not bound to the specs model directly
// However, with .inModel(model) we can create resources that are bound to a specific model from another resource
//Resource baseGeneratorRes = ResourceFactory.createResource(baseUri + datasetLabel + "-" + prts[0]);

//Model specs = ModelFactory.createDefaultModel();
//Resource generatorRes = baseGeneratorRes.inModel(specs);


// TODO Attempt to determine attributes automatically ; or merge this data from a file or something
//Resource engineRes = specs.createResource()
//        .addProperty(LSQ.vendor, specs.createResource(LSQ.defaultLsqrNs + "Virtuoso"))
//        .addProperty(LSQ.version, "Virtuoso v.7.2")
//        .addProperty(LSQ.processor, "2.5GHz i7")
//        .addProperty(LSQ.ram,"8GB");
//generatorRes
//    .addProperty(LSQ.engine, engineRes);

//Resource datasetRes = specs.createResource();
//generatorRes
//    .addLiteral(LSQ.dataset, datasetRes)
//    .addLiteral(PROV.hadPrimarySource, specs.createResource(endpointUrl))
//    .addLiteral(PROV.startedAtTime, startTime);


//List<Resource> workloadResources = stream
//        .map(line -> {
//        Resource r = logModel.createResource();
//        webLogParser.parseEntry(line, r);
//        return r;
//    })
//    .collect(Collectors.toList());

