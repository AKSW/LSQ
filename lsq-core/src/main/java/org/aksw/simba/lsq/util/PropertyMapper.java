package org.aksw.simba.lsq.util;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class PropertyMapper
    implements Mapper
{
    protected Property property;
    protected RDFDatatype rdfDatatype;

    public PropertyMapper(Property property, RDFDatatype rdfDatatype) {
        super();
        this.property = property;
        this.rdfDatatype = rdfDatatype;
    }

    public int parse(Resource r, String lexicalForm) {
        Object value = rdfDatatype.parse(lexicalForm);
        r.addLiteral(property, value);

        return 1;
        //return r;
    }

    public String unparse(Resource r) {
        String result;
        if(r.hasProperty(property)) {
            Object value = r.getProperty(property).getLiteral().getValue();
            result = rdfDatatype.unparse(value);
        } else {
            result = "";
        }
        return result;
    }
}