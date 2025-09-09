package org.aksw.simba.lsq;

import java.util.Map;

import org.aksw.simba.lsq.util.ElementVisitorFeatureExtractor;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Assert;
import org.junit.Test;

public class TestLsqFeatureExtraction {
    @Test
    public void testFeatureExtraction() {
        Query query = QueryFactory.create("""
            SELECT DISTINCT ?s WHERE {
                { ?s a/a ?o . ?s ?p ?x }
              UNION
                { ?x a ?z }
            }
            LIMIT 10
            OFFSET 1
            """);

        Map<Resource, Integer> actualFeatures = ElementVisitorFeatureExtractor.getFeatures(query);

        Map<Resource, Integer> expectedFeatures = Map.of(
            LSQ.TriplePath, 1,
            LSQ.LinkPath, 2,
            LSQ.SeqPath, 1,
            LSQ.TriplePattern, 2,
            LSQ.Group, 3,
            LSQ.Union, 1,
            LSQ.Select, 1,
            LSQ.Distinct, 1,
            LSQ.Limit, 1,
            LSQ.Offset, 1);

        Assert.assertEquals(expectedFeatures, actualFeatures);
    }
}
