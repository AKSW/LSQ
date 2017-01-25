package org.aksw.simba.lsq.util;

import org.apache.jena.rdf.model.Resource;

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