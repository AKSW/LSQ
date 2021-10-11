package org.aksw.simba.lsq.core.rx.io.input;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.ResourceInDataset;
import org.aksw.jena_sparql_api.stmt.SparqlStmtUtils;
import org.aksw.simba.lsq.core.ResourceParser;
import org.aksw.simba.lsq.core.io.input.registry.LsqInputFormatRegistry;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

public class LsqProbeUtils {

    private static final Logger logger = LoggerFactory.getLogger(LsqProbeUtils.class);

    public static List<Entry<String, Number>> probeLogFormat(String resource) {
        //FileSystemResourceLoader loader = new FileSystemResourceLoader();
        Map<String, ResourceParser> registry = LsqInputFormatRegistry.createDefaultLogFmtRegistry();

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

//        registry = Collections.singletonMap("wikidata", registry.get("wikidata"));

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
                        .take(1000)
                        .toList()
                        .onErrorReturn(x -> Collections.emptyList())
                        .blockingGet();
            } catch(Exception e) {
                baseItems = Collections.emptyList();
                logger.debug("Probing against format " + formatName + " raised exception", e);
            }

            // int availableItems = baseItems.size();
            double weight = analyzeInformationRatio(baseItems, r -> r.hasProperty(LSQ.processingError));

            result.put(weight, formatName);
        }

        return result;
    }


    /** Analyze the avg number of properties per resource */
    public static double analyzeInformationRatio(
            Collection<? extends Resource> baseItems,
            Predicate<? super Resource> errorCondition) {
        int availableItems = baseItems.size();

        List<Resource> parsedItems = baseItems.stream()
                .filter(r -> !errorCondition.test(r))
                .collect(Collectors.toList());

        long parsedItemCount = parsedItems.size();

        double avgImmediatePropertyCount = parsedItems.stream()
                .mapToInt(r -> r.listProperties().toList().size())
                .average().orElse(0);

        // Weight is the average number of properties multiplied by the
        // fraction of successfully parsed items
        double parsedFraction = availableItems == 0 ? 0 : (parsedItemCount / (double)availableItems);
        double weight = parsedFraction * avgImmediatePropertyCount;
        return weight;
    }

}
