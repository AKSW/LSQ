package org.aksw.simba.lsq.cli.main;

import java.util.List;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.rdf.model.Resource;

public class DcatUtils {

    public List<Resource> findDistributions(QueryExecutionFactory qef, String searchString) {
        String keywordQueryStr = "SELECT DISTINCT ?s { ?s a dcat:Dataset ; ?s rdfs:keyword ?k }";
        //QueryUtils.injectFilter(query, expr);
        return null;
    }
}
