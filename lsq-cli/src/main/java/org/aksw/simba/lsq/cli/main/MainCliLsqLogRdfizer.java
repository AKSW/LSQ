package org.aksw.simba.lsq.cli.main;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.jena_sparql_api.utils.model.ResourceInDataset;
import org.aksw.jena_sparql_api.utils.model.ResourceInDatasetImpl;
import org.aksw.simba.lsq.core.LsqUtils;
import org.aksw.simba.lsq.core.ResourceParser;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.beust.jcommander.JCommander;

import io.reactivex.Flowable;
import joptsimple.internal.Strings;

public class MainCliLsqLogRdfizer {
	private static final Logger logger = LoggerFactory.getLogger(MainCliLsqLogRdfizer.class);
	
	public static void main(String[] args) throws Exception {
		CommandMain cm = new CommandMain();

		JCommander jc = new JCommander.Builder()
	    	  .addObject(cm)
	    	  .build();

		jc.parse(args);

		String logFormat = cm.inputLogFormat;
		List<String> logSources = cm.nonOptionArgs;
		
		List<String> rawPrefixSources = cm.prefixSources;
		Iterable<String> prefixSources = LsqUtils.prependDefaultPrefixSources(rawPrefixSources);
		Function<String, SparqlStmt> sparqlStmtParser = LsqUtils.createSparqlParser(prefixSources);
		
		
		ResourceLoader loader = new FileSystemResourceLoader(); // new DefaultResourceLoader();
		Map<String, ResourceParser> logFmtRegistry = LsqUtils.createDefaultLogFmtRegistry();
		
		Flowable<ResourceInDataset> logRdfEvents = Flowable
			.fromIterable(logSources)
			.flatMap(logSource -> {
	        	Path path = Paths.get(logSource);
	        	String filename = path.getFileName().toString();

	        	logger.info("Processing log source " + logSource);
	        	
				String effectiveLogFormat;
				if(Strings.isNullOrEmpty(logFormat)) {
					List<Entry<String, Number>> formats = LsqUtils.probeLogFormat(logFmtRegistry, loader, logSource);
					if(formats.isEmpty()) {
						throw new RuntimeException("Could not auto-detect a log format");
					}
					
//					if(formats.size() != 1) {
//						throw new RuntimeException("Expected probe to return exactly 1 log format for source " + logSource + ", got: " + formats);
//					}
					effectiveLogFormat = formats.get(0).getKey();
					logger.info("Auto-selected format [" + effectiveLogFormat + "] among auto-detected candidates " + formats);
				} else {
					effectiveLogFormat = logFormat;
				}

		        ResourceParser webLogParser = logFmtRegistry.get(effectiveLogFormat);
		        
		        // TODO This does not properly close the input stream - we should switch
		        // to rxjava completely
		        Flowable<ResourceInDataset> st = webLogParser.parse(() -> loader.getResource(logSource).getInputStream());
		        //st = LsqUtils.postProcessStream(st, in, true);
		        
		        st = st.map(x -> {
		        	long seqId = x.getProperty(LSQ.sequenceId).getLong();
		        	ResourceInDataset xx = ResourceInDatasetImpl.renameResource(x, filename + "-" + seqId);
		        	
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
		
		
		RDFDataMgrRx.writeResources(logRdfEvents, System.out, RDFFormat.TRIG_PRETTY);
	}
}
