package org.aksw.simba.lsq;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.junit.Assert;
import org.junit.Test;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

public class TestLsqValidationWithShacl {

	
	@Test
	public void testLsqValidationWithShacl() {
		Model shaclModel = RDFDataMgr.loadModel("lsq.shacl.ttl");

		Model dataModel = RDFDataMgr.loadModel("2019-02-14-single-resource.lsq.ttl");

		// Perform the validation of everything, using the data model
		// also as the shapes model - you may have them separated
		Resource report = ValidationUtil.validateModel(dataModel, shaclModel, true);		
		
		boolean conforms = report.getProperty(SH.conforms).getBoolean();
		
		if(!conforms) {
			// Print violations
			RDFDataMgr.write(System.err, report.getModel(), RDFFormat.TURTLE_PRETTY);
		}
		
		Assert.assertTrue(conforms);		
	}
}
