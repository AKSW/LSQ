package org.aksw.simba.lsq.parser;

import java.util.regex.Pattern;

import org.apache.jena.datatypes.RDFDatatype;
import com.google.common.base.Converter;

public class FieldSpec {

    protected String fieldType;
    protected String fieldId;

    protected boolean isOptional;

    protected Pattern pattern;

    protected Converter<String, String> normalizer;
    protected RDFDatatype rdfTypeMapper;
    protected Mapper mapper;


    public String getFieldType() {
        return fieldType;
    }

    public FieldSpec setFieldType(String fieldType) {
        this.fieldType = fieldType;
        return this;
    }

    public String getFieldId() {
        return fieldId;
    }

    public FieldSpec setFieldId(String fieldId) {
        this.fieldId = fieldId;
        return this;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public FieldSpec setOptional(boolean isOptional) {
        this.isOptional = isOptional;
        return this;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public FieldSpec setPattern(Pattern pattern) {
        this.pattern = pattern;
        return this;
    }

    public Converter<String, String> getNormalizer() {
        return normalizer;
    }

    public FieldSpec setNormalizer(Converter<String, String> normalizer) {
        this.normalizer = normalizer;
        return this;
    }

    public RDFDatatype getRdfTypeMapper() {
        return rdfTypeMapper;
    }

    public FieldSpec setRdfTypeMapper(RDFDatatype rdfTypeMapper) {
        this.rdfTypeMapper = rdfTypeMapper;
        return this;
    }

    public Mapper getMapper() {
        return mapper;
    }

    public FieldSpec setMapper(Mapper mapper) {
        this.mapper = mapper;
        return this;
    }

}
