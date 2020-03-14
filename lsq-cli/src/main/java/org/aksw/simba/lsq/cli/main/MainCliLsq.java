package org.aksw.simba.lsq.cli.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Function;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.io.json.GroupedResourceInDataset;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.utils.model.ResourceInDataset;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqAnalyze;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqBenchmark;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqInvert;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqMain;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqProbe;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqRdfize;
import org.aksw.simba.lsq.core.LsqConfigImpl;
import org.aksw.simba.lsq.core.LsqProcessor;
import org.aksw.simba.lsq.core.LsqUtils;
import org.aksw.simba.lsq.core.ResourceParser;
import org.aksw.simba.lsq.core.Skolemize;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.util.NestedResource;
import org.aksw.sparql_integrate.ngs.cli.cmd.CmdNgsMap;
import org.aksw.sparql_integrate.ngs.cli.cmd.CmdNgsSort;
import org.aksw.sparql_integrate.ngs.cli.main.DatasetFlowOps;
import org.aksw.sparql_integrate.ngs.cli.main.MainCliNamedGraphStream;
import org.aksw.sparql_integrate.ngs.cli.main.NamedGraphStreamOps;
import org.aksw.sparql_integrate.ngs.cli.main.ResourceInDatasetFlowOps;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sys.JenaSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;



/**
 * This should become the new main class
 * 
 * @author raven
 *
 */
public class MainCliLsq {
	private static final Logger logger = LoggerFactory.getLogger(MainCliLsq.class); 
	
	// public static void displayUsageIf()
	
	public static void main(String[] args) throws Exception {
		CmdLsqMain cmdMain = new CmdLsqMain();
		
		CmdLsqProbe probeCmd = new  CmdLsqProbe();
		CmdLsqRdfize rdfizeCmd = new  CmdLsqRdfize();
//		CmdLsqInvert invertCmd = new  CmdLsqInvert();
		CmdLsqBenchmark benchmarkCmd = new  CmdLsqBenchmark();
		CmdLsqAnalyze analyzeCmd = new  CmdLsqAnalyze();

		JCommander jc = JCommander.newBuilder()
				.addObject(cmdMain)
				.addCommand("probe", probeCmd)
				.addCommand("rdfize", rdfizeCmd)
//				.addCommand("invert", invertCmd)
				.addCommand("analyze", analyzeCmd)
				.addCommand("benchmark", benchmarkCmd)
				.build();


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
			benchmark(benchmarkCmd);
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
		if(endpointUrl == null) {
			throw new RuntimeException("Please specify the URL of the endpoint the provided query logs are assigned to.");
		}
		
		
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
							(r, ds) -> r.inDataset(ds));
			
			
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
		RDFDataMgrRx.writeResources(logRdfEvents, System.out, RDFFormat.TRIG_PRETTY);
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
	public static void invert(CmdLsqInvert cmdInvert) throws FileNotFoundException, IOException, ParseException {
		CmdNgsMap cmd = new CmdNgsMap();
		cmd.stmts.add("lsq-invert-rdfized-log.sparql");
		cmd.nonOptionArgs.addAll(cmdInvert.nonOptionArgs);
		
		JenaSystem.init();
		NamedGraphStreamOps.map(DefaultPrefixes.prefixes, cmd);
	}
	
	// FIXME hasRemoteExec needs to be skolemized - this
	// should probably be done in 'rdfize'
	public static void analyze(CmdLsqAnalyze analyzeCmd) throws Exception {
		CmdLsqRdfize rdfizeCmd = new CmdLsqRdfize();
		rdfizeCmd.nonOptionArgs = analyzeCmd.nonOptionArgs;
		
		
		Flowable<ResourceInDataset> flow = createLsqRdfFlow(rdfizeCmd);
		
		Flowable<Dataset> dsFlow = flow.map(rid -> {
			LsqQuery q = rid.as(LsqQuery.class);
		
            NestedResource queryRes = NestedResource.from(q);
            Function<String, NestedResource> queryAspectFn =
            		aspect -> queryRes.nest("-" + aspect);
			
			String queryStr = q.getText();
			Query query = QueryFactory.create(queryStr);
			//SparqlQueryParser parser = SparqlQueryParserImpl.create(); 
			
			LsqProcessor.rdfizeQueryStructuralFeatures(q, queryAspectFn, query);
			
            // Post processing: Remove skolem identifiers
            q.getModel().removeAll(null, Skolemize.skolemId, null);


			return rid;
		})
		.map(ResourceInDataset::getDataset);
		
		RDFDataMgrRx.writeDatasets(dsFlow, System.out, RDFFormat.TRIG_PRETTY);
	}

	public static void benchmark(CmdLsqBenchmark benchmarkCmd) throws Exception {
		CmdLsqRdfize rdfizeCmd = new CmdLsqRdfize();
		rdfizeCmd.nonOptionArgs = benchmarkCmd.nonOptionArgs;
		
		LsqProcessor processor = createLsqProcessor(benchmarkCmd);

		
		Flowable<ResourceInDataset> flow = createLsqRdfFlow(rdfizeCmd);
		
		Flowable<Dataset> dsFlow = flow.map(rid -> {
			processor.applyForQueryOrWebLogRecord(rid, false);

			return rid;
		})
		.map(ResourceInDataset::getDataset);
		
		RDFDataMgrRx.writeDatasets(dsFlow, System.out, RDFFormat.TRIG_PRETTY);
	}
	
	
	public static LsqProcessor createLsqProcessor(CmdLsqBenchmark benchmarkCmd) throws FileNotFoundException, IOException, ParseException {

		LsqConfigImpl config = new LsqConfigImpl();
		config
			.setPrefixSources(Collections.emptyList())
			.setHttpUserAgent(benchmarkCmd.userAgent)
			.setDatasetSize(benchmarkCmd.datasetSize)
			.setBenchmarkEndpoint(benchmarkCmd.endpoint)
			.addBenchmarkDefaultGraphs(benchmarkCmd.defaultGraphs)
			.setBenchmarkQueryExecutionTimeoutInMs(benchmarkCmd.timeoutInMs)
			.setRdfizerQueryExecutionEnabled(true)
			.setDelayInMs(benchmarkCmd.delayInMs)
			.setExperimentIri(benchmarkCmd.experimentIri)
			;

		LsqProcessor result = LsqUtils.createProcessor(config);
		return result;
	}

}
