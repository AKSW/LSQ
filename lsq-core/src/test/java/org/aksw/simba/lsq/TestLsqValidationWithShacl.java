package org.aksw.simba.lsq;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.ValidationReport;
import org.junit.Assert;
import org.junit.Test;

public class TestLsqValidationWithShacl {

	
	@Test
	public void testLsqValidationWithShacl() {
		Model shaclModel = RDFDataMgr.loadModel("lsq.shacl.ttl");

		Model dataModel = RDFDataMgr.loadModel("2019-02-14-single-resource.lsq.ttl");

		// Perform the validation of everything, using the data model
		// also as the shapes model - you may have them separated
        ValidationReport report;
        try {
                report = ShaclValidator.get().validate(shaclModel.getGraph(), dataModel.getGraph());
        } catch (Exception e) {
                RDFDataMgr.write(System.err, shaclModel, RDFFormat.NTRIPLES);
                throw new RuntimeException("Internal error during shacl validation - model printed to stderr", e);
        }
        
        boolean conforms = report.conforms();

//		
//		Resource report = ValidationUtil.validateModel(dataModel, shaclModel, true);		
//		
//		boolean conforms = report.getProperty(SH.conforms).getBoolean();
//		
//		if(!conforms) {
//			// Print violations
//			RDFDataMgr.write(System.err, report.getModel(), RDFFormat.TURTLE_PRETTY);
//		}
//		
		Assert.assertTrue(conforms);		
	}
}
