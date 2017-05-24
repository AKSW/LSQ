package org.aksw.simba.lsq.parser;

import java.util.Objects;

import org.apache.jena.rdf.model.Resource;

public class FixMapper
    implements Mapper {

    protected Mapper delegate;
    protected String prefix;
    protected String suffix;

    public FixMapper(Mapper delegate, String prefix, String suffix) {
        super();
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(suffix);

        this.delegate = delegate;
        this.prefix = prefix;
        this.suffix = suffix;
        //this.prefix = prefix != null ? prefix : "";
        //this.suffix = suffix != null ? suffix : "";
    }

    @Override
    public int parse(Resource r, String lexicalForm) {
        boolean isPrefixMatch = lexicalForm.startsWith(prefix);
        boolean isSuffixMatch = lexicalForm.endsWith(suffix);

        boolean isAccepted = isPrefixMatch && isSuffixMatch;

        int result = isAccepted
        		? delegate.parse(r, lexicalForm.substring(prefix.length(), lexicalForm.length() - suffix.length()))
        		: 0;

        return result;
    }

    @Override
    public String unparse(Resource r) {
        StringBuilder sb = new StringBuilder();
        if(prefix != null) {
            sb.append(prefix);
        }

        String s = delegate.unparse(r);
        sb.append(s);

        if(suffix != null) {
            sb.append(suffix);
        }

        String result = sb.toString();
        return result;
    }
}