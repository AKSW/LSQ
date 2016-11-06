package org.aksw.simba.lsq.util;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.simba.lsq.vocab.PROV;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebLogParser {

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
            + "(?<bytecount>\\d+) "
            + "\"(?<referer>[^\"]+)\""
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

    public void parseEntry(String str, Resource inout) {
        //Matcher m = regexPattern.matcher(str);
        Map<String, String> m = patternMatcher.apply(str);

        //List<String> groupNames = Arrays.asList("host", "user", "request", "path", "protocol", "verb"

        if(m != null) {
            ResourceUtils.addLiteral(inout, LSQ.host, m.get("host"));
            ResourceUtils.addLiteral(inout, LSQ.user, m.get("user"));

//            String request = m.get("request");
//            ResourceUtils.addLiteral(inout, LSQ.request, request);

            // Parse the request part into http verb, path and protocol
//            if(request != null) {
//                Matcher n = requestParser.matcher(request);
//                if(n.find()) {
            String pathStr = m.get("path");

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
        }
    }
}
