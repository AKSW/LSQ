package org.aksw.simba.lsq.cli.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.simba.lsq.core.LsqConfigImpl;
import org.aksw.simba.lsq.core.LsqUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.sparql.core.DatasetDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.io.Files;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

public class LsqCliParser {

    private static final Logger logger = LoggerFactory.getLogger(LsqCliParser.class);


    protected OptionParser parser = new OptionParser();

    //protected Map<String, Mapper> logFmtRegistry;
    protected Map<String, Function<InputStream, Stream<Resource>>> logFmtRegistry;

    protected OptionSpec<String> inputOs;
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
    protected OptionSpec<Void> logIriAsBaseIriOs;
    protected OptionSpec<String> queryIdPatternOs;
    protected OptionSpec<String> datasetEndpointUriOs;
    protected OptionSpec<String> expBaseUriOs;
    protected OptionSpec<String> fedEndpointsOs;
    protected OptionSpec<File> fedEndpointsFileOs;
    
    protected OptionSpec<Long> queryDelayInMsOs;
    protected OptionSpec<String> httpUserAgentOs;
    
    protected OptionSpec<String> prefixSourcesOs;

    
    
    public OptionParser getOptionParser() {
        return parser;
    }

    public LsqCliParser() {
        this(LsqUtils.createDefaultLogFmtRegistry());
    }

    public LsqCliParser(Map<String, Function<InputStream, Stream<Resource>>> logFmtRegistry) {
        this.parser = new OptionParser();

        this.logFmtRegistry = logFmtRegistry;

        initOptionSpecs();
    }

    public void initOptionSpecs() {

        inputOs = parser
        		.nonOptions("File(s) containing input data")
                //.acceptsAll(Arrays.asList("f", "file"), "File containing input data")
                //.withRequiredArg()
                //.ofType(File.class)
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
                //.withOptionalArg()
                //.ofType(Boolean.class)
                //.defaultsTo(false)
                ;

        datasetEndpointUriOs = parser
                .acceptsAll(Arrays.asList("p", "public"), "Public endpoint URL for record purposes - e.g. http://dbpedia.org/sparql")
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

        queryIdPatternOs = parser
                .acceptsAll(Arrays.asList("q", "querypattern"), "Pattern to parse out query ids; use empty string to use whole IRI")
                .availableIf(logIriAsBaseIriOs)
                .withOptionalArg()
                //.withRequiredArg()
                .defaultsTo("q-([^->]+)");

        
        queryDelayInMsOs = parser
                .acceptsAll(Arrays.asList("y", "delay"), "Delay in milliseconds")
                .withRequiredArg()
                .ofType(Long.class)
                .defaultsTo(0l)
                //.defaultsTo(60000l)
                //.defaultsTo(null)
                ;

        httpUserAgentOs = parser
                .acceptsAll(Arrays.asList("a", "agent"), "Http user agent field")
                .withRequiredArg()
                .defaultsTo("Linked Sparql Queries (LSQ) client. User agent not set.")
                ;

        prefixSourcesOs = parser
                .acceptsAll(Arrays.asList("n", "namespaces"), "RDF namespace files")
                .withRequiredArg()
                //.defaultsTo("rdf-prefixes/prefix.cc.2019-12-17.jsonld")
                ;

//        reuseLogIri = parser
//                .acceptsAll(Arrays.asList("b", "base"), "Base URI for URI generation")
//                .withRequiredArg()
//                .defaultsTo(LSQ.defaultLsqrNs)
//                ;

    }


    public LsqConfigImpl parse(String[] args) throws IOException {

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

        RDFFormat outFormat = RDFWriterRegistry.registered().stream().filter(f -> f.toString().equalsIgnoreCase(outFormatStr)).findFirst().orElse(null);
        //RDFFormat outFormat = RDFWriterRegistry.get
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


        LsqConfigImpl config = new LsqConfigImpl();



        config.setLogFmtRegistry(logFmtRegistry);
        config.setOutBaseIri(baseUri);
        config.setExperimentIri(expBaseUri);

        List<String> inputs = inputOs.values(options);
        config.setInQueryLogFiles(inputs);
        
        String inLogFormat = logFormatOs.value(options);
        config.setInQueryLogFormat(inLogFormat);

        // By default, reuse log iris if the format is rdf; unless it is explicitly overridden ...
        boolean reuseLogIris = !options.has(logIriAsBaseIriOs)
                ? logFormatOs.value(options).equals("rdf")
                : true;//logIriAsBaseIriOs.value(options);

        String queryIdPatternStr = queryIdPatternOs.value(options);
        queryIdPatternStr = queryIdPatternStr == null ? null : queryIdPatternStr.trim();
        Pattern queryIdPattern = Strings.isNullOrEmpty(queryIdPatternStr) ? null : Pattern.compile(queryIdPatternStr);

        
        Iterable<String> prefixSources = prefixSourcesOs.values(options);
        prefixSources = LsqUtils.prependDefaultPrefixSources(prefixSources);
        
        config.setReuseLogIri(reuseLogIris);
        config.setQueryIdPattern(queryIdPattern);



        config.setFetchDatasetSizeEnabled(fetchDatasetSize);

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
        config.setOutRdfFormat(outFormatStr);

        
        Long delayInMs = queryDelayInMsOs.value(options);
        config.setDelayInMs(delayInMs);
        String userAgent = httpUserAgentOs.value(options);
        config.setHttpUserAgent(userAgent);
        
        config.setPrefixSources(prefixSources);
        
        return config;
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

