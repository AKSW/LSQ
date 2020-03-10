package org.aksw.simba.lsq.model;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface LocalExecution
	extends Resource
{
	@Iri(LSQ.Strs.runTimeMs)
	Integer getRuntimeInMs();
	LocalExecution setRuntimeInMs(Integer runtimeInMs);
	
	@Iri(LSQ.Strs.resultSize)
	Integer getResultSetSize();
	LocalExecution setResultSetSize();
}
