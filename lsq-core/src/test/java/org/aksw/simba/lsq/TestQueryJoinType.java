package org.aksw.simba.lsq;

import org.aksw.simba.lsq.core.LSQARQ2SPIN;
import org.aksw.simba.lsq.core.QueryStatistics2;
import org.aksw.simba.lsq.util.SpinUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.junit.Test;

public class TestQueryJoinType {
	@Test
	public void test() {
		String queryStr = "PREFIX tcga: <http://tcga.deri.ie/schema/>  SELECT  ?expValue  WHERE {   {    ?s	tcga:bcr_patient_barcode	<http://tcga.deri.ie/TCGA-37-3789>.      <http://tcga.deri.ie/TCGA-37-3789>	tcga:result	?results.     ?results  tcga:RPKM ?expValue.   } UNION   {    ?uri	tcga:bcr_patient_barcode	<http://tcga.deri.ie/TCGA-37-3789>.      <http://tcga.deri.ie/TCGA-37-3789>	tcga:result	?geneResults.     ?geneResults  tcga:scaled_estimate ?expValue.   } }";
		Query query = QueryFactory.create(queryStr);

		Model model = ModelFactory.createDefaultModel();
		Resource featureRes = model.createResource();

        LSQARQ2SPIN arq2spin = new LSQARQ2SPIN(model);
        Resource spinRes = arq2spin.createQuery(query, null);
               

		Resource outputRes = model.createResource();

		SpinUtils.enrichWithHasTriplePattern(spinRes, spinRes);
		//QueryStatistics2.enrichResourceWithQueryFeatures(featureRes, query);
		QueryStatistics2.getDirectQueryRelatedRDFizedStats(spinRes, spinRes);
		RDFDataMgr.write(System.out, spinRes.getModel(), RDFFormat.TURTLE);

	}
}
