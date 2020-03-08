package org.aksw.simba.lsq.cli.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.aksw.jena_sparql_api.common.DefaultPrefixes;
import org.aksw.jena_sparql_api.io.json.GroupedResourceInDataset;
import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.utils.model.ResourceInDataset;
import org.aksw.jena_sparql_api.utils.model.ResourceInDatasetImpl;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqAnalyze;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqInvert;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqMain;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqProbe;
import org.aksw.simba.lsq.cli.main.cmd.CmdLsqRdfize;
import org.aksw.simba.lsq.core.LsqConfigImpl;
import org.aksw.simba.lsq.core.LsqProcessor;
import org.aksw.simba.lsq.core.LsqUtils;
import org.aksw.simba.lsq.core.ResourceParser;
import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.sparql_integrate.ngs.cli.cmd.CmdNgsMap;
import org.aksw.sparql_integrate.ngs.cli.cmd.CmdNgsSort;
import org.aksw.sparql_integrate.ngs.cli.main.MainCliNamedGraphStream;
import org.aksw.sparql_integrate.ngs.cli.main.NamedGraphStreamOps;
import org.aksw.sparql_integrate.ngs.cli.main.ResourceInDatasetFlowOps;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.riot.RDFDataMgr;
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
		
		CmdLsqProbe cmdProbe = new  CmdLsqProbe();
		CmdLsqRdfize cmdRdfize = new  CmdLsqRdfize();
		CmdLsqInvert cmdInvert = new  CmdLsqInvert();
		CmdLsqAnalyze cmdAnalyze = new  CmdLsqAnalyze();

		JCommander jc = JCommander.newBuilder()
				.addObject(cmdMain)
				.addCommand("probe", cmdProbe)
				.addCommand("rdfize", cmdRdfize)
				.addCommand("invert", cmdInvert)
				.addCommand("analyze", cmdAnalyze)
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
			probe(cmdProbe);
			break;
		}
		case "rdfize": {
			rdfize(cmdRdfize);
			break;
		}
		case "invert": {
			invert(cmdInvert);
			break;
		}
		case "analyze": {
			analyze(cmdAnalyze);
			break;
		}
		default:
			throw new RuntimeException("Unsupported command: " + cmd);
		}
	}

	public static void rdfize(CmdLsqRdfize cmdRdfize) throws Exception {
		String logFormat = cmdRdfize.inputLogFormat;
		List<String> logSources = cmdRdfize.nonOptionArgs;
		
		List<String> rawPrefixSources = cmdRdfize.prefixSources;
		Iterable<String> prefixSources = LsqUtils.prependDefaultPrefixSources(rawPrefixSources);
		Function<String, SparqlStmt> sparqlStmtParser = LsqUtils.createSparqlParser(prefixSources);
		
		
		Map<String, ResourceParser> logFmtRegistry = LsqUtils.createDefaultLogFmtRegistry();

		
		Flowable<ResourceInDataset> logRdfEvents = Flowable
			.fromIterable(logSources)
			.flatMap(logSource -> {
				Flowable<ResourceInDataset> st = LsqUtils.createReader(logSource, logFormat, logFmtRegistry);
		    	
				String filename;
				if(logSource == null) {
					filename = "stdin";
				} else {
					Path path = Paths.get(logSource);
					filename = path.getFileName().toString();
				}

		    	/*
		    	logger.info("Processing log source " + logSource);
		    	
				String effectiveLogFormat;
				if(Strings.isNullOrEmpty(logFormat)) {
					List<Entry<String, Number>> formats = LsqUtils.probeLogFormat(logFmtRegistry, logSource);
					if(formats.isEmpty()) {
						throw new RuntimeException("Could not auto-detect a log format");
					}
					
//						if(formats.size() != 1) {
//							throw new RuntimeException("Expected probe to return exactly 1 log format for source " + logSource + ", got: " + formats);
//						}
					effectiveLogFormat = formats.get(0).getKey();
					logger.info("Auto-selected format [" + effectiveLogFormat + "] among auto-detected candidates " + formats);
				} else {
					effectiveLogFormat = logFormat;
				}

		        ResourceParser webLogParser = logFmtRegistry.get(effectiveLogFormat);
		        
		        Flowable<ResourceInDataset> st = webLogParser.parse(() -> RDFDataMgr.open(logSource));
*/
				// st = st.doOnNext(x -> System.out.println("GOT GRAPH: " + x.getDataset().getDefaultModel().size() + " " + Lists.newArrayList(x.getDataset().listNames())));

		        //st = LsqUtils.postProcessStream(st, in, true);
		        
		        long[] nextId = {0};
		        st = st
		        	//.zipWith(, zipper)
		        	.doOnNext(x -> x.addLiteral(LSQ.sequenceId, nextId[0]++))	
		        	.map(x -> {
		        	long seqId = x.getProperty(LSQ.sequenceId).getLong();
		        	String graphAndResourceIri = "urn:lsq:" + filename + "-" + seqId;
		        	ResourceInDataset xx = ResourceInDatasetImpl.renameResource(x, graphAndResourceIri);
		        	xx = ResourceInDatasetImpl.renameGraph(xx, graphAndResourceIri);		        	
		        	
		        	try {
		        		LsqUtils.postProcessSparqlStmt(xx, sparqlStmtParser);
		        	} catch(Exception e) {
		                xx.addLiteral(LSQ.processingError, e.toString());
		        	}

		        	// Remove text and query properties, as LSQ.text is
		        	// the polished one
		        	// xx.removeAll(LSQ.query);
		        	// xx.removeAll(RDFS.label);

		        	return xx;
		        })
		        .filter(x -> x != null);

				return st;
			});
		
		
		if(cmdRdfize.slimMode) {
			CmdNgsMap cmd = new CmdNgsMap();
			cmd.stmts.add("lsq-slimify.sparql");
//			cmd.nonOptionArgs.addAll(cmdInvert.nonOptionArgs);
			
//			JenaSystem.init();
			//RDFDataMgrEx.loadQueries("lsq-slimify.sparql", PrefixMapping.Extended);
			
//			SparqlStmtUtils.processFile(pm, "lsq-slimify.sparql");
//			MainCliNamedGraphStream.createMapper2(); //map(DefaultPrefixes.prefixes, cmd);
			FlowableTransformer<ResourceInDataset, ResourceInDataset> mapper =
					MainCliNamedGraphStream.createMapper(PrefixMapping.Extended, cmd,
							r -> r.getDataset(),
							(r, ds) -> r.inDataset(ds));
			
			
			logRdfEvents = logRdfEvents
					.compose(mapper);
		}
		
		if(!cmdRdfize.noMerge) {
			CmdNgsSort sortCmd = new CmdNgsSort();
			
			FlowableTransformer<GroupedResourceInDataset, GroupedResourceInDataset> sorter = ResourceInDatasetFlowOps.createSystemSorter(sortCmd, null);
			logRdfEvents = logRdfEvents
					.compose(ResourceInDatasetFlowOps.groupedResourceInDataset())
					.compose(sorter)
					.flatMap(ResourceInDatasetFlowOps::ungrouperResourceInDataset);
		}		
		
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

	public static void invert(CmdLsqInvert cmdInvert) throws FileNotFoundException, IOException, ParseException {
		CmdNgsMap cmd = new CmdNgsMap();
		cmd.stmts.add("lsq-invert-rdfized-log.sparql");
		cmd.nonOptionArgs.addAll(cmdInvert.nonOptionArgs);
		
		JenaSystem.init();
		NamedGraphStreamOps.map(DefaultPrefixes.prefixes, cmd);
	}

	public static void analyze(CmdLsqAnalyze cmdAnalyze) throws FileNotFoundException, IOException, ParseException {
	}

	public static LsqProcessor createLsqProcessor(CmdLsqAnalyze cmdAnalyze) throws FileNotFoundException, IOException, ParseException {

		LsqConfigImpl config = new LsqConfigImpl();
		config
			.setHttpUserAgent(cmdAnalyze.userAgent)
			.setDatasetSize(cmdAnalyze.datasetSize)
			.setBenchmarkEndpoint(cmdAnalyze.endpoint)
			.addBenchmarkDefaultGraphs(cmdAnalyze.defaultGraphs)
			.setBenchmarkQueryExecutionTimeoutInMs(cmdAnalyze.timeoutInMs)
			.setDelayInMs(cmdAnalyze.delayInMs)
			.setExperimentIri(cmdAnalyze.experimentIri)
			;

		LsqProcessor result = LsqUtils.createProcessor(config);
		return result;
	}

}
