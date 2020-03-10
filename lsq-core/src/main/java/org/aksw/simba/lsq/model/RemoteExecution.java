package org.aksw.simba.lsq.model;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface RemoteExecution
	extends Resource
{
	@Iri(LSQ.Strs.host)
	String getHost();
	RemoteExecution setHost(String host);

//	@Iri(LSQ.Strs.timestamp)
//	Calendar getTimestamp();
//	RemoteExecution setTimestamp(Calendar calendar);
	
	@Iri(LSQ.Strs.sequenceId)
	Long getSequenceId();
	RemoteExecution setSequenceId(Long value);
}
