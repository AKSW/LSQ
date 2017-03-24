package org.aksw.simba.lsq;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.simba.lsq.core.QueryStatistics2;
import org.aksw.simba.lsq.util.SpinUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.model.Triple;

import com.google.common.collect.Multimap;

public class TestSelectivity {

    @Test
    public void testSelectivity() {
        Model dataModel = RDFDataMgr.loadModel("test-data.ttl");

        Model spinModel = ModelFactory.createDefaultModel();
        Query q = ARQ2SPIN.parseQuery("PREFIX ex:<http://example.org/> SELECT * { ?s ex:p1 ?o1 ; ex:p2 ?o2 }", spinModel);
        //spinModel = q.getModel();

        QueryExecutionFactory qef = FluentQueryExecutionFactory.from(dataModel).create();

        Multimap<Resource, Triple> bgpToTps = SpinUtils.indexBasicPatterns2(spinModel);

        for(Entry<Resource, Collection<Triple>> e : bgpToTps.asMap().entrySet()) {
            Map<Triple, Long> sel = QueryStatistics2.computeSelectivity(qef, e.getValue());
            System.out.println("TP/BGP compatibility counts: " + sel);

            Map<Var, Long> joinVarCounts = QueryStatistics2.fetchCountVarJoin(qef, e.getValue());
            System.out.println("TP/BGP join var counts " + joinVarCounts);
        }

    }
}
