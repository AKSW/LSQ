package org.aksw.simba.lsq.cli.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.simba.lsq.core.LsqUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Streams;
import com.google.common.collect.TreeMultimap;



public class MainTestFormat {
	public static void main(String[] args) throws IOException {
		Map<String, Function<InputStream, Stream<Resource>>> registry = LsqUtils.createDefaultLogFmtRegistry();
		
		ResourceLoader loader = new FileSystemResourceLoader();		
		
		for(int i = 0; i < args.length; ++i) {			
			String filename = args[i];

			Multimap<Long, String> report = determineFormat(registry, loader, filename);
			
			List<String> bestCands = Streams.stream(report.asMap().entrySet().iterator())
				.filter(e -> e.getKey() != 0)
				.limit(1)
				.map(Entry::getValue)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
				
			
			System.out.println(filename + "\t" + bestCands);
		}
		
	}


	public static Multimap<Long, String> determineFormat(Map<String, Function<InputStream, Stream<Resource>>> registry, ResourceLoader loader, String filename) {
		org.springframework.core.io.Resource resource = loader.getResource(filename);
		
		// succcessCountToFormat
		Multimap<Long, String> result = TreeMultimap.create(Ordering.natural().reverse(), Ordering.natural());
		
		for(Entry<String, Function<InputStream, Stream<Resource>>> entry : registry.entrySet()) {
			String formatName = entry.getKey();
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
}
