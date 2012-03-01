package org.yaji.regex.jur;

import org.yaji.regex.Matcher;
import org.yaji.regex.Pattern;


class JavaUtilRegexPattern implements Pattern {

    private final java.util.regex.Pattern pattern;

    public JavaUtilRegexPattern(java.util.regex.Pattern pattern) {
        this.pattern = pattern;
    }

    public Matcher matcher(String string) {
        return new JavaUtilRegexMatcher(pattern.matcher(string));
    }
    
}