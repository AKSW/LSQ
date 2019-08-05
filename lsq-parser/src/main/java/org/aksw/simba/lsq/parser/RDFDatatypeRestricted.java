package org.aksw.simba.lsq.parser;

import java.util.regex.Pattern;

import org.aksw.jena_sparql_api.sparql.ext.datatypes.RDFDatatypeDelegate;
import org.apache.jena.datatypes.RDFDatatype;

/**
 * Wrapper around an underlying rdf datatype where the lexical value is restricted
 * by a regex pattern
 *
 * @author raven
 *
 */
public class RDFDatatypeRestricted
    extends RDFDatatypeDelegate
{
    protected Pattern pattern;

    public RDFDatatypeRestricted(RDFDatatype delegate, Pattern pattern) {
        super(delegate);
        this.pattern = pattern;
    }

    @Override
    public boolean isValid(String lexicalForm) {
        boolean result = pattern.matcher(lexicalForm).find();
        result = result && super.isValid(lexicalForm);
        return result;
    }
}