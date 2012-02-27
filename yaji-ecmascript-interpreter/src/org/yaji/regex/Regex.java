package org.yaji.regex;


import org.yaji.regex.jur.JavaUtilRegex;

import FESI.Exceptions.EcmaScriptException;


public class Regex {
    public static final int CASE_INSENSITIVE = java.util.regex.Pattern.CASE_INSENSITIVE;

    public static final int MULTILINE = java.util.regex.Pattern.MULTILINE;
    
    private static RegexImplementation implementation = new JavaUtilRegex();
    
    public static Pattern compile(String string,int flags) throws EcmaScriptException {
        return implementation.compile(string, flags);
    }
}
