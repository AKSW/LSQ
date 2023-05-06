package org.aksw.simba.lsq.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;
import org.spinrdf.model.Variable;

public interface JoinVertex
	extends Resource
{
	@Iri(LSQ.Terms.proxyFor)
	Variable getSpinVariable();

	@Iri(LSQ.Terms.joinVertexDegree)
	Integer getJoinVertexDegree();

	@Iri(LSQ.Terms.joinVertexType)
	Resource getJoinVertexType();
	JoinVertex setJoinVertexType(Resource joinVertexType);
	
	//LSQ.joinVertexDegree, degree).addProperty(LSQ.joinVertexType
}
