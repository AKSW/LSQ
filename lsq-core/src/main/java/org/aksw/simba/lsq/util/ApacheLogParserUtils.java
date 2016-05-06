package org.aksw.simba.lsq.util;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.simba.lsq.vocab.PROV;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApacheLogParserUtils {

    private static final Logger logger = LoggerFactory
            .getLogger(ApacheLogParserUtils.class);


    public static String logEntryPatternStr
            = "^"
            + "(?<host>[^\\s]+) "
            + "(\\S+) "
            + "(?<user>\\S+) "
            + "\\[(?<time>[\\w:/]+\\s[+\\-]\\d{4})\\] "
            + "\"(?<request>.+?)\" "
            + "(?<response>\\d{3}) "
            + "(?<bytecount>\\d+) "
            + "\"(?<referer>[^\"]+)\""
            ; String foo = ""
            + "\"(?<agent>[^\"]*)\""
            ;

    public static String requestParserStr = "(?<verb>\\S+)\\s+(?<path>\\S+)\\s+(?<protocol>\\S+)";

    private static final Pattern logEntryPattern = Pattern.compile(logEntryPatternStr);
    private static final Pattern requestParser = Pattern.compile(requestParserStr);

    // 17/Apr/2011:06:47:47 +0200
    private static final DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");

/*
    private String hostname;
    private Date date;
    private ApacheLogRequest request;
    private String response;
    private long byteCount;
    private String referer;
    private String userAgent;
*/

    public static void parseEntry(String str, Resource inout) {
        Matcher m = logEntryPattern.matcher(str);
        if(m.find()) {
            inout.addLiteral(LSQ.host, m.group("host"));
            inout.addLiteral(LSQ.user, m.group("user"));

            String request = m.group("request");
            inout.addLiteral(LSQ.request, request);

            // Parse the request part into http verb, path and protocol
            Matcher n = requestParser.matcher(request);
            if(n.find()) {
                String pathStr = n.group("path");

                inout.addLiteral(LSQ.protocol, n.group("protocol"));
                inout.addLiteral(LSQ.path, pathStr);
                inout.addLiteral(LSQ.verb, n.group("verb"));


                // Parse the path and extract sparql query string if present
                try {
                    URI uri = new URI(pathStr);
                    List<NameValuePair> qsArgs = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8.name());
                    String queryStr = qsArgs.stream()
                        .filter(x -> x.getName().equals("query"))
                        .map(x -> x.getValue())
                        .findFirst()
                        .orElse(null);

                    if(queryStr != null) {
                        inout.addLiteral(LSQ.query, queryStr);
                    }
                } catch (Exception e) {
                    logger.warn("Could not parse URI: " + pathStr);
                }
            }


            String timestampStr = m.group("time");
            Date date;
            try {
                date = dateFormat.parse(timestampStr);
                Calendar cal = new GregorianCalendar();
                cal.setTime(date);
                inout.addLiteral(PROV.atTime, cal);
            } catch (ParseException e) {
                inout.addLiteral(LSQ.runtimeError, "Failed to parse timestamp: " + timestampStr);
            }

        }
    }
}
