package org.aksw.simba.lsq.parser;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.simba.lsq.vocab.PROV;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Converter;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

public class WebLogParser {

    public static Map<String, Mapper> loadRegistry(Model model) {
        List<Resource> rs = model.listResourcesWithProperty(RDF.type, LSQ.WebAccessLogFormat).toList();

        Map<String, Mapper> result = rs.stream()
            .filter(r -> r.hasProperty(LSQ.pattern))
            .collect(Collectors.toMap(
                    r -> r.getLocalName(),
                    r -> create(r.getProperty(LSQ.pattern).getString())
            ));

        return result;
    }


    private static final Logger logger = LoggerFactory
            .getLogger(WebLogParser.class);

    private static Map<String, WebLogParser> formatRegistry;

    public static Map<String, WebLogParser> getFormatRegistry() {
        if(formatRegistry == null) {
            formatRegistry = new HashMap<>();

//            formatRegistry.put("apache", new WebLogParser(apacheLogEntryPattern, apacheDateFormat));
//            formatRegistry.put("virtuoso", new WebLogParser(virtuosoLogEntryPattern, virtuosoDateFormat));
//            formatRegistry.put("distributed", new WebLogParser(distributedLogEntryPattern, apacheDateFormat));
//            formatRegistry.put("bio2rdf", new WebLogParser(bio2rdfLogEntryPattern, apacheDateFormat));
        }

        return formatRegistry;
    }

    public static Mapper create(String pattern) {
        Map<String, BiConsumer<StringMapper, String>> map = createWebServerLogStringMapperConfig();

        Mapper result = StringMapper.create(pattern, map::get);

        return result;
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
        result.put("l", (m, x) -> m.ignoreField("\\S+", "-"));
        result.put("u", (m, x) -> m.addField(LSQ.user, "\\S+", String.class));
        result.put("t", (m, x) -> {
            DateFormat dateFormat = x == null
                    ? apacheDateFormat
                    : new SimpleDateFormat(x);

            RDFDatatype rdfDatatype = new RDFDatatypeDateFormat(dateFormat);

            m.addString("[");
            m.addField(PROV.atTime, "[^]]*", rdfDatatype);
            m.addString("]");
        });

        // custom extension for dates without '[]'
        result.put("C", (m, x) -> {
            DateFormat dateFormat = x == null
                    ? apacheDateFormat
                    : new SimpleDateFormat(x);

            RDFDatatype rdfDatatype = new RDFDatatypeDateFormat(dateFormat);

            // Hacky approach to convert datetime pattern to regex - based on
            // https://stackoverflow.com/questions/6928267/converting-simpledateformat-date-format-to-regular-expression
            String regexFormat = x.replaceAll("[mHsSdMy]", "\\\\d");
            
            m.addField(PROV.atTime, regexFormat, rdfDatatype);
        });


        result.put("r", (m, x) -> {
            m.addField(LSQ.verb, "[^\\s\"]*", String.class);
            m.skipPattern("\\s*", " ");
            m.addField(LSQ.requestPath, "[^\\s\"]*", String.class);
            m.skipPattern("\\s*", " ");
            m.addField(LSQ.protocol, "[^\\s\"]*", String.class);
        });


        Converter<String, String> dashToZeroConverter = ConverterChain.create(
                Maps.asConverter(HashBiMap.create(Collections.singletonMap("-", "0"))),
                Converter.identity());


        result.put(">s", (m, x) -> m.addField(LSQ.statusCode, "-|\\d{3}", Integer.class, dashToZeroConverter));
        result.put("b", (m, x) -> m.addField(LSQ.numResponseBytes, "-|\\d+", Integer.class, dashToZeroConverter));
        //result.put(">s", (m, x) -> m.ignoreField("-|\\d{3}"));
        //result.put("b", (m, x) -> m.ignoreField("-|\\d+"));

        result.put("U", (m, x) -> {
            m.addField(LSQ.requestPath, "[^\\s\"?]*", String.class);
        });
//
        result.put("q", (m, x) -> {
            Mapper mapper = new FixMapper(new PropertyMapper(LSQ.queryString, String.class), "?", "");

            m.addFieldNoNest(LSQ.queryString, "[^\\s\"]*", mapper, true);
        });
//


        // Headers
        result.put("i", (m, x) -> {
            Property p = ResourceFactory.createProperty("http://example.org/header#" + x);
            Mapper subMapper = PropertyMapper.create(p, String.class);

            m.addField(LSQ.headers, "[^\"]*", subMapper, false);
        });

        // %v The canonical ServerName of the server serving the request.
        result.put("v", (m, x) -> {
            m.addField(LSQ.property("serverName"), "[^\\s\"]*", String.class);
        });


        result.put("sparql", (m, x) -> {
            m.addField(LSQ.query, ".*", String.class);
        });

        result.put("encsparql", (m, x) -> {        	
            m.addField(LSQ.query, "[^\\s]*", String.class, Converter.from(StringUtils::urlDecode, StringUtils::urlEncode));
        });

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

            Optional.ofNullable(m.get("host")).ifPresent(o -> inout.addLiteral(LSQ.host, o));
            Optional.ofNullable(m.get("user")).ifPresent(o -> inout.addLiteral(LSQ.user, o));

//            String request = m.get("request");
//            ResourceUtils.addLiteral(inout, LSQ.request, request);

            // Parse the request part into http verb, path and protocol
//            if(request != null) {
//                Matcher n = requestParser.matcher(request);
//                if(n.find()) {
            String pathStr = Objects.toString(m.get("path"));

            Optional.ofNullable(m.get("protocol")).ifPresent(o -> inout.addLiteral(LSQ.protocol, o));
            Optional.ofNullable(m.get(pathStr)).ifPresent(o -> inout.addLiteral(LSQ.requestPath, o));
            Optional.ofNullable(m.get("verb")).ifPresent(o -> inout.addLiteral(LSQ.verb, o));

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


    public static void extractRawQueryString(Resource r) {
        List<Function<Resource, String>> extractors = Arrays.asList(
                x -> x.hasProperty(LSQ.requestPath) ? extractQueryString(x.getProperty(LSQ.requestPath).getString()) : null,
                x -> x.hasProperty(LSQ.queryString) ? extractQueryString2(x.getProperty(LSQ.queryString).getString()) : null
        );

        extractors.stream()
            .map(e -> e.apply(r))
            .filter(s -> s != null)
            .findFirst()
            .ifPresent(s -> r.addLiteral(LSQ.query, s));
    }

    public static String extractQueryString2(String uri) {
        List<NameValuePair> qsArgs = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
        String result = qsArgs.stream()
            .filter(x -> x.getName().equals("query"))
            .findFirst()
            .map(x -> x.getValue())
            .orElse(null);
        return result;
    }

    // TODO extract the query also from referrer fields
    public static String extractQueryString(String pathStr) {
        String result = null;

        if(pathStr != null) {

            pathStr = encodeUnsafeCharacters(pathStr);


            // Parse the path and extract sparql query string if present
            //String mockUri = "http://example.org/" + pathStr;
            try {
                //URI uri = new URI(pathStr);
                int queryStrOffset = pathStr.indexOf("?");

                result = queryStrOffset >= 0 ? extractQueryString2(pathStr.substring(queryStrOffset + 1)) : null;
            } catch (Exception e) {
                //System.out.println(mockUri.substring(244));
                logger.warn("Could not parse URI: " + pathStr, e);
                //logger.warn("Could not parse URI: " + mockUri, e);
            }
        }

        return result;
    }

}
