package org.aksw.simba.lsq.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.AtomicLongMap;

public class StringMapper
    implements Mapper
{
    // The pattern is composed of string parts and
    protected List<String> pattern = new ArrayList<>();
    protected Map<String, Mapper> fieldToMapper = new HashMap<>();
    protected Map<String, Pattern> fieldToPattern = new HashMap<>();

    protected AtomicLongMap<String> fieldTypeToIndex = AtomicLongMap.create();

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

    public void addField(Property property, String patternStr, Class<?> clazz) {
        RDFDatatype rdfDatatype = TypeMapper.getInstance().getTypeByClass(clazz);
        addField(property, patternStr, rdfDatatype);
    }

    public void addField(Property property, String patternStr, RDFDatatype rdfDatatype) {
        String fieldName = property.getLocalName();
        addField(fieldName, property, patternStr, rdfDatatype);
    }

    public void addField(String fieldCat, Property property, String patternStr, RDFDatatype rdfDatatype) {
        Long index = fieldTypeToIndex.getAndIncrement(fieldCat);

        String fieldName = fieldCat + "[" + index + "]";

        Pattern pat = Pattern.compile("^" + patternStr);
        Mapper mapper = new PropertyMapper(property, rdfDatatype);
        //pattern.add(new Item(true, fieldName));
        pattern.add(fieldName);
        fieldToPattern.put(fieldName, pat);
        fieldToMapper.put(fieldName, mapper);
    }

    //protected PatternMatcher patternMatcher;




//	protected Map<Property, >
//
//	@Override
//	public void accept(Resource r, String source) {
//		Map<String, String> groupToValue = patternMatcher.apply(source);
//
//		groupToValue.forEach((group, v) -> {
//			BiConsumer<Resource, String> parser = groupToParser.get(group);
//			if(parser == null) {
//				throw new RuntimeException("No parser for: " + group);
//			}
//			parser.accept(r, v);
//		});
//	}
    //addParser('%')

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

                    Mapper mapper = fieldToMapper.get(fieldValue);
                    if(mapper != null) {
                        mapper.parse(r, contrib);
                    }

                    remaining = remaining.substring(m.end());
                } else {
                    throw new RuntimeException("Field '" + fieldValue + "' with pattern '" + pattern + "' does not match '" + remaining + "'");
                }
            } else {
                if(!remaining.startsWith(fieldValue)) {
                    throw new RuntimeException("Separator '" + fieldValue + "' is not a prefix of '" + remaining + "'");
                }

                remaining = remaining.substring(fieldValue.length());
            }
        }

        return 1;

        /*
        StringBuilder tmp = new StringBuilder();
        for(Item item : pattern) {
            String fieldValue = item.getValue();

            String contrib = item.isField()
                ? "(<?" + fieldValue + ">" + fieldToPattern.get(fieldValue) + ")"
                : Pattern.quote(fieldValue);

            tmp.append(contrib);
        }

        String patternStr = tmp.toString();
        Pattern pattern = Pattern.compile(patternStr);
        PatternMatcher matcher = new PatternMatcherImpl(pattern);

        Map<String, String> fieldToValue = matcher.apply(str);
        fieldToValue.forEach((k, v) -> {
            Mapper mapper = fieldToMapper.get(k);
            if(mapper != null) {
                mapper.parse(r, v);
            }
        });

        return r;
        //r.addLiteral(property, result);
        */
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
                	Pattern pat = fieldToPattern.get(item);
                    return pat != null
                            ? "{" + item + ":" + pat + "}"
                            : item;
                })
                .collect(Collectors.joining());
        return result;
    }

    public static Pattern tokenPattern = Pattern.compile("%(\\{([^}]*)\\})?(\\S*\\w+)"); //, Pattern.MULTILINE | Pattern.DOTALL);

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

            BiConsumer<StringMapper, String> xxx = map.apply(token);
            xxx.accept(result, arg);

            s = m.end();
        }
        String sp = str.substring(s, str.length());
        result.addString(sp);

        return result;
    }
}