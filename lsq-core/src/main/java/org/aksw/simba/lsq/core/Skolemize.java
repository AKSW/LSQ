package org.aksw.simba.lsq.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.ResourceUtils;

public class Skolemize {
    public static void skolemize(Resource r) {
        Map<Resource, String> map = new HashMap<>();


        BiFunction<Resource, List<Property>, String> fn = (x, path) -> "test";

        skolemize(r, r, Collections.emptyList(), fn, map);

        map.entrySet().forEach(e -> ResourceUtils.renameResource(e.getKey(), e.getValue()));
    }

    public static void skolemize(Resource baseResource, Resource targetResource, List<Property> path, BiFunction<Resource, List<Property>, String> fn, Map<Resource, String> map) {
        for(Statement stmt : targetResource.listProperties().toSet()) {
            RDFNode o = stmt.getObject();
            if(o.isAnon()) {
                Resource or = o.asResource();
                String uri = fn.apply(baseResource, path);
                if(uri != null) {
                    map.put(or, uri);
                }

                Property p = stmt.getPredicate();

                List<Property> newPath = new ArrayList<>(path);
                newPath.add(p);

                skolemize(baseResource, or, newPath, fn, map);

            }
        }
    }


}
