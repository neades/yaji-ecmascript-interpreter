package org.yaji.regex;


public interface Pattern {

    public Matcher matcher(CharSequence string);
    
}