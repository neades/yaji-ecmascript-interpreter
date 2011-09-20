// StringObject.java
// FESI Copyright (c) Jean-Marc Lugrin, 1999
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package FESI.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.ProgrammingError;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;
import FESI.Util.IAppendable;

/**
 * Implements the EcmaScript String singleton.
 */
public class StringObject extends BuiltinFunctionObject {
    private static final class StringPrototypeTrim extends
            BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        private StringPrototypeTrim(String functionName,
                Evaluator evaluator, ESObject functionPrototype) {
            super(functionPrototype, evaluator, functionName, 0);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,ESValue[] arguments) throws EcmaScriptException {
            checkThisObjectCoercible(thisObject);
            char[] s = thisObject.toString().toCharArray();
            int start;
            for( start=0; start < s.length && isWhitespace(s[start]); start++) {
                //  loop
            }
            int end;
            for( end=s.length-1; end > start && isWhitespace(s[end]); end --) {
                // loop
            }
            return new ESString(new String(s,start,end-start+1));
        }

        private boolean isWhitespace(char c) {
            return Character.isWhitespace(c) || Character.isSpaceChar(c);
        }
    }

    // For stringPrototype
    private static class StringPrototypeToString extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringPrototypeToString(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            if (thisObject instanceof StringPrototype) {
                return ((StringPrototype) thisObject).value;
            }
            throw new TypeError("String.prototype.toString can only be applied to a String");
        }
    }

    private static class StringPrototypeSearch extends BuiltinFunctionObject {
        private static final long serialVersionUID = -3110437308530985673L;

        StringPrototypeSearch(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            if (arguments.length < 1) {
                throw new EcmaScriptException(
                        "search requires 1 pattern argument");
            }
            String str = thisObject.toString();
            RegExpPrototype pattern;
            if (arguments[0] instanceof RegExpPrototype) {
                pattern = (RegExpPrototype) arguments[0];
            } else {
                throw new EcmaScriptException(
                        "The search argument must be a RegExp");
            }
            Matcher matcher = pattern.getPattern().matcher(str);
            if (matcher.find()) {
                return ESNumber.valueOf(matcher.start());
            } else {
                return ESNumber.valueOf(-1);
            }
        }
    }

    private static class StringPrototypeReplace extends BuiltinFunctionObject {
        private static final long serialVersionUID = -5972680989663620329L;

        StringPrototypeReplace(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            if (arguments.length < 2) {
                throw new EcmaScriptException(
                        "replace requires 2 arguments: pattern and replacement string");
            }
            String str = thisObject.toString();
            RegExpPrototype pattern;
            if (arguments[0] instanceof RegExpPrototype) {
                pattern = (RegExpPrototype) arguments[0];
            } else {
                throw new EcmaScriptException(
                        "The replace argument must be a RegExp");
            }
            Matcher matcher = pattern.getPattern().matcher(str);
            String replacement = arguments[1].toString();

            if (pattern.isGlobal()) {
                return new ESString(matcher.replaceAll(replacement));
            } else {
                return new ESString(matcher.replaceFirst(replacement));
            }
        }
    }

    private static class StringPrototypeMatch extends BuiltinFunctionObject {
        private static final long serialVersionUID = -4817060573308387732L;

        StringPrototypeMatch(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            if (arguments.length < 1) {
                throw new EcmaScriptException(
                        "match requires 1 pattern argument");
            }
            String str = thisObject.toString();
            RegExpPrototype pattern;
            if (arguments[0] instanceof RegExpPrototype) {
                pattern = (RegExpPrototype) arguments[0];
            } else {
                throw new EcmaScriptException(
                        "The match argument must be a RegExp");
            }
            Matcher matcher = pattern.getPattern().matcher(str);

            boolean result = matcher.find();
            if (result) {
                // at least one match
                ESObject ap = this.getEvaluator().getArrayPrototype();
                ArrayPrototype resultArray = new ArrayPrototype(ap, this
                        .getEvaluator());
                resultArray.putProperty(StandardProperty.INDEXstring, ESNumber
                        .valueOf(matcher.start()), StandardProperty.INDEXhash);
                resultArray.putProperty(StandardProperty.INPUTstring, new ESString(str),
                        StandardProperty.INPUThash);
                resultArray.setSize(matcher.groupCount() + 1);
                for (int i = 0; i <= matcher.groupCount(); i++) {
                    resultArray.setElementAt(
                            new ESString(matcher.group(i)), i);
                } // for
                return resultArray;
            } else {
                return ESNull.theNull;
            }
        }
    }

    private static class StringPrototypeSplit extends BuiltinFunctionObject {
        private static final long serialVersionUID = 2991853591081656659L;

        StringPrototypeSplit(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            String str = thisObject.toString();
            ESObject ap = this.getEvaluator().getArrayPrototype();
            ArrayPrototype theArray = new ArrayPrototype(ap, this
                    .getEvaluator());
            if (arguments.length <= 0) {
                theArray.setSize(1);
                theArray.setElementAt(thisObject, 0);
            } else {
                if (arguments[0] instanceof RegExpPrototype) {
                    RegExpPrototype pattern = (RegExpPrototype) arguments[0];
                    Pattern spliter = pattern.getPattern();
                    String[] result = null;
                    if (arguments.length > 1) {
                        int n = arguments[1].toUInt32();
                        result = spliter.split(str, n);
                    } else {
                        result = spliter.split(str);
                    }
                    int l = result.length;
                    theArray.setSize(l);
                    for (int i = 0; i < l; i++) {
                        theArray.setElementAt(new ESString(result[i]), i);
                    }

                } else { // ! instanceof ESJavaRegExp, using "normal" split
                    String sep = arguments[0].toString();
                    if (sep.length() == 0) {
                        int l = str.length();
                        theArray.setSize(l);
                        for (int i = 0; i < l; i++) {
                            theArray.setElementAt(new ESString(str
                                    .substring(i, i + 1)), i);
                        }
                    } else {
                        int i = 0;
                        int start = 0;
                        while (start < str.length()) {
                            int pos = str.indexOf(sep, start);
                            if (pos < 0)
                                pos = str.length();
                            // System.out.println("start: " + start +
                            // ", pos: " + pos);
                            theArray.setSize(i + 1);
                            theArray.setElementAt(new ESString(str
                                    .substring(start, pos)), i);
                            start = pos + sep.length();
                            i++;
                        }
                    }
                } // instanceof ESORORegExp
            }
            return theArray;
        }
    }


    
    private static class StringPrototypeValueOf extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringPrototypeValueOf(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            if (thisObject instanceof StringPrototype) {
                return ((StringPrototype) thisObject).value;
            }
            throw new TypeError("String.prototype.valueOf can only be applied to a String");
        }
    }

    private static class StringPrototypeCharAt extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringPrototypeCharAt(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            String str = thisObject.toString();
            int pos = 0;
            if (arguments.length > 0) {
                pos = arguments[0].toInt32();
            }
            if (pos >= 0 && pos < str.length()) {
                char c[] = { str.charAt(pos) };
                return new ESString(new String(c));
            }
            return new ESString("");
        }
    }

    private static class StringPrototypeCharCodeAt extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringPrototypeCharCodeAt(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            String str = thisObject.toString();
            int pos = 0;
            if (arguments.length > 0) {
                pos = arguments[0].toInt32();
            }
            if (pos >= 0 && pos < str.length()) {
                char c = str.charAt(pos);
                return ESNumber.valueOf(c);
            }
            return ESNumber.valueOf(Double.NaN);
        }
    }

    private static class StringPrototypeConcat extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;
        StringPrototypeConcat(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }
        
        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            IAppendable appendable = getEvaluator().createAppendable(arguments.length + 1, 1000);
            appendable.append(thisObject.toString());
            for (ESValue argument : arguments) {
                appendable.append(argument.toString());
            }
            return new ESString(appendable);
        }
    }
    
    private static class StringPrototypeIndexOf extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringPrototypeIndexOf(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            String str = thisObject.toString();
            int pos = 0;
            if (arguments.length <= 0) {
                return ESNumber.valueOf(-1);
            }
            String searched = arguments[0].toString();
            if (arguments.length > 1) {
                pos = arguments[1].toInt32();
            }
            int res = str.indexOf(searched, pos);
            return ESNumber.valueOf(res);
        }
    }

    private static class StringPrototypeLastIndexOf extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringPrototypeLastIndexOf(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            String str = thisObject.toString();
            int pos = str.length();
            if (arguments.length <= 0) {
                return ESNumber.valueOf(-1);
            }
            String searched = arguments[0].toString();
            if (arguments.length > 1) {
                double p = arguments[1].doubleValue();
                if (!Double.isNaN(p)) {
                    pos = arguments[1].toInt32();
                }
            }
            int res = str.lastIndexOf(searched, pos);
            return ESNumber.valueOf(res);
        }
    }

    private static class StringPrototypeSubstring extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringPrototypeSubstring(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            String str = thisObject.toString();
            int start = 0;
            int end = str.length();
            if (arguments.length > 0) {
                start = arguments[0].toInt32();
            }
            if (start < 0) {
                start = 0;
            } else if (start > str.length()) {
                start = str.length();
            }
            if (arguments.length > 1) {
                end = arguments[1].toInt32();
                if (end < 0) {
                    end = 0;
                } else if (end > str.length()) {
                    end = str.length();
                }
            }
            if (start > end) {
                int x = start;
                start = end;
                end = x;
            }
            return new ESString(str.substring(start, end));
        }
    }

    private static class StringPrototypeToLowerCase extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringPrototypeToLowerCase(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            String str = thisObject.toString();

            return new ESString(str.toLowerCase());
        }
    }

    private static class StringPrototypeToUpperCase extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringPrototypeToUpperCase(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            String str = thisObject.toString();

            return new ESString(str.toUpperCase());
        }
    }

    // For stringObject
    private static class StringObjectFromCharCode extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringObjectFromCharCode(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            ESObject sp = this.getEvaluator().getStringPrototype();
            StringPrototype theObject = new StringPrototype(sp, this
                    .getEvaluator());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arguments.length; i++) {
                char c = (char) (arguments[i].toUInt16());
                sb.append(c);
            }
            theObject.value = new ESString(sb.toString());
            return theObject;
        }
    }


    private static final long serialVersionUID = -5722531882648574621L;

    private StringObject(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator, "String", 1);
    }

    // overrides
    @Override
    public String toString() {
        return "<String>";
    }

    // overrides
    @Override
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        if (arguments.length == 0) {
            return new ESString("");
        }
        return new ESString(arguments[0].toString());

    }

    // overrides
    @Override
    public ESObject doConstruct(ESObject thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        StringPrototype theObject = null;
        ESObject sp = getEvaluator().getStringPrototype();
        theObject = new StringPrototype(sp, getEvaluator());
        if (arguments.length > 0) {
            theObject.value = new ESString(arguments[0].toString());
        } else {
            theObject.value = new ESString("");
        }
        return theObject;
    }

    /**
     * Utility function to create the single String object
     * 
     * @param evaluator
     *            the Evaluator
     * @param objectPrototype
     *            The Object prototype attached to the evaluator
     * @param functionPrototype
     *            The Function prototype attached to the evaluator
     * 
     * @return the String singleton
     */
    public static StringObject makeStringObject(Evaluator evaluator,
            ObjectPrototype objectPrototype, FunctionPrototype functionPrototype) {

        StringPrototype stringPrototype = new StringPrototype(objectPrototype,
                evaluator);
        StringObject stringObject = new StringObject(functionPrototype,
                evaluator);

        try {
            stringObject.putHiddenProperty("prototype", stringPrototype);
            stringObject.putHiddenProperty("length", ESNumber.valueOf(1));
            stringObject.putHiddenProperty("fromCharCode",
                    new StringObjectFromCharCode("fromCharCode", evaluator,
                            functionPrototype));

            stringPrototype.putHiddenProperty("constructor", stringObject);
            stringPrototype.putHiddenProperty("toString",
                    new StringPrototypeToString("toString", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("valueOf",
                    new StringPrototypeValueOf("valueOf", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("charAt",
                    new StringPrototypeCharAt("charAt", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("charCodeAt",
                    new StringPrototypeCharCodeAt("charCodeAt", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("concat",
                    new StringPrototypeConcat("concat", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("indexOf",
                    new StringPrototypeIndexOf("indexOf", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("lastIndexOf",
                    new StringPrototypeLastIndexOf("lastIndexOf", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("match", 
                    new StringPrototypeMatch("match", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("replace",
                    new StringPrototypeReplace("replace", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("search", 
                    new StringPrototypeSearch("search", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("split",
                    new StringPrototypeSplit("split", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("substring",
                    new StringPrototypeSubstring("substring", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("toLowerCase",
                    new StringPrototypeToLowerCase("toLowerCase", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("toUpperCase",
                    new StringPrototypeToUpperCase("toUpperCase", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("trim", new StringPrototypeTrim("trim", evaluator, functionPrototype));
            
        } catch (EcmaScriptException e) {
            e.printStackTrace();
            throw new ProgrammingError(e.getMessage());
        }

        evaluator.setStringPrototype(stringPrototype);

        return stringObject;
    }
}