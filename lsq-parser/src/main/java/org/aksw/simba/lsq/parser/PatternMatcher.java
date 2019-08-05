package org.aksw.simba.lsq.parser;

import java.util.Map;
import java.util.function.Function;

//@FunctionalInterface
public interface PatternMatcher
    extends Function<String, Map<String, String>> {
    //Map<String, String> match(String line);
}
