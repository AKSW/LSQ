package org.aksw.simba.lsq.model;

import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;


/**
 * This class is main entry point for accessing information about a query in LSQ.
 * 
 * TODO This model keeps the SPIN representation of a query separate from the LSQ record about it,
 * yet I am not totally sure whether actually these should be just two views of a resource which
 * represents a SPARQL query.
 * 
 * 
 * @author Claus Stadler, Jan 7, 2019
 *
 */
public interface LsqQuery
	extends Resource
{
	@Iri(LSQ.Strs.text)
	String getText();
	LsqQuery setText(String text);
	
	@Iri(LSQ.Strs.hasSpin)
	org.topbraid.spin.model.Query getSpinQuery();
	LsqQuery setSpinQuery(Resource resource);
	
	// TODO We should investigate whether an extension of the model to shacl makes sense
	// The main question is which (sub-)set of all possible
	// sparql queries can be represented as shacl
	
	@Iri(LSQ.Strs.hasStructuralFeatures)
	LsqFeatureSummary getStructuralFeatures();
	LsqQuery setFeatureSummary(Resource r);
	
	@Iri(LSQ.Strs.hasLocalExec)
	<T extends Resource> Set<T> getLocalExecutions(Class<T> itemClazz);

	//Set<>getLocalExecutions();
	
	@Iri(LSQ.Strs.hasRemoteExec)
	<T extends Resource> Set<T> getRemoteExecutions(Class<T> itemClazz);
}

