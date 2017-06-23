package org.aksw.simba.lsq.parser;

import org.apache.jena.rdf.model.Resource;

public class MapperDelegate
    implements Mapper
{
    protected Mapper delegate;

    public MapperDelegate(Mapper delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public int parse(Resource r, String lexicalForm) {
        int result = delegate.parse(r, lexicalForm);
        return result;
    }

    @Override
    public String unparse(Resource r) {
        String result = delegate.unparse(r);
        return result;
    }
}
