package org.aksw.simba.lsq.upgrade.v0_0_1_to_1_0_0;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;

public class MainLsqUpgrade {
    public static void main(String[] args) {
        Model model = RDFDataMgr.loadModel(args[0]);

        SparqlService ss = FluentSparqlService.from(model).create();

        System.out.println(ServiceUtils.fetchInteger(ss.getQueryExecutionFactory().createQueryExecution("PREFIX lsq: <http://lsq.aksw.org/vocab#> SELECT (COUNT(*) AS ?x) { ?s lsq:hasRemoteExecution ?o  }"), Vars.x));
        ss.getUpdateExecutionFactory().createUpdateProcessor("PREFIX lsq: <http://lsq.aksw.org/vocab#> DELETE { ?s lsq:hasRemoteExecution ?o } INSERT {  ?s lsq:hasRemoteExec ?o } WHERE { ?s lsq:hasRemoteExecution ?o }").execute();
        System.out.println(ServiceUtils.fetchInteger(ss.getQueryExecutionFactory().createQueryExecution("PREFIX lsq: <http://lsq.aksw.org/vocab#> SELECT (COUNT(*) AS ?x) { ?s lsq:hasRemoteExecution ?o  }"), Vars.x));
    }


}
