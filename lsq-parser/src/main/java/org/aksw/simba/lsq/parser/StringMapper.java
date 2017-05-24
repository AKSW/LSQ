package org.aksw.simba.lsq.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import com.google.common.base.Strings;
import com.google.common.util.concurrent.AtomicLongMap;

public class StringMapper
    implements Mapper
{
	private static final Logger logger = LoggerFactory.getLogger(StringMapper.class);

    // The pattern is composed of string parts and
    protected List<String> pattern = new ArrayList<>();
    protected Map<String, Mapper> fieldToMapper = new HashMap<>();
    protected Map<String, Pattern> fieldToPattern = new HashMap<>();
    protected Set<String> isOptional = new HashSet<>();
    //protected ReversibleMap<String, String> fieldToCat = new ReversibleMapImpl<>();


    protected AtomicLongMap<String> fieldTypeToIndex = AtomicLongMap.create();

    public void skipPattern(String str) {
        String fieldName = allocateFieldName("ignored");

        pattern.add(fieldName);
        Pattern p = Pattern.compile("^" + str);
        fieldToPattern.put(fieldName, p);
    }

    public void ignoreField(String str) {
        String fieldCat = "ignored";
        Long index = fieldTypeToIndex.getAndIncrement(fieldCat);

        String fieldName = fieldCat + "[" + index + "]";

        //pattern.add(new Item(true, fieldName));
        pattern.add(fieldName);
        Pattern p = Pattern.compile(str);
        fieldToPattern.put(fieldName, p);
    }

    public void addString(String str) {
        if(!Strings.isNullOrEmpty(str)) {
            //pattern.add(new Item(false, str));
        	pattern.add(str);
        }
    }

    public void addField(Property property, String patternStr, Mapper mapper, boolean optional) {
    	Mapper m = new NestedPropertyMapper(property, mapper);

    	String fieldCat = property.getLocalName();
    	String fieldName = allocateFieldName(fieldCat);

    	Pattern pat = Pattern.compile("^" + patternStr);

    	addField(fieldName, pat, m, optional);
    }

    public void addField(Property property, String patternStr, Class<?> clazz) {
        RDFDatatype rdfDatatype = TypeMapper.getInstance().getTypeByClass(clazz);
        addField(property, patternStr, rdfDatatype);
    }

    public void addField(Property property, String patternStr, RDFDatatype rdfDatatype) {
        String fieldName = property.getLocalName();
        addField(fieldName, property, patternStr, rdfDatatype, false);
    }

    public String allocateFieldName(String fieldCat) {
        Long index = fieldTypeToIndex.getAndIncrement(fieldCat);

        String result = fieldCat + "[" + index + "]";

        return result;
    }

    public void addField(String fieldCat, Property property, String patternStr, RDFDatatype rdfDatatype, boolean optional) {
        String fieldName = allocateFieldName(fieldCat);

        Pattern pat = Pattern.compile("^" + patternStr);
        Mapper mapper = new PropertyMapper(property, rdfDatatype);

        addField(fieldName, pat, mapper, optional);
    }

    public void addField(String fieldName, Pattern pat, Mapper mapper, boolean optional) {
        //pattern.add(new Item(true, fieldName));
        pattern.add(fieldName);
        fieldToPattern.put(fieldName, pat);
        fieldToMapper.put(fieldName, mapper);

        if(optional) {
        	isOptional.add(fieldName);
        }
    }

    public void addFieldNoNest(Property property, String patternStr, Mapper m, boolean optional) {
    	String fieldCat = property.getLocalName();
    	String fieldName = allocateFieldName(fieldCat);

    	Pattern pat = Pattern.compile("^" + patternStr);

    	addField(fieldName, pat, m, optional);
    }

    public int parse(Resource r, String str) {

        String remaining = str;

        for(String fieldValue : pattern) {

            String contrib = null;
            boolean isField = fieldToPattern.containsKey(fieldValue);
            if(isField) {
                Pattern pattern = fieldToPattern.get(fieldValue);
                Matcher m = pattern.matcher(remaining);
                if(m.find()) {
                    contrib = m.group();

                    logger.trace("Contribution: " + fieldValue + " -> " + contrib);

                    Mapper mapper = fieldToMapper.get(fieldValue);
                    if(mapper != null) {
                        mapper.parse(r, contrib);
                    }

                    remaining = remaining.substring(m.end());
                } else {
                	boolean optional = isOptional.contains(fieldValue);
                	if(!optional) {
                		throw new RuntimeException("Field '" + fieldValue + "' with pattern '" + pattern + "' does not match '" + remaining + "'");
                	}
                }
            } else {
                if(!remaining.startsWith(fieldValue)) {
                    throw new RuntimeException("Separator '" + fieldValue + "' is not a prefix of '" + remaining + "'");
                }

                remaining = remaining.substring(fieldValue.length());
            }
        }

        return 1;
    }

    public String unparse(Resource r) {
        StringBuilder sb = new StringBuilder();
        for(String fieldValue : pattern) {

            String contrib;
            boolean isField = fieldToPattern.containsKey(fieldValue);
            if(isField) {
                Mapper mapper = fieldToMapper.get(fieldValue);
                contrib = mapper == null ? "" : mapper.unparse(r);
            } else {
                contrib = fieldValue;
            }

            sb.append(contrib);
        }

        String result = sb.toString();
        return result;
    }

    @Override
    public String toString() {
        String result = pattern.stream()
                .map(item -> {
                	boolean isIgnored = !fieldToMapper.containsKey(item);

                	String prefix = isIgnored
                		? ""
                		: item + ":";

                	Pattern pat = fieldToPattern.get(item);
                    String r = pat != null
                            ? "{" + prefix + pat + "}"
                            : item;

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