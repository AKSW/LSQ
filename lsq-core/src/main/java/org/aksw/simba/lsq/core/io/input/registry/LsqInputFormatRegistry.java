package org.aksw.simba.lsq.core.io.input.registry;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.aksw.simba.lsq.core.ResourceParser;
import org.aksw.simba.lsq.core.rx.io.input.LsqRxIo;
import org.aksw.simba.lsq.parser.Mapper;
import org.aksw.simba.lsq.parser.WebLogParser;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class LsqInputFormatRegistry {

    public static Map<String, ResourceParser> createDefaultLogFmtRegistry() {
        Map<String, ResourceParser> result = new LinkedHashMap<>();

        // Load line based log formats
        result.putAll(
                wrap(WebLogParser.loadRegistry(RDFDataMgr.loadModel("default-log-formats.ttl"))));

        // Add custom RDF based log format(s)
        result.put("rdf", in -> LsqRxIo.createResourceStreamFromRdf(in, Lang.NTRIPLES, "http://example.org/"));

        // Add multi-line sparql format
        result.put("sparql", in -> LsqRxIo.createSparqlStream(in));

        return result;
    }



    public static Map<String, ResourceParser> wrap(Map<String, Mapper> webLogParserRegistry) {
         Map<String, ResourceParser> result = webLogParserRegistry.entrySet().stream().map(e -> {
            String name = e.getKey();
            ResourceParser r = inSupp -> LsqRxIo.createResourceStreamFromMapperRegistry(inSupp, webLogParserRegistry::get, name);
            return new SimpleEntry<>(name, r);
        }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return result;
    }



}
