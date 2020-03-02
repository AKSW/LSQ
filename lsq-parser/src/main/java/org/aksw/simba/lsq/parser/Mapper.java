package org.aksw.simba.lsq.parser;

import org.apache.jena.rdf.model.Resource;

/**
 * This class differs from Jena's TypeMapper that it does not parse strings into individual
 * Nodes but Resources.
 * 
 * @author raven
 *
 */
public interface Mapper {

	/**
	 *
	 * @param r The resource into which to parse the given string as a property's value.
	 * @param lexicalForm
	 * @return The number of properties parsed from the string
	 */
    int parse(Resource r, String lexicalForm);
    String unparse(Resource r);
}