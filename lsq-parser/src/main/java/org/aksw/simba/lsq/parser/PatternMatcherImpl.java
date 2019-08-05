package org.aksw.simba.lsq.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatcherImpl
    implements PatternMatcher
{
    protected Pattern pattern;
    protected List<String> groupNames;
    
    // TODO Allow > if escaped
    public static final Pattern groupNamePattern = Pattern.compile("\\?<([^>]+)>", Pattern.MULTILINE);
    
    public static List<String> extractGroupNames(String str) {
        List<String> result = new ArrayList<>();
        Matcher m = groupNamePattern.matcher(str);
        while(m.find()) {
            String v = m.group(1);
            result.add(v);
        }
        return result;
    }
    
    public PatternMatcherImpl(Pattern pattern) {
        this(pattern, extractGroupNames(pattern.toString()));
    }


    public PatternMatcherImpl(Pattern pattern, List<String> groupNames) {
        super();
        this.pattern = pattern;
        this.groupNames = groupNames;
    }


    @Override
    public Map<String, String> apply(String str) {
        Map<String, String> result = matchAsMap(pattern, groupNames, str);
        return result;
    }

    
    public static Map<String, String> matchAsMap(Pattern pattern, List<String> groupNames, String str) {
        Map<String, String> result = null;

        Matcher m = pattern.matcher(str);
        if(m.find()) {
            result = new HashMap<>();
            
            for(String k : groupNames) {
                String v = m.group(k);
                result.put(k, v);
            }
        }

        return result;
    }

}
