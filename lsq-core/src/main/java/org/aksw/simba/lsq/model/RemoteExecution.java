package org.aksw.simba.lsq.model;

import java.util.Calendar;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
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

	@Iri(LSQ.Strs.userAgent)
	String getUserAgent();
	RemoteExecution setUserAgent(String userAgent);

	@Iri(LSQ.Strs.hostHash)
	String getHostHash();
	RemoteExecution setHostHash(String hostHash);

	@Iri(LSQ.Strs.atTime)
	Calendar getTimestamp();
	RemoteExecution setTimestamp(Calendar calendar);

	@Iri(LSQ.Strs.endpoint)
	@IriType
	String getEndpointUrl();
	RemoteExecution setEndpointUrl(String endpointUrl);

	@Iri(LSQ.Strs.sequenceId)
	Long getSequenceId();
	RemoteExecution setSequenceId(Long value);
}
