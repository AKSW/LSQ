package org.aksw.simba.lsq;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.validation.ValidationUtil;

public class TestLsqValidationWithShacl {

	
	@Test
	public void testLsqValidationWithShacl() {
		Model shaclModel = RDFDataMgr.loadModel("lsq.shacl.ttl");

		Model dataModel = RDFDataMgr.loadModel("lsq-tests/triple-pattern-selectivity/tpsel01.ttl");

		// Perform the validation of everything, using the data model
		// also as the shapes model - you may have them separated
		Resource report = ValidationUtil.validateModel(dataModel, shaclModel, true);
		
		// Print violations
		System.out.println(ModelPrinter.get().print(report.getModel()));
	}
}
