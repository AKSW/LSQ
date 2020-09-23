package org.aksw.simba.lsq;

import java.util.Map;

import org.aksw.simba.lsq.util.ElementVisitorFeatureExtractor;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.ext.com.google.common.collect.ImmutableMap;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Resource;
import org.junit.Assert;
import org.junit.Test;


public class TestLsqFeatureExtraction {

    @Test
    public void testFeatureExtraction() {
        Map<Resource, Integer> actual = ElementVisitorFeatureExtractor.getFeatures(
                QueryFactory.create(
                        "SELECT DISTINCT ?s WHERE { { ?s a/a ?o . ?s ?p ?x } UNION { ?x a ?z } } LIMIT 10 OFFSET 1"));

        Map<Resource, Integer> expected = ImmutableMap.<Resource, Integer>builder()
            .put(LSQ.TriplePath, 1)
            .put(LSQ.LinkPath, 2)
            .put(LSQ.SeqPath, 1)
            .put(LSQ.TriplePattern, 2)
            .put(LSQ.Group, 3)
            .put(LSQ.Union, 1)
            .put(LSQ.Select, 1)
            .put(LSQ.Distinct, 1)
            .put(LSQ.Limit, 1)
            .put(LSQ.Offset, 1)
            .build();

        Assert.assertEquals(expected, actual);

    }

}
