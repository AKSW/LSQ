package org.aksw.simba.lsq;

import org.aksw.jenax.arq.util.syntax.QueryHash;
import org.aksw.jenax.reprogen.core.MapperProxyUtils;
import org.aksw.simba.lsq.enricher.core.LsqEnrichments;
import org.aksw.simba.lsq.model.LsqQuery;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.junit.Test;

public class TestLsqModel {
    @Test
    public void test01() {
        Model model = ModelFactory.createDefaultModel();
        // Query query = QueryFactory.create("SELECT ?o ?p ?s { ?s ?p ?o }");
        Query query = QueryFactory.create("SELECT ?p ?o { ?s ?p ?o } OFFSET 5 LIMIT 10");
        LsqQuery before = model.createResource().as(LsqQuery.class);
        QueryHash hash = QueryHash.createHash(query);
        System.out.println(hash);

        if (true) {
            return;
        }

        before.setHash(hash.toString());
        before.setQueryAndHash(query);
        before = MapperProxyUtils.skolemize("http://lsq.aksw.org/", before).as(LsqQuery.class);
        // before.setHash("--hash--");
        before = LsqEnrichments.enrichWithFullSpinModelCore(before);
        before = LsqEnrichments.enrichWithStaticAnalysis(before);
        LsqQuery after = MapperProxyUtils.skolemize("http://lsq.aksw.org/", before).as(LsqQuery.class);
        RDFDataMgr.write(System.out, after.getModel(), RDFFormat.TURTLE_BLOCKS);
    }
}
