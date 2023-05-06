package org.aksw.simba.lsq.model;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface Host
	extends Resource
{
	@Iri(LSQ.Terms.host)
	String getHost();
	RemoteExecution setHost(String host);
}