package org.yaji.regex;

public interface Matcher {

    boolean find();

    int start();

    int groupCount();

    String group(int i);

    int end();

    String group();
    
}