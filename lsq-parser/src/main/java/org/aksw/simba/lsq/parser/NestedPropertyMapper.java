package org.aksw.simba.lsq.parser;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public class NestedPropertyMapper
    extends MapperDelegate
{
    //protected Directed<Property> property;
    protected Property property;

    public NestedPropertyMapper(Property property, Mapper delegate) {
        super(delegate);
        this.property = property;
    }

    @Override
    public int parse(Resource r, String lexicalForm) {
        Resource subR;
        Statement stmt = r.getProperty(property);
        if(stmt != null) {
            subR = stmt.getResource();
        } else {
            subR = r.getModel().createResource();
            r.addProperty(property, subR);
        }

        int result = super.parse(subR, lexicalForm);

        return result;
    }

    @Override
    public String unparse(Resource r) {
        String result;

        Statement stmt = r.getProperty(property);
        if(stmt != null) {
            Resource subR = stmt.getResource();

            result = super.unparse(subR);
        } else {
            result = null;
        }

        return result;
    }

}
