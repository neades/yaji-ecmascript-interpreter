package org.yaji.json;

import java.util.Stack;

import FESI.Data.ESNumber;
import FESI.Data.ESString;
import FESI.Data.ESValue;
import FESI.Exceptions.EcmaScriptException;

public abstract class JsonIndent {

    private static final JsonIndent NULL_INDENT = new JsonIndent() {
        @Override
        public String start() {
            return "";
        }

        @Override
        public String separator() {
            return ",";
        }

        @Override
        public String preValue() {
            return "";
        }

        @Override
        public String end() {
            return "";
        }

        @Override
        public void push() {
            // does nothing
        }

        @Override
        public void pop() {
            // does nothing
        }

    };

    public static JsonIndent create(ESValue indent) throws EcmaScriptException {
        StringBuilder spaces = new StringBuilder();
        if (indent instanceof ESNumber) {
            int nSpaces = Math.min(10, indent.toInt32());
            for(int i=0; i<nSpaces; i++) {
                spaces.append(' ');
            }
        } else if (indent instanceof ESString) {
            String string = indent.toString();
            if (string.length()>10) {
                spaces.append(string.substring(0,10));
            } else {
                spaces.append(string);
            }
        } 
        if (spaces.length() != 0) {
            final String gap = spaces.toString();
            return new JsonIndent() {
                private Stack<String> stack = new Stack<String>();
                private String indent = "";
                @Override
                public String start() {
                    return "\n"+indent;
                }

                @Override
                public String separator() {
                    return ",\n"+indent;
                }

                @Override
                public String preValue() {
                    return " ";
                }

                @Override
                public String end() {
                    return "\n"+stack.peek();
                }

                @Override
                public void push() {
                    stack.push(indent);
                    indent += gap;
                }

                @Override
                public void pop() {
                    indent = stack.pop();
                }

            };
        } else {
            return NULL_INDENT;
        }
    }

    public static JsonIndent create() {
        return NULL_INDENT;
    }
    
    private JsonIndent() {
        // Setting scope
    }

    public abstract String start();
    public abstract String separator();
    public abstract String preValue();
    public abstract String end();
    public abstract void push();
    public abstract void pop();
}
