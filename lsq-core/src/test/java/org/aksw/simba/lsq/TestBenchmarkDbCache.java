package org.aksw.simba.lsq;

import org.aksw.simba.lsq.core.LsqBenchmarkProcessor;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;

public class TestBenchmarkDbCache {

    /**
     * Test whether queries are correctly cached in the database
     *
     */
    public void testBenchmarkDbCache() {
        Dataset cache = DatasetFactory.create();

        LsqBenchmarkProcessor.createProcessor()
    }
}
