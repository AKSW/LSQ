package org.aksw.simba.lsq.util;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aksw.beast.vocabs.PROV;
import org.aksw.jena_sparql_api.sparql.ext.datatypes.RDFDatatypeDelegate;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.impl.XSDDateTimeType;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.AtomicLongMap;

class Item {
    protected boolean isField;
    protected String str;

    public Item(boolean isField, String str) {
        super();
        this.isField = isField;
        this.str = str;
    }

    public boolean isField() {
        return isField;
    }

    public String getValue() {
        return str;
    }
}

class StringMapper
    implements Mapper
{
    // The pattern is composed of string parts and
    protected List<Item> pattern = new ArrayList<>();
    protected Map<String, Mapper> fieldToMapper = new HashMap<>();
    protected Map<String, Pattern> fieldToPattern = new HashMap<>();

    protected AtomicLongMap<String> fieldTypeToIndex = AtomicLongMap.create();

    public void ignoreField(String str) {
        String fieldCat = "ignored";
        Long index = fieldTypeToIndex.getAndIncrement(fieldCat);

        String fieldName = fieldCat + "[" + index + "]";

        pattern.add(new Item(true, fieldName));
        Pattern p = Pattern.compile(str);
        fieldToPattern.put(fieldName, p);
    }

    public void addString(String str) {
        if(!Strings.isNullOrEmpty(str)) {
            pattern.add(new Item(false, str));
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
        pattern.add(new Item(true, fieldName));
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

    public Resource parse(Resource r, String str) {

        String remaining = str;

        for(Item item : pattern) {
            String fieldValue = item.getValue();

            String contrib = null;
            if(item.isField()) {
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

        return r;

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
        for(Item item : pattern) {
            String fieldValue = item.getValue();

            String contrib;
            if(item.isField()) {
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
                    String val = item.getValue();
                    return item.isField()
                            ? "{" + val + ":" + fieldToPattern.get(val) + "}"
                            : val;
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

interface Mapper {
    Resource parse(Resource r, String lexicalForm);
    String unparse(Resource r);
}

class FixMapper
    implements Mapper {

    protected Mapper delegate;
    protected String prefix;
    protected String suffix;

    public FixMapper(Mapper delegate, String prefix, String suffix) {
        super();
        this.delegate = delegate;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public Resource parse(Resource r, String lexicalForm) {
        boolean isPrefixMatch = prefix == null || lexicalForm.startsWith(prefix);
        boolean isSuffixMatch = suffix == null || lexicalForm.endsWith(suffix);

        boolean isAccepted = isPrefixMatch && isSuffixMatch;

        if(isAccepted) {
            delegate.parse(r, lexicalForm);
        }

        return r;
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

class PropertyMapper
    implements Mapper
{
    protected Property property;
    protected RDFDatatype rdfDatatype;

    public PropertyMapper(Property property, RDFDatatype rdfDatatype) {
        super();
        this.property = property;
        this.rdfDatatype = rdfDatatype;
    }

    public Resource parse(Resource r, String lexicalForm) {
        Object value = rdfDatatype.parse(lexicalForm);
        r.addLiteral(property, value);

        return r;
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

/**
 * Wrapper around an underlying rdf datatype where the lexical value is restricted
 * by a regex pattern
 *
 * @author raven
 *
 */
class RDFDatatypeRestricted
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


class RDFDatatypeDateFormat
    extends RDFDatatypeDelegate
{
    protected Class<?> clazz;
    protected DateFormat dateFormat;

    public RDFDatatypeDateFormat(DateFormat dateFormat) {
        super(new XSDDateTimeType("dateTime"));
        this.clazz = Date.class;
        this.dateFormat = dateFormat;
    }

    @Override
    public Class<?> getJavaClass() {
        return clazz;
    }

    public String unparse(Object value) {
        Date date = (Date) value;
        String result = dateFormat.format(date);
//        Calendar cal = new GregorianCalendar();
//        cal.setTime(date);
//        XSDDateTime tmp = new XSDDateTime(cal);
//        String result = super.unparse(tmp);
        return result;
    }

    @Override
    public Object parse(String lexicalForm) {
        try {
        Date date = dateFormat.parse(lexicalForm);
            //Object tmp = super.parse(lexicalForm);
            //XSDDateTime xsd = (XSDDateTime) tmp;
            //Calendar cal = xsd.asCalendar();
            Calendar calendar = new GregorianCalendar();//Calendar.getInstance();
            calendar.setTime(date);

            Date result = calendar.getTime();
            return result;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}




public class WebLogParser {

    public static void main(String[] args) {
        Map<String, BiConsumer<StringMapper, String>> map = createWebServerLogStringMapperConfig();

        String logLine = "127.0.0.1 - - [06/Nov/2016:05:12:49 +0100] \"GET /icons/ubuntu-logo.png HTTP/1.1\" 200 3623 \"http://localhost/\" \"Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:49.0) Gecko/20100101 Firefox/49.0\"";

        StringMapper mapper = StringMapper.create("%h %l %u %t \"%r\" %>s %b", map::get);

        System.out.println(logLine);
        System.out.println(mapper);

        Resource x = mapper.parse(ModelFactory.createDefaultModel().createResource(), logLine);
        RDFDataMgr.write(System.out, x.getModel(), RDFFormat.TURTLE);

        System.out.println(mapper);

        //Resource r = ModelFactory.createDefaultModel().createResource();
        x
            .removeAll(PROV.atTime)
            .removeAll(LSQ.verb)
            .removeAll(LSQ.host)
            .addLiteral(PROV.atTime, new Date())
            .addLiteral(LSQ.verb, "GET")
            .addLiteral(LSQ.host, "0.0.0.0");

        System.out.println(mapper.unparse(x));
    }

    private static final Logger logger = LoggerFactory
            .getLogger(WebLogParser.class);

    private static Map<String, WebLogParser> formatRegistry;

    public static Map<String, WebLogParser> getFormatRegistry() {
        if(formatRegistry == null) {
            formatRegistry = new HashMap<>();

            formatRegistry.put("apache", new WebLogParser(apacheLogEntryPattern, apacheDateFormat));
            formatRegistry.put("virtuoso", new WebLogParser(virtuosoLogEntryPattern, virtuosoDateFormat));
            formatRegistry.put("distributed", new WebLogParser(distributedLogEntryPattern, apacheDateFormat));
            formatRegistry.put("bio2rdf", new WebLogParser(bio2rdfLogEntryPattern, apacheDateFormat));
        }

        return formatRegistry;
    }


    public static final String requestPattern
            =  "(?<verb>\\S+)\\s+"
            +  "(?<path>\\S+)\\s+"
            +  "(?<protocol>\\S+)";

    /**
     * Map from suffix to a function that based on an optional argument
     * returns a regex fragment
     *
     * combined: "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-agent}i\""
     *
     * @return
     */
    public static Map<String, BiConsumer<StringMapper, String>> createWebServerLogStringMapperConfig() {
        Map<String, BiConsumer<StringMapper, String>> result = new HashMap<>();

        result.put("h", (m, x) -> m.addField(LSQ.host, "[^\\s]+", String.class));
        result.put("l", (m, x) -> m.ignoreField("\\S+"));
        result.put("u", (m, x) -> m.addField(LSQ.user, "\\S+", String.class));
        result.put("t", (m, x) -> {
            DateFormat dateFormat = x == null
                    ? apacheDateFormat
                    : new SimpleDateFormat(x);

            RDFDatatype rdfDatatype = new RDFDatatypeDateFormat(dateFormat);
            PropertyMapper mapper = new PropertyMapper(PROV.atTime, rdfDatatype);

            m.addString("[");
            m.addField(PROV.atTime, "[^]]*", rdfDatatype);
            //addField("timestamp", mapper); // The field name is optional - it is used in the generated regex
            m.addString("]");
        });


        result.put("r", (m, x) -> {
            m.addField(LSQ.verb, "\\S*", String.class);
            m.addString(" ");
            m.addField(LSQ.path, "\\S*", String.class);
            m.addString(" ");
            m.addField(LSQ.protocol, "[^\\s\"]*", String.class);
        });

        result.put(">s", (m, x) -> m.ignoreField("\\d{3}"));
        result.put("b", (m, x) -> m.ignoreField("\\d+"));

//      result.put("b", (x) -> "(?<bytecount>\\d+)");



//        result.put("h", (x) -> "(?<host>[^\\s]+)");
//        result.put("l", (x) -> "(\\S+)"); // TODO define proper regex
//        result.put("u", (x) -> "(?<user>\\S+)");
//        result.put("t", (x) -> "\\[(?<time>)[^]]*\\]");   //"(\\[(?<time>[\\w:/]+\\s[+\\-]\\d{4})\\])");
//
//        result.put("r", (x) -> requestPattern);
//        result.put(">s", (x) -> "(?<response>\\d{3})");
//        result.put("b", (x) -> "(?<bytecount>\\d+)");


        //result.put("i", (x) -> "(\\[(?<time>[\\w:/]+\\s[+\\-]\\d{4})\\])");

        return result;
    }

    // Pattern: percent followed by any non-white space char sequence that ends on alphanumeric chars



    // 10.0.0.0 [13/Sep/2015:07:57:48 -0400] "GET /robots.txt HTTP/1.0" 200 3485 4125 "http://cu.bio2rdf.org/robots.txt" "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)" - "-"
    public static String bio2rdfLogEntryPatternStr
        = "^"
        + "(?<host>[^\\s]+) "
        + "\\[(?<time>[^]]*)\\] "
        + "\""
        +  "(?<verb>\\S+)\\s+"
        +  "(?<path>\\S+)\\s+"
        +  "(?<protocol>\\S+)"
        + "\" "
        + "(?<response>\\d+) "
        + "(?<bytecount>\\d+) "
        + "(?<unknown>\\d+) "
        + "\"(?<referer>[^\"]+)\""
        ;


    //9c6a991dbf3332fdc973c5b8461ba79f [30/Apr/2010 00:00:00 -0600] "R" "/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&should-sponge=&query=SELECT+DISTINCT+%3Fcity+%3Flatd%0D%0AFROM+%3Chttp%3A%2F%2Fdbpedia.org%3E%0D%0AWHERE+%7B%0D%0A+%3Fcity+%3Chttp%3A%2F%2Fdbpedia.org%2Fproperty%2FsubdivisionName%3E+%3Chttp%3A%2F%2Fdbpedia.org%2Fresource%2FNetherlands%3E.%0D%0A+%3Fcity+%3Chttp%3A%2F%2Fdbpedia.org%2Fproperty%2Flatd%3E+%3Flatd.%0D%0A%7D&format=text%2Fhtml&debug=on&timeout=2200"
    public static String virtuosoLogEntryPatternStr
            = "^"
            + "(?<host>[^\\s]+) "
            + "\\[(?<time>[\\w:/ ]+\\s[+\\-]\\d{4})\\] "
            + "\"(?<unknown>.+?)\" "
            + "\"(?<path>.+?)\""
            ;

//    cu.drugbank.bio2rdf.org 109.245.1.153 [13/Sep/2015:06:41:12 -0400] "GET /sparql?default-graph-uri=&query=PREFIX+drugbank%3A+%3Chttp%3A%2F%2Fbio2rdf.org%2Fdrugbank%3A%3E%0D%0ASELECT+%3FP+%3FO%0D%0AWHERE+%7B%0D%0A+drugbank%3ADB00125+%3FP+%3FO%0D%0A%7D&format=text%2Fhtml&timeout=0&debug=on HTTP/1.1" 200 3228 5305 "http://drugbank.bio2rdf.org/sparql" "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/537.36" - "-"
    public static String distributedLogEntryPatternStr
        = "^"
        + "((?<target>[^\\s]+)\\s+)?" // optional group
        + "(?<host>[^\\s]+)\\s+"
        + "\\[(?<time>[\\w:/ ]+\\s[+\\-]\\d{4})\\]\\s+"
        //+ "(\\S+) "
        //+ "\"(?<request>.+?)\" "
        + "\""
        +  "(?<verb>\\S+)\\s+"
        +  "(?<path>\\S+)\\s+"
        +  "(?<protocol>\\S+)"
        + "\"\\s+"
        + "(?<response>\\d{3})\\s+"
        + "(?<bytecount>\\d+)\\s+"
//        + "\"(?<referer>[^\"]+)\""
        ;

    // 127.0.0.1 - - [06/Nov/2016:05:12:49 +0100] "GET /icons/ubuntu-logo.png HTTP/1.1" 200 3623 "http://localhost/" "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:49.0) Gecko/20100101 Firefox/49.0"
    public static String apacheLogEntryPatternStr
            = "^"
            + "(?<host>[^\\s]+) "
            + "(\\S+) "
            + "(?<user>\\S+) "
            + "\\[(?<time>[\\w:/]+\\s[+\\-]\\d{4})\\] "
            //+ "\"(?<request>.+?)\" "
            + "\""
            +  "(?<verb>\\S+)\\s+"
            +  "(?<path>\\S+)\\s+"
            +  "(?<protocol>\\S+)"
            + "\" "
            + "(?<response>\\d{3}) "
            //+ "(?<bytecount>\\d+) "
            //+ "\"(?<referer>[^\"]+)\""
            ;
//    String foo = ""
//            + "\"(?<agent>[^\"]*)\""
//            ;

    public static String requestParserStr = "(?<verb>\\S+)\\s+(?<path>\\S+)\\s+(?<protocol>\\S+)";

    public static final Pattern apacheLogEntryPattern = Pattern.compile(apacheLogEntryPatternStr);
    public static final Pattern virtuosoLogEntryPattern = Pattern.compile(virtuosoLogEntryPatternStr);
    public static final Pattern distributedLogEntryPattern = Pattern.compile(distributedLogEntryPatternStr);
    public static final Pattern bio2rdfLogEntryPattern = Pattern.compile(bio2rdfLogEntryPatternStr);

    public static final Pattern requestParser = Pattern.compile(requestParserStr);

    // 17/Apr/2011:06:47:47 +0200
    public static final DateFormat apacheDateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");

    // 30/Apr/2010 00:00:00 -0600
    public static final DateFormat virtuosoDateFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss Z");

/*
    private String hostname;
    private Date date;
    private ApacheLogRequest request;
    private String response;
    private long byteCount;
    private String referer;
    private String userAgent;
*/

    protected PatternMatcher patternMatcher;
    protected DateFormat dateFormat;

    public WebLogParser(Pattern pattern, DateFormat dateFormat) {
        this(new PatternMatcherImpl(pattern), dateFormat);
    }

    public WebLogParser(PatternMatcher patternMatcher, DateFormat dateFormat) {
        this.patternMatcher = patternMatcher;
        this.dateFormat = dateFormat;
    }

    public static String encodeUnsafeCharacters(String uri) {
        String result = uri
                .replace("{", "%7B")
                .replace("}", "%7D")
                ;

        return result;
    }

    /**
     * Returns the provided resource if the string could be parsed.
     * Otherwise, returns null
     *
     * @param str
     * @param inout
     * @return
     */
    public boolean parseEntry(String str, Resource inout) {
        //Matcher m = regexPattern.matcher(str);
        Map<String, String> m = patternMatcher.apply(str);
//System.out.println(m);
        //List<String> groupNames = Arrays.asList("host", "user", "request", "path", "protocol", "verb"

        boolean result;
        if(m != null) {
            result = true;

            ResourceUtils.addLiteral(inout, LSQ.host, m.get("host"));
            ResourceUtils.addLiteral(inout, LSQ.user, m.get("user"));

//            String request = m.get("request");
//            ResourceUtils.addLiteral(inout, LSQ.request, request);

            // Parse the request part into http verb, path and protocol
//            if(request != null) {
//                Matcher n = requestParser.matcher(request);
//                if(n.find()) {
            String pathStr = Objects.toString(m.get("path"));

            ResourceUtils.addLiteral(inout, LSQ.protocol, m.get("protocol"));
            ResourceUtils.addLiteral(inout, LSQ.path, pathStr);
            ResourceUtils.addLiteral(inout, LSQ.verb, m.get("verb"));

            if(pathStr != null) {

                pathStr = encodeUnsafeCharacters(pathStr);


                // Parse the path and extract sparql query string if present
                String mockUri = "http://example.org/" + pathStr;
                try {
                    URI uri = new URI(mockUri);
                    List<NameValuePair> qsArgs = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8.name());
                    String queryStr = qsArgs.stream()
                        .filter(x -> x.getName().equals("query"))
                        .findFirst()
                        .map(x -> x.getValue())
                        .orElse(null);

                    if(queryStr != null) {
                        inout.addLiteral(LSQ.query, queryStr);
                    }
                } catch (Exception e) {
                    //System.out.println(mockUri.substring(244));
                    logger.warn("Could not parse URI: " + mockUri, e);
                }
            }

            String timestampStr = m.get("time");
            if(timestampStr != null) {
                Date date;
                try {
                    date = dateFormat.parse(timestampStr);
                    Calendar cal = new GregorianCalendar();
                    cal.setTime(date);
                    inout.addLiteral(PROV.atTime, cal);
                } catch (ParseException e) {
                    inout.addLiteral(LSQ.processingError, "Failed to parse timestamp: " + timestampStr);
                }
            }
        } else {
            result = false;
        }

        return result;
    }
}
