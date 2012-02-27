package org.yaji.regex.jur;

import java.util.Formatter;
import java.util.regex.PatternSyntaxException;

import org.yaji.regex.Pattern;
import org.yaji.regex.Regex;
import org.yaji.regex.RegexImplementation;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.SyntaxError;

public class JavaUtilRegex implements RegexImplementation {

    public Pattern compile(String regex, int flags) throws EcmaScriptException {
        try {
        return new JavaUtilRegexPattern(java.util.regex.Pattern.compile(convertRegExp(regex), convertFlags(flags)|java.util.regex.Pattern.DOTALL));
        } catch(PatternSyntaxException e) {
            throw new SyntaxError("PatternSyntaxException: /" + regex + "/", e);

        }
    }
    
    private static int [][] flagsMap = {
        { Regex.MULTILINE, java.util.regex.Pattern.MULTILINE },
        { Regex.CASE_INSENSITIVE, java.util.regex.Pattern.CASE_INSENSITIVE }
    };
    private int convertFlags(int flags) {
        int converted = 0;
        for(int i=0; i<flagsMap.length; i++) {
            if ((flags & flagsMap[i][0]) != 0) {
                converted |= flagsMap[i][1];
            }
        }
        return converted;
    }
    
    private static abstract class State {
        private static class DecimalEscapeState extends JavaUtilRegex.State {
            private int decimal = 0;
            private final JavaUtilRegex.State parentState;
            
            public DecimalEscapeState(char c, JavaUtilRegex.State parentState) {
                this.parentState = parentState;
                decimal = c - '0';
            }
            @Override
            public JavaUtilRegex.State process(char c, StringBuilder sb) {
                if (Character.isDigit(c)) {
                    decimal = 8*decimal + (c-'0');
                    return this;
                }
                sb.append("\\u");
                Formatter formatter = new Formatter(sb);
                formatter.format("%04x", Integer.valueOf(decimal));
                return parentState.process(c,sb);
            }
        }
        public final static JavaUtilRegex.State NORMAL = new State() {
            @Override
            public JavaUtilRegex.State process(char c, StringBuilder sb) {
                if (c == '\\') {
                    return NORMAL_INESCAPE;
                }
                if (c == '[') {
                    return FIRSTINCLASSRANGE;
                }
                sb.append(c);
                return this;
            }
        };
        public final static JavaUtilRegex.State NORMAL_INESCAPE = new State() {
            @Override
            public JavaUtilRegex.State process(char c, StringBuilder sb) {
                sb.append('\\');
                sb.append(c);
                return NORMAL;
            }
        };
        public final static JavaUtilRegex.State CLASSRANGE_INESCAPE = new State() {
            @Override
            public JavaUtilRegex.State process(char c, StringBuilder sb) {
                if (c == 'b') {
                    sb.append("\u0008");
                } else if (Character.isDigit(c)) {
                    return new DecimalEscapeState(c,CLASSRANGE);
                } else {
                    sb.append('\\');
                    sb.append(c);
                }
                return CLASSRANGE;
            }
        };
        public final static JavaUtilRegex.State CLASSRANGE = new State() {
            @Override
            public JavaUtilRegex.State process(char c, StringBuilder sb) {
                if (c == '\\') {
                    return CLASSRANGE_INESCAPE;
                }
                if (c ==']') {
                    sb.append(c);
                    return NORMAL;
                }
                sb.append(c);
                return this;
            }
        };
        public final static JavaUtilRegex.State FIRSTINCLASSRANGE = new State() {
            @Override
            public JavaUtilRegex.State process(char c, StringBuilder sb) {
                if (c ==']') {
                    sb.append("[^\\w\\W\\u0000]");
                    return NORMAL;
                }
                if (c == '^') {
                    return FIRSTININVERTEDCLASSRANGE;
                }
                sb.append('[');
                return CLASSRANGE.process(c, sb);
            }
        };
        public final static JavaUtilRegex.State FIRSTININVERTEDCLASSRANGE = new State() {
            @Override
            public JavaUtilRegex.State process(char c, StringBuilder sb) {
                if (c ==']') {
                    sb.append('.');
                    return NORMAL;
                }
                sb.append('[');
                sb.append('^');
                return CLASSRANGE.process(c, sb);
            }
        };
        
        
        public abstract JavaUtilRegex.State process(char c, StringBuilder sb);
    }
    private static java.util.regex.Pattern shouldConvert = java.util.regex.Pattern.compile("((\\\\b)|(\\[(\\^)?\\])|(\\\\\\d))");
    protected static String convertRegExp(String source) {
       
        if (shouldConvert.matcher(source).find()) {
            StringBuilder sb = new StringBuilder(source.length());
            JavaUtilRegex.State state = State.NORMAL;
            for( char c : source.toCharArray()) {
               state = state.process(c, sb); 
            }
            return sb.toString();
        }
        return source;
    }


}