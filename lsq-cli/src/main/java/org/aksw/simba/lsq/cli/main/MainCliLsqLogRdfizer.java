package org.aksw.simba.lsq.cli.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.rx.RDFDataMgrRx;
import org.aksw.simba.lsq.core.LsqUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFFormat;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.beust.jcommander.JCommander;

import io.reactivex.Flowable;
import joptsimple.internal.Strings;

public class MainCliLsqLogRdfizer {
	public static void main(String[] args) throws Exception {
		CommandMain cm = new CommandMain();

		JCommander jc = new JCommander.Builder()
	    	  .addObject(cm)
	    	  .build();

		jc.parse(args);

		String logFormat = cm.inputLogFormat;
		List<String> sources = cm.nonOptionArgs;
		
		ResourceLoader loader = new FileSystemResourceLoader(); // new DefaultResourceLoader();
		Map<String, Function<InputStream, Stream<Resource>>> logFmtRegistry = LsqUtils.createDefaultLogFmtRegistry();
		
		Flowable<Resource> logRdfEvents = Flowable
			.fromIterable(sources)
			.flatMap(source -> {
				String effectiveLogFormat;
				if(Strings.isNullOrEmpty(logFormat)) {
					List<String> formats = LsqUtils.probeLogFormat(logFmtRegistry, loader, source);
					if(formats.size() != 1) {
						throw new RuntimeException("Expected probe to return exactly 1 log format for source " + source + ", got: " + formats);
					}
					effectiveLogFormat = formats.get(0);
				} else {
					effectiveLogFormat = logFormat;
				}

		        Function<InputStream, Stream<Resource>> webLogParser = logFmtRegistry.get(effectiveLogFormat);
		        
		        // TODO This does not properly close the input stream - we should switch
		        // to rxjava completely
				Flowable<Resource> r = Flowable.fromIterable(() -> {
					InputStream in;
					try {
						in = loader.getResource(source).getInputStream();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
			        Iterator<Resource> it = webLogParser.apply(in).iterator();
			        return it;
				});


				return r;
			});
		
		
		RDFDataMgrRx.writeResources(logRdfEvents, System.out, RDFFormat.TRIG_PRETTY);
	}
}
