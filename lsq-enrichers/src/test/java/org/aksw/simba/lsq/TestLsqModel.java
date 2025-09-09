package org.aksw.simba.lsq;

import java.util.List;

import org.aksw.jenax.arq.util.syntax.QueryHash;
import org.aksw.jenax.arq.util.triple.GraphUtils;
import org.aksw.jenax.reprogen.core.MapperProxyUtils;
import org.aksw.simba.lsq.enricher.core.LsqEnrichments;
import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.model.LsqStructuralFeatures;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import org.junit.Test;

public class TestLsqModel {
    @Test
    public void test01() {
        String ns = "http://lsq.aksw.org/";

        Dataset ds = DatasetFactory.create();
        LSQ.addPrefixes(ds.getPrefixMapping());

        Model model = ds.getDefaultModel();
        // Query query = QueryFactory.create("SELECT ?o ?p ?s { ?s ?p ?o }");
        Query query = QueryFactory.create("SELECT ?p ?o { ?s ?p ?o } OFFSET 5 LIMIT 10");
        LsqQuery before = model.createResource().as(LsqQuery.class);
        QueryHash hash = QueryHash.createHash(query);

        before.setHash(hash.toString());
        before.setQueryAndHash(query);
        before = MapperProxyUtils.skolemize(ns, before).as(LsqQuery.class);
        // before.setHash("--hash--");
        before = LsqEnrichments.enrichWithFullSpinModelCore(before);
        before = LsqEnrichments.enrichWithStaticAnalysis(before);
        LsqQuery after = MapperProxyUtils.skolemize(ns, before).as(LsqQuery.class);

        // Expect no blank nodes
        List<Node> expectedBnodes = List.of();
        List<Node> actualBnodes = GraphUtils.streamNodes(after.getModel().getGraph())
                .filter(Node::isBlank).toList();
        Assert.assertEquals(expectedBnodes, actualBnodes);

        LsqStructuralFeatures sf = after.getStructuralFeatures();
        Assert.assertEquals(Integer.valueOf(1), sf.getBgpCount());
        Assert.assertEquals(Integer.valueOf(2), sf.getProjectVarCount());
        // RDFDataMgr.write(System.out, ds, RDFFormat.TRIG_BLOCKS);
    }
}
