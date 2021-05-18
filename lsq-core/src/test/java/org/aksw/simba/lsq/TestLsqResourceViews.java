package org.aksw.simba.lsq;

import org.aksw.simba.lsq.model.LsqQuery;
import org.aksw.simba.lsq.model.LsqStructuralFeatures;
import org.aksw.simba.lsq.model.RemoteExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;


public class TestLsqResourceViews {

	@Test
	public void testLsqResourceViews() {
		Model m = ModelFactory.createDefaultModel();

		LsqQuery q = m.createResource().as(LsqQuery.class);
		String queryStr = "SELECT * { ?s a ?o }";
		q.setText(queryStr);
		
		
//		q.getRemoteExecutions(RemoteExecution.class)
		q.getRemoteExecutions()
				.add(m.createResource().as(RemoteExecution.class)
			.setHost("host")
			.setSequenceId(0l)
		);
		LsqStructuralFeatures summary = m.createResource().as(LsqStructuralFeatures.class);
		summary.setProjectVarCount(2);
		
		q.setStructuralFeatures(summary);
		
		
		Assert.assertEquals(q.getText(), queryStr);
		Assert.assertEquals((long)q.getStructuralFeatures().getProjectVarCount(), 2l);
		Assert.assertEquals(q.getRemoteExecutions().iterator().next().getHost(), "host");
	}
}
