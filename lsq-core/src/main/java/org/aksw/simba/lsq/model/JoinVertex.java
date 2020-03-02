package org.aksw.simba.lsq.model;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.spin.model.Variable;

public interface JoinVertex
	extends Resource
{
	@Iri(LSQ.Strs.proxyFor)
	Variable getSpinVariable();

	@Iri(LSQ.Strs.joinVertexDegree)
	Integer getJoinVertexDegree();

	@Iri(LSQ.Strs.joinVertexType)
	Resource getJoinVertexType();
	JoinVertex setJoinVertexType(Resource joinVertexType);
	
	//LSQ.joinVertexDegree, degree).addProperty(LSQ.joinVertexType
}
