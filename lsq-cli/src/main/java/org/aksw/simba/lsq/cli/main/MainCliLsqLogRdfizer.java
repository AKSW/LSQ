package org.aksw.simba.lsq.cli.main;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.jena_sparql_api.stmt.SparqlStmt;
import org.aksw.simba.lsq.core.LsqUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.ResourceUtils;
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
		Map<String, Function<InputStream, Stream<Resource>>> logFmtRegistry = LsqUtils.createDefaultLogFmtRegistry();
		
		Flowable<Resource> logRdfEvents = Flowable
			.fromIterable(logSources)
			.flatMap(logSource -> {
	        	Path path = Paths.get(logSource);
	        	String filename = path.getFileName().toString();

	        	logger.info("Processing log source " + logSource);
	        	
				String effectiveLogFormat;
				if(Strings.isNullOrEmpty(logFormat)) {
					List<String> formats = LsqUtils.probeLogFormat(logFmtRegistry, loader, logSource);
					if(formats.isEmpty()) {
						throw new RuntimeException("Could not auto-detect a log format");
					}
					
//					if(formats.size() != 1) {
//						throw new RuntimeException("Expected probe to return exactly 1 log format for source " + logSource + ", got: " + formats);
//					}
					effectiveLogFormat = formats.get(0);
					logger.info("Auto-selected format [" + effectiveLogFormat + "] among auto-detected candidates " + formats);
				} else {
					effectiveLogFormat = logFormat;
				}

		        Function<InputStream, Stream<Resource>> webLogParser = logFmtRegistry.get(effectiveLogFormat);
		        
		        // TODO This does not properly close the input stream - we should switch
		        // to rxjava completely
				Flowable<Resource> r = Flowable.fromIterable(() -> {
					InputStream in;
					try {
						in = loader.getResource(logSource).getInputStream();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
			        Stream<Resource> st = webLogParser.apply(in);
			        st = LsqUtils.postProcessStream(st, in, true);
			        
			        st = st.map(x -> {
			        	long seqId = x.getProperty(LSQ.sequenceId).getLong();
			        	Resource xx = ResourceUtils.renameResource(x, filename + "-" + seqId);

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

			        Iterator<Resource> it = st.iterator();
			        return it;
				});


				return r;
			});
		
		
		RDFDataMgrRx.writeResources(logRdfEvents, System.out, RDFFormat.TRIG_PRETTY);
	}
}
