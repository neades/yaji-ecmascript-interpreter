package org.yaji.regex.jur;

import java.util.Formatter;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.yaji.regex.Regex;
import org.yaji.regex.RegexImplementation;


import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.SyntaxError;

public class JavaUtilRegex implements RegexImplementation {

    public org.yaji.regex.Pattern compile(String regex, int flags) throws EcmaScriptException {
        try {
            return new JavaUtilRegexPattern(Pattern.compile(convertRegExp(regex), convertFlags(flags)));
        } catch(PatternSyntaxException e) {
            throw new SyntaxError("PatternSyntaxException: /" + regex + "/", e);

        }
    }
    
    private static int [][] flagsMap = {
        { Regex.MULTILINE, Pattern.MULTILINE|Pattern.DOTALL },
        { Regex.CASE_INSENSITIVE, Pattern.CASE_INSENSITIVE }
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
        private static class DecimalEscapeState extends State {
            private int decimal = 0;
            private final State parentState;
            
            public DecimalEscapeState(char c, State parentState) {
                this.parentState = parentState;
                decimal = c - '0';
            }
            @Override
            public State process(char c, StringBuilder sb) {
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
        public final static State NORMAL = new State() {
            @Override
            public State process(char c, StringBuilder sb) {
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
        public final static State CONTROLESCAPE = new State() {
            @Override
            public State process(char c, StringBuilder sb) {
                int controlCode = c % 32;
                sb.append("\\u00");
                if (controlCode < 16) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(controlCode));
                return NORMAL;
            }
        };
        public final static State NORMAL_INESCAPE = new State() {
            @Override
            public State process(char c, StringBuilder sb) {
                switch(c) {
                case '0':
                    sb.append("\\u0000");
                    break;
                case 'c':
                    return CONTROLESCAPE;
                default:
                    sb.append('\\');
                    sb.append(c);
                }
                return NORMAL;
            }
        };
        public final static State CLASSRANGE_INESCAPE = new State() {
            @Override
            public State process(char c, StringBuilder sb) {
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
        public final static State CLASSRANGE = new State() {
            @Override
            public State process(char c, StringBuilder sb) {
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
        public final static State FIRSTINCLASSRANGE = new State() {
            @Override
            public State process(char c, StringBuilder sb) {
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
        public final static State FIRSTININVERTEDCLASSRANGE = new State() {
            @Override
            public State process(char c, StringBuilder sb) {
                if (c ==']') {
                    sb.append('.');
                    return NORMAL;
                }
                sb.append('[');
                sb.append('^');
                return CLASSRANGE.process(c, sb);
            }
        };
        
        
        public abstract State process(char c, StringBuilder sb);
    }
//    private static Pattern shouldConvert = Pattern.compile("((\\\\b)|(\\[(\\^)?\\])|(\\\\\\d)|(\\\\0)|(\\\\c)");
    protected static String convertRegExp(String source) throws EcmaScriptException {
       
        //if (shouldConvert.matcher(source).find()) {
            StringBuilder sb = new StringBuilder(source.length());
            State state = State.NORMAL;
            for( char c : source.toCharArray()) {
               state = state.process(c, sb); 
            }
            if (state != State.NORMAL) {
                throw new SyntaxError("Invalid RegExp '"+source+"'");
            }
            return sb.toString();
//        }
//        return source;
    }


}