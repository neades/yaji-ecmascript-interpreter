package org.yaji.regex.jur;

import org.yaji.regex.Matcher;

public class JavaUtilRegexMatcher implements Matcher {

    private final java.util.regex.Matcher matcher;

    public JavaUtilRegexMatcher(java.util.regex.Matcher matcher) {
        this.matcher = matcher;
    }

    public boolean find() {
        return matcher.find();
    }

    public int start() {
        return matcher.start();
    }

    public int groupCount() {
        return matcher.groupCount();
    }

    public String group(int i) {
        return matcher.group(i);
    }

    public int end() {
        return matcher.end();
    }

    public String group() {
        return matcher.group();
    }
}