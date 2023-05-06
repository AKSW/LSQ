package org.aksw.simba.lsq;

import org.aksw.simba.lsq.enrich.core.QueryStatistics2;
import org.aksw.simba.lsq.spinx.model.SpinQueryEx;
import org.aksw.simba.lsq.util.SpinUtilsOld;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.spinrdf.arq.ARQ2SPIN;

public class TestLsqQueryJoinType {
    //@Test
    public void test() {
        String queryStr = "PREFIX tcga: <http://tcga.deri.ie/schema/>  SELECT  ?expValue  WHERE {   {    ?s	tcga:bcr_patient_barcode	<http://tcga.deri.ie/TCGA-37-3789>.      <http://tcga.deri.ie/TCGA-37-3789>	tcga:result	?results.     ?results  tcga:RPKM ?expValue.   } UNION   {    ?uri	tcga:bcr_patient_barcode	<http://tcga.deri.ie/TCGA-37-3789>.      <http://tcga.deri.ie/TCGA-37-3789>	tcga:result	?geneResults.     ?geneResults  tcga:scaled_estimate ?expValue.   } }";
        Query query = QueryFactory.create(queryStr);

        Model model = ModelFactory.createDefaultModel();
        Resource featureRes = model.createResource();

        ARQ2SPIN arq2spin = new ARQ2SPIN(model);
        SpinQueryEx spinRes = arq2spin.createQuery(query, null).as(SpinQueryEx.class);


        Resource outputRes = model.createResource();

        SpinUtilsOld.enrichWithHasTriplePattern(spinRes, spinRes);
        //QueryStatistics2.enrichResourceWithQueryFeatures(featureRes, query);
        QueryStatistics2.getDirectQueryRelatedRDFizedStats(spinRes, spinRes);
        RDFDataMgr.write(System.out, spinRes.getModel(), RDFFormat.TURTLE);

    }
}
