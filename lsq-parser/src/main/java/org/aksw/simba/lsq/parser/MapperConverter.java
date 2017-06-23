package org.aksw.simba.lsq.parser;

import org.apache.jena.rdf.model.Resource;

import com.google.common.base.Converter;

public class MapperConverter
    extends MapperDelegate
{
    public MapperConverter(Mapper delegate, Converter<String, String> converter) {
        super(delegate);
        this.converter = converter;
    }

    protected Converter<String, String> converter;

    @Override
    public int parse(Resource r, String lexicalForm) {
        String str = converter.convert(lexicalForm);
        int result = super.parse(r, str);
        return result;
    }

    @Override
    public String unparse(Resource r) {
        String str = super.unparse(r);
        String result = converter.reverse().convert(str);
        return result;
    }
}
