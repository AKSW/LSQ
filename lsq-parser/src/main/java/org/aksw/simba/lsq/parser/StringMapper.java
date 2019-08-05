package org.aksw.simba.lsq.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Converter;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.AtomicLongMap;

public class StringMapper
    implements Mapper
{
    private static final Logger logger = LoggerFactory.getLogger(StringMapper.class);

    // The pattern is composed of string parts and
//    protected List<String> pattern = new ArrayList<>();
//    protected Map<String, Mapper> fieldToMapper = new HashMap<>();
//    protected Map<String, Pattern> fieldToPattern = new HashMap<>();
//    protected Set<String> isOptional = new HashSet<>();
    //protected ReversibleMap<String, String> fieldToCat = new ReversibleMapImpl<>();

    protected List<FieldSpec> fieldSpecs = new ArrayList<>();


    protected AtomicLongMap<String> fieldTypeToIndex = AtomicLongMap.create();

    public void skipPattern(String str) {
        String fieldName = allocateFieldId("ignored");
        Pattern p = Pattern.compile("^" + str);

        //pattern.add(fieldName);
        //fieldToPattern.put(fieldName, p);

        fieldSpecs.add(new FieldSpec()
                .setFieldType("ignored")
                .setFieldId(fieldName)
                .setPattern(p));
    }

    public void skipPattern(String str, String outPlaceholderStr) {
        String fieldName = allocateFieldId("ignored");
        Pattern p = Pattern.compile("^" + str);

        fieldSpecs.add(new FieldSpec()
                .setFieldType("ignored")
                .setFieldId(fieldName)
                .setPattern(p)
                .setMapper(new FixMapper(null, outPlaceholderStr, "")));

//        pattern.add(fieldName);
//        fieldToPattern.put(fieldName, p);
//        fieldToMapper.put(fieldName, );
    }

    public void ignoreField(String str) {
        ignoreField(str, "");
    }

    /**
     *
     * @param str
     * @param outPlaceholderStr The string representing the ignored field in the final output (e.g. "-")
     */
    public void ignoreField(String str, String outPlaceholderStr) {
        String fieldType = "ignored";
        Long index = fieldTypeToIndex.getAndIncrement(fieldType);

        String fieldName = fieldType + "[" + index + "]";
        Pattern p = Pattern.compile(str);

        //pattern.add(new Item(true, fieldName));
//        pattern.add(fieldName);
//        fieldToPattern.put(fieldName, p);
//
//        fieldToMapper.put(fieldName, new FixMapper(null, outPlaceholderStr, ""));

        fieldSpecs.add(new FieldSpec()
                .setFieldType("ignored")
                .setFieldId(fieldName)
                .setPattern(p)
                .setMapper(new FixMapper(null, outPlaceholderStr, "")));
    }

    public void addString(String str) {
        if(!Strings.isNullOrEmpty(str)) {
            //pattern.add(new Item(false, str));
//            pattern.add(str);

            Pattern p = Pattern.compile(Pattern.quote(str));
            fieldSpecs.add(new FieldSpec()
                    //.setFieldType("ignored")
                    //.setFieldId(fieldName)
                    .setPattern(p)
                    .setMapper(new FixMapper(null, str, "")));
        }
    }

    public void addField(Property property, String patternStr, Mapper mapper, boolean optional) {
        Mapper m = new NestedPropertyMapper(property, mapper);

        String fieldType = property.getLocalName();
        String fieldName = allocateFieldId(fieldType);

        Pattern pat = Pattern.compile("^" + patternStr);

        addField(fieldName, pat, m, optional);
    }

    public void addField(Property property, String patternStr, Class<?> clazz, Converter<String, String> converter) {
        RDFDatatype rdfDatatype = TypeMapper.getInstance().getTypeByClass(clazz);
        addField(property, patternStr, rdfDatatype, converter);
    }

    public void addField(Property property, String patternStr, Class<?> clazz) {
        RDFDatatype rdfDatatype = TypeMapper.getInstance().getTypeByClass(clazz);
        addField(property, patternStr, rdfDatatype);
    }

    public void addField(Property property, String patternStr, RDFDatatype rdfDatatype) {
        String fieldName = property.getLocalName();
        addField(fieldName, property, patternStr, rdfDatatype, false, null);
    }

    public void addField(Property property, String patternStr, RDFDatatype rdfDatatype, Converter<String, String> converter) {
        String fieldName = property.getLocalName();
        addField(fieldName, property, patternStr, rdfDatatype, false, converter);
    }

    public String allocateFieldId(String fieldType) {
        Long index = fieldTypeToIndex.getAndIncrement(fieldType);

        String result = fieldType + "[" + index + "]";

        return result;
    }

    public void addField(String fieldType, Property property, String patternStr, RDFDatatype rdfDatatype, boolean optional, Converter<String, String> converter) {
        String fieldName = allocateFieldId(fieldType);

        Pattern pat = Pattern.compile("^" + patternStr);
        Mapper mapper = new PropertyMapper(property, rdfDatatype);

        if(converter != null) {
            mapper = new MapperConverter(mapper, converter);
        }

        addField(fieldName, pat, mapper, optional);
    }

    public void addField(String fieldName, Pattern pat, Mapper mapper, boolean optional) {
        //pattern.add(new Item(true, fieldName));
//        pattern.add(fieldName);
//        fieldToPattern.put(fieldName, pat);
//        fieldToMapper.put(fieldName, mapper);
//
//        if(optional) {
//            isOptional.add(fieldName);
//        }

        FieldSpec fieldSpec = new FieldSpec()
                .setFieldId(fieldName)
                .setPattern(pat)
                .setMapper(mapper)
                .setOptional(optional);

        fieldSpecs.add(fieldSpec);
    }

    public void addFieldNoNest(Property property, String patternStr, Mapper m, boolean optional) {
        String fieldType = property.getLocalName();
        String fieldName = allocateFieldId(fieldType);

        Pattern pat = Pattern.compile("^" + patternStr);

        addField(fieldName, pat, m, optional);
    }

    public int parse(Resource r, String str) {

        String remaining = str;

        for(FieldSpec fieldSpec : fieldSpecs) { //String fieldValue : pattern) {
            String fieldId = fieldSpec.getFieldId();

            String contrib = null;
            //boolean isField = fieldToPattern.containsKey(fieldValue);
            //if(isField) {
                //Pattern pattern = fieldToPattern.get(fieldValue);
            Pattern pattern = fieldSpec.getPattern();
            Matcher m = pattern.matcher(remaining);
            if(m.find()) {
                contrib = m.group();

                logger.trace("Contribution: " + fieldId + " -> " + contrib);

                Mapper mapper = fieldSpec.getMapper();
                if(mapper != null) {
                    mapper.parse(r, contrib);
                }

                remaining = remaining.substring(m.end());
            } else {
                boolean optional = fieldSpec.isOptional();
                if(!optional) {
                    throw new RuntimeException("Field '" + fieldId + "' with pattern '" + pattern + "' does not match '" + remaining + "'");
                }
            }
//            } else {
//                if(!remaining.startsWith(fieldValue)) {
//                    throw new RuntimeException("Separator '" + fieldValue + "' is not a prefix of '" + remaining + "'");
//                }
//
//                remaining = remaining.substring(fieldValue.length());
//            }
        }

        return 1;
    }

    public String unparse(Resource r) {
        StringBuilder sb = new StringBuilder();
        for(FieldSpec fieldSpec : fieldSpecs) {

            String contrib;
            Mapper mapper = fieldSpec.getMapper();


            contrib = mapper == null ? "" : mapper.unparse(r);
            //contrib = contrib == null ? "" :

//            boolean isField = fieldToPattern.containsKey(fieldValue);
//            if(isField) {
//                Mapper mapper = fieldToMapper.get(fieldValue);
//            } else {
//                contrib = fieldValue;
//            }

            sb.append(contrib);
        }

        String result = sb.toString();
        return result;
    }

    @Override
    public String toString() {
        String result = fieldSpecs.stream()
                .map(fieldSpec -> {
                    boolean isIgnored = fieldSpec.getMapper() == null; //!fieldToMapper.containsKey(item);

                    String prefix = isIgnored
                        ? ""
                        : fieldSpec.getFieldId() + ":";

                    Pattern pat = fieldSpec.getPattern();
                    String r = pat != null
                            ? "{" + prefix + pat + "}"
                            : "" + fieldSpec;

                    return r;
                })
                .collect(Collectors.joining());
        return result;
    }

    public static Pattern tokenPattern = Pattern.compile("%(\\{([^}]*)\\})?([^% ]*\\w+)"); //, Pattern.MULTILINE | Pattern.DOTALL);

    public static StringMapper create(String str, Function<String, BiConsumer<StringMapper, String>> map) {

        StringMapper result = new StringMapper();

        Matcher m = tokenPattern.matcher(str);
        int s = 0;
        while(m.find()) {
            String sp = str.substring(s, m.start());
            result.addString(sp);

            String arg = m.group(2);
            String token = m.group(3);

            BiConsumer<StringMapper, String> argToRegex = map.apply(token);
            //Objects.requireNonNull(argToRegex);
            if(argToRegex == null) {
                System.out.println("No entry for: " + token);
            }

            BiConsumer<StringMapper, String> tokenProcessor = map.apply(token);
            if(tokenProcessor == null) {
                throw new RuntimeException("No processor for: " + token);
            }
            tokenProcessor.accept(result, arg);

            s = m.end();
        }
        String sp = str.substring(s, str.length());
        result.addString(sp);

        return result;
    }
}