package org.aksw.simba.lsq.cli.main;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.http.repository.impl.UriToPathUtils;
import org.aksw.jena_sparql_api.io.json.GroupedResourceInDataset;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.utils.model.ResourceInDataset;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqAnalyze;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqBenchmarkCreate;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqBenchmarkMain;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqBenchmarkRun;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqInvert;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqMain;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqProbe;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqRdfize;
import org.aksw.simba.lsq.core.LsqConfigImpl;
import org.aksw.simba.lsq.core.LsqProcessor;
import org.aksw.simba.lsq.core.LsqUtils;
import org.aksw.simba.lsq.core.ResourceParser;
import org.aksw.simba.lsq.core.Skolemize;
import org.aksw.simba.lsq.model.ExperimentConfig;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.util.NestedResource;
import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.sparql_integrate.ngs.cli.cmd.CmdNgsMap;
import org.aksw.sparql_integrate.ngs.cli.cmd.CmdNgsSort;
import org.aksw.sparql_integrate.ngs.cli.main.ExceptionUtils;
import org.aksw.sparql_integrate.ngs.cli.main.MainCliNamedGraphStream;
import org.aksw.sparql_integrate.ngs.cli.main.NamedGraphStreamOps;
import org.aksw.sparql_integrate.ngs.cli.main.ResourceInDatasetFlowOps;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WebContent;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;



/**
 * This is the main class of Linked Sparql Queries
 *
 * @author raven
 *
 */
public class MainCliLsq {
    private static final Logger logger = LoggerFactory.getLogger(MainCliLsq.class);

    // public static void displayUsageIf()

    public static void main(String[] args) throws Exception {

//		Flowable.fromIterable(LongStream.iterate(0, i -> i + 1)::iterator)
//		.forEach(x -> {
//			System.out.println(x);
//			throw new IOException("x");
//		});

        CmdLsqMain cmdMain = new CmdLsqMain();

        CmdLsqProbe probeCmd = new  CmdLsqProbe();
        CmdLsqRdfize rdfizeCmd = new  CmdLsqRdfize();
//		CmdLsqInvert invertCmd = new  CmdLsqInvert();
        CmdLsqBenchmarkMain benchmarkCmd = new  CmdLsqBenchmarkMain();
        CmdLsqAnalyze analyzeCmd = new  CmdLsqAnalyze();

        JCommander jc = JCommander.newBuilder()
                .addObject(cmdMain)
                .addCommand("probe", probeCmd)
                .addCommand("rdfize", rdfizeCmd)
//				.addCommand("invert", invertCmd)
                .addCommand("analyze", analyzeCmd)
                .addCommand("benchmark", benchmarkCmd)
                .build();

        JCommander benchmarkSubCmds = jc.getCommands().get("benchmark");
        CmdLsqBenchmarkCreate benchmarkCreateCmd = new CmdLsqBenchmarkCreate();
        benchmarkSubCmds.addCommand("create", benchmarkCreateCmd);
        CmdLsqBenchmarkRun benchmarkRunCmd = new CmdLsqBenchmarkRun();
        benchmarkSubCmds.addCommand("run", benchmarkRunCmd);


        jc.parse(args);

        if (cmdMain.help || jc.getParsedCommand() == null) {
            jc.usage();
            return;
        }

        // TODO Change this to a plugin system - for now I hack this in statically
        String cmd = jc.getParsedCommand();
        switch (cmd) {
        case "probe": {
            probe(probeCmd);
            break;
        }
        case "rdfize": {
            rdfize(rdfizeCmd);
            break;
        }
//		case "invert": {
//			invert(invertCmd);
//			break;
//		}
        case "analyze": {
            analyze(analyzeCmd);
            break;
        }
        case "benchmark": {
            String benchmarkSubCmd = benchmarkSubCmds.getParsedCommand();
            switch(benchmarkSubCmd) {
            case "create":
                benchmarkCreate(benchmarkCreateCmd);
                break;
            case "run":
                benchmarkRun(benchmarkRunCmd);
                break;
            default:
                throw new RuntimeException("Unsupported command: " + cmd);
            }
            break;
        }
        default:
            throw new RuntimeException("Unsupported command: " + cmd);
        }
    }


    public static Flowable<ResourceInDataset> createLsqRdfFlow(CmdLsqRdfize rdfizeCmd) throws FileNotFoundException, IOException, ParseException {
        String logFormat = rdfizeCmd.inputLogFormat;
        List<String> logSources = rdfizeCmd.nonOptionArgs;
        String baseIri = rdfizeCmd.baseIri;

        String tmpHostHashSalt = rdfizeCmd.hostSalt;
        if(tmpHostHashSalt == null) {
            tmpHostHashSalt = UUID.randomUUID().toString();
            logger.info("Auto generated host hash salt: " + tmpHostHashSalt);
        }

        String hostHashSalt = tmpHostHashSalt;

        String endpointUrl = rdfizeCmd.endpointUrl;
// TODO Validate all sources first: For trig files it is ok if no endpoint is specified
//		if(endpointUrl == null) {
//			throw new RuntimeException("Please specify the URL of the endpoint the provided query logs are assigned to.");
//		}


        List<String> rawPrefixSources = rdfizeCmd.prefixSources;
        Iterable<String> prefixSources = LsqUtils.prependDefaultPrefixSources(rawPrefixSources);
        Function<String, SparqlStmt> sparqlStmtParser = LsqUtils.createSparqlParser(prefixSources);


        Map<String, ResourceParser> logFmtRegistry = LsqUtils.createDefaultLogFmtRegistry();


        Flowable<ResourceInDataset> logRdfEvents = Flowable
            .fromIterable(logSources)
            .flatMap(logSource -> {
                Flowable<ResourceInDataset> st = LsqUtils.createReader(
                        logSource,
                        sparqlStmtParser,
                        logFormat,
                        logFmtRegistry,
                        baseIri,
                        hostHashSalt,
                        endpointUrl);

                return st;
            });


        if(rdfizeCmd.slimMode) {
            CmdNgsMap cmd = new CmdNgsMap();
            cmd.stmts.add("lsq-slimify.sparql");
//			cmd.nonOptionArgs.addAll(cmdInvert.nonOptionArgs);

//			JenaSystem.init();
            //RDFDataMgrEx.loadQueries("lsq-slimify.sparql", PrefixMapping.Extended);

//			SparqlStmtUtils.processFile(pm, "lsq-slimify.sparql");
//			MainCliNamedGraphStream.createMapper2(); //map(DefaultPrefixes.prefixes, cmd);
            FlowableTransformer<ResourceInDataset, ResourceInDataset> mapper =
                    MainCliNamedGraphStream.createMapper(PrefixMapping.Extended, cmd.stmts,
                            r -> r.getDataset(),
                            (r, ds) -> r.inDataset(ds), cxt -> {});


            logRdfEvents = logRdfEvents
                    .compose(mapper);
        }

        if(!rdfizeCmd.noMerge) {
            CmdNgsSort sortCmd = new CmdNgsSort();
            sortCmd.bufferSize = rdfizeCmd.bufferSize;
            sortCmd.temporaryDirectory = rdfizeCmd.temporaryDirectory;

            FlowableTransformer<GroupedResourceInDataset, GroupedResourceInDataset> sorter = ResourceInDatasetFlowOps.createSystemSorter(sortCmd, null);
            logRdfEvents = logRdfEvents
                    .compose(ResourceInDatasetFlowOps.groupedResourceInDataset())
                    .compose(sorter)
                    .compose(ResourceInDatasetFlowOps::mergeConsecutiveResourceInDatasets)
                    .flatMap(ResourceInDatasetFlowOps::ungrouperResourceInDataset);
        }

        return logRdfEvents;
    }

    public static void rdfize(CmdLsqRdfize cmdRdfize) throws Exception {
        Flowable<ResourceInDataset> logRdfEvents = createLsqRdfFlow(cmdRdfize);
        try {
            RDFDataMgrRx.writeResources(logRdfEvents, new FileOutputStream(FileDescriptor.out), RDFFormat.TRIG_PRETTY);
        } catch(Exception e) {
            ExceptionUtils.rethrowIfNotBrokenPipe(e);
        }
    }

    public static void probe(CmdLsqProbe cmdProbe) {
        List<String> nonOptionArgs = cmdProbe.nonOptionArgs;
        if(nonOptionArgs.size() == 0) {
            System.out.println("No arguments provided.");
            System.out.println("Argument must be one or more log files which will be probed against all registered LSQ log formats");
        }

        for(int i = 0; i < nonOptionArgs.size(); ++i) {
            String filename = nonOptionArgs.get(i);

            List<Entry<String, Number>> bestCands = LsqUtils.probeLogFormat(filename);

            System.out.println(filename + "\t" + bestCands);
        }
    }

    @Deprecated // The process was changed that inversion of the
    // log-record-to-query relation is no longer needed as it is now how the process works
    public static void invert(CmdLsqInvert cmdInvert) throws Exception {
        CmdNgsMap cmd = new CmdNgsMap();
        cmd.stmts.add("lsq-invert-rdfized-log.sparql");
        cmd.nonOptionArgs.addAll(cmdInvert.nonOptionArgs);

        JenaSystem.init();
        NamedGraphStreamOps.map(DefaultPrefixes.prefixes, cmd, new FileOutputStream(FileDescriptor.out));
    }

    // FIXME hasRemoteExec needs to be skolemized - this
    // should probably be done in 'rdfize'
    public static void analyze(CmdLsqAnalyze analyzeCmd) throws Exception {
        CmdLsqRdfize rdfizeCmd = new CmdLsqRdfize();
        rdfizeCmd.nonOptionArgs = analyzeCmd.nonOptionArgs;
        rdfizeCmd.noMerge = true;


        Flowable<ResourceInDataset> flow = createLsqRdfFlow(rdfizeCmd);

        Flowable<Dataset> dsFlow = flow.map(rid -> {
            LsqQuery q = rid.as(LsqQuery.class);

            NestedResource queryRes = NestedResource.from(q);
            Function<String, NestedResource> queryAspectFn =
                    aspect -> queryRes.nest("-" + aspect);

            String queryStr = q.getText();
            if(q.getParseError() == null) {
                Query query = QueryFactory.create(queryStr);
                //SparqlQueryParser parser = SparqlQueryParserImpl.create();

                LsqProcessor.rdfizeQueryStructuralFeatures(q, queryAspectFn, query);

                // Post processing: Remove skolem identifiers
                q.getModel().removeAll(null, Skolemize.skolemId, null);
            }

            return rid;
        })
        .map(ResourceInDataset::getDataset);

        RDFDataMgrRx.writeDatasets(dsFlow, new FileOutputStream(FileDescriptor.out), RDFFormat.TRIG_PRETTY);
    }


    /**
     * Creates a model with the configuration of the benchmark experiment
     * which serves as the base for running the benchmark
     *
     *
     * @param benchmarkCreateCmd
     * @throws Exception
     */
    public static void benchmarkCreate(CmdLsqBenchmarkCreate benchmarkCreateCmd) throws Exception {

        String baseIri = benchmarkCreateCmd.baseIri;
        String endpointUrl = benchmarkCreateCmd.endpoint;

        // We could have datasetId or a datasetIri
        String datasetId = benchmarkCreateCmd.dataset;

        String datasetIri = null;
        if(datasetId != null && datasetId.matches("^[\\w]+://.*")) {
            datasetIri = datasetId;
        }

        // experimentId = distributionId + "_" + timestamp
        String datasetLabel = UriToPathUtils.resolvePath(datasetId).toString()
                .replace('/', '-');


        Instant now = Instant.now();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(now, ZoneId.systemDefault());
        // Calendar nowCal = GregorianCalendar.from(zdt);
        //String timestamp = now.toString();
        String timestamp = DateTimeFormatter.ISO_LOCAL_DATE.format(zdt);



        String expId = "xc-" + datasetLabel + "_" + timestamp;
        String expIri = baseIri + expId;

        Long qt = benchmarkCreateCmd.queryTimeoutInMs;
        Long ct = benchmarkCreateCmd.connectionTimeoutInMs;

        Long datasetSize = benchmarkCreateCmd.datasetSize;

        if(datasetSize == null) {
//            logger.info("Dataset size not set. Attempting to query its size.");
//            logger.info("If successful, restart lsq with -s <obtainedDatasetSize>");

            // TODO inject default graphs
            try(RDFConnection conn = RDFConnectionRemote.create()
                .destination(endpointUrl)
                .acceptHeaderSelectQuery(WebContent.contentTypeResultsXML)
                .build()) {

                Object raw = SparqlRx.execSelect(conn, "SELECT (COUNT(*) AS ?c) { ?s ?p ?o }")
                    .blockingFirst()
                    .get("c").asNode().getLiteralValue();
                datasetSize = ((Number)raw).longValue();
            }
        }

        Model model = ModelFactory.createDefaultModel();

        model.setNsPrefix("lsq", LSQ.ns);

        ExperimentConfig cfg = model.createResource(expIri).as(ExperimentConfig.class);
        cfg
            .setIdentifier(expId)
            // .setCreationDate(nowCal)
            .setDataRef(model.createResource().as(DataRefSparqlEndpoint.class)
                    .setServiceUrl(endpointUrl)
                    .mutateDefaultGraphs(dgs -> dgs.addAll(benchmarkCreateCmd.defaultGraphs))
                    )
            .setQueryTimeout(qt == null ? null : new BigDecimal(qt).multiply(new BigDecimal(0.001)))
            .setConnectionTimeout(ct == null ? null : new BigDecimal(ct).multiply(new BigDecimal(0.001)))
            .setUserAgent(benchmarkCreateCmd.userAgent)
            .setDatasetSize(datasetSize)
            .setDatasetLabel(datasetLabel)
            .setDatasetIri(datasetIri)
            ;

        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
    }

    public static void benchmarkRun(CmdLsqBenchmarkRun benchmarkCmd) throws Exception {
        CmdLsqRdfize rdfizeCmd = new CmdLsqRdfize();
        rdfizeCmd.nonOptionArgs = benchmarkCmd.logSources;
        rdfizeCmd.noMerge = true;

        String configSrc = benchmarkCmd.config;

        //List<String> logSources = benchmarkCmd.logSources;
        // Load the benchmark config and create a benchmark run for it

        Model model = RDFDataMgr.loadModel(configSrc);

        ExperimentConfig cfg = model.listResourcesWithProperty(LSQ.endpoint)
                .nextOptional()
                .map(x -> x.as(ExperimentConfig.class))
                .orElse(null);

        if(cfg != null) {
            LsqProcessor processor = createLsqProcessor(cfg);

            Flowable<ResourceInDataset> flow = createLsqRdfFlow(rdfizeCmd);

            Flowable<Dataset> dsFlow = flow.map(rid -> {
                LsqQuery q = rid.as(LsqQuery.class);
                // processor.applyForQueryOrWebLogRecord(rid, false);
                processor.benchmark(q);

                return rid;
            }).map(ResourceInDataset::getDataset);

            RDFDataMgrRx.writeDatasets(dsFlow, new FileOutputStream(FileDescriptor.out), RDFFormat.TRIG_PRETTY);
        }

//
//        String endpoint = benchmarkCmd.endpoint;
//
//        LsqProcessor processor = createLsqProcessor(benchmarkCmd);
//
//        String datasetIri = benchmarkCmd.dataset;
//        String serviceUrl = benchmarkCmd.endpoint;
//
    }


    @Deprecated
    public static LsqProcessor createLsqProcessor(ExperimentConfig cfg) throws FileNotFoundException, IOException, ParseException {

        // The experiment IRI is distributionLabel + current timestamp
        // Note, that distribution + timestamp nails down the hardware resources used to run the experiment
//        String distributionIri = benchmarkCmd.dataset;
//        String distributionId = UriToPathUtils.resolvePath(distributionIri).toString()
//                .replace('/', '-');
//
//        String timestamp = Instant.now().toString();
//
//        String defaultExperimentId = "x-" + distributionId + "_" + timestamp;
//        String defaultExperimentIri = benchmarkCmd.baseIri + defaultExperimentId;

        DataRefSparqlEndpoint se = Objects.requireNonNull(cfg.getDataRef());



        LsqConfigImpl config = new LsqConfigImpl();
        config
            .setPrefixSources(Collections.emptyList())
            .setHttpUserAgent(cfg.getUserAgent())
            .setDatasetSize(cfg.getDatasetSize())
            .setBenchmarkEndpoint(se.getServiceUrl())
            .addBenchmarkDefaultGraphs(se.getDefaultGraphs())
            .setBenchmarkQueryExecutionTimeoutInMs(cfg.getQueryTimeout() == null ? null : cfg.getQueryTimeout().multiply(new BigDecimal(1000)).longValue())
            .setRdfizerQueryExecutionEnabled(true)
            .setDelayInMs(cfg.getRequestDelay() == null ? null : cfg.getRequestDelay().multiply(new BigDecimal(1000)).longValue())
            .setExperimentId(cfg.getIdentifier())
            .setExperimentIri(cfg.getURI())
            .setDatasetLabel(cfg.getDatasetLabel())
            ;

        LsqProcessor result = LsqUtils.createProcessor(config);
        return result;
    }

}
