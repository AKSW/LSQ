package org.aksw.simba.lsq.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.simba.lsq.vocab.LSQ;
import org.aksw.simba.lsq.vocab.PROV;
import org.apache.jena.rdf.model.Resource;

public class ApacheLogParserUtils {
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

    private static final Pattern logEntryPattern = Pattern.compile(logEntryPatternStr);

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

            String timestampStr = m.group("time");
            Date date;
            try {
                date = dateFormat.parse(timestampStr);
                Calendar cal = new GregorianCalendar();
                cal.setTime(date);
                inout.addLiteral(PROV.atTime, cal);
            } catch (ParseException e) {
                inout.addLiteral(LSQ.runtimeError, "Failed to parse timestam: " + timestampStr);
            }

        }
    }
}
