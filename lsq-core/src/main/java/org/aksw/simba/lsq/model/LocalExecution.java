package org.aksw.simba.lsq.model;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.simba.lsq.vocab.LSQ;

public interface LocalExecution {
	@Iri(LSQ.Strs.runTimeMs)
	Integer getRuntimeInMs();
	LocalExecution setRuntimeInMs(Integer runtimeInMs);
	
	@Iri(LSQ.Strs.resultSize)
	Integer getResultSetSize();
	LocalExecution setResultSetSize();
}
