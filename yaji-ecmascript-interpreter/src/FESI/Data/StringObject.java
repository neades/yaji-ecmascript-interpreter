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

import java.text.Collator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.ProgrammingError;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.ILocaleListener;
import FESI.Util.IAppendable;

/**
 * Implements the EcmaScript String singleton.
 */
public class StringObject extends BuiltinFunctionObject {
    private static abstract class CoercedStringFunction extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;
        
        public CoercedStringFunction(ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype,evaluator,functionName,length);
        }
        
        @Override
        public ESValue callFunction(ESValue thisObject,ESValue[] arguments) throws EcmaScriptException {
            checkThisObjectCoercible(thisObject);
            return invoke(thisObject.callToString(),arguments);
        }

        protected abstract ESValue invoke(String string, ESValue[] arguments) throws EcmaScriptException;
    }
    
    private static final class StringPrototypeTrim extends CoercedStringFunction {
        private static final long serialVersionUID = 1L;

        private StringPrototypeTrim(String functionName,
                Evaluator evaluator, ESObject functionPrototype) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, 0);
        }

        @Override
        public ESValue invoke(String string, ESValue[] arguments) throws EcmaScriptException {
            return new ESString(ESString.trim(string));
        }
    }

    // For stringPrototype
    private static class StringPrototypeToString extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringPrototypeToString(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
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

    private static class StringPrototypeSlice extends CoercedStringFunction {
        private static final long serialVersionUID = 1L;

        StringPrototypeSlice(String name, Evaluator evaluator, FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 2);
        }

        @Override
        public ESValue invoke(String str, ESValue[] arguments) throws EcmaScriptException {
            int strLength = str.length();
            int start = getArgAsInt32(arguments,0);
            if (start < 0) {
                start = Math.max(strLength + start,0);
            }
            ESValue endValue = getArg(arguments,1);
            int end = (endValue.getTypeOf() == EStypeUndefined) ? strLength : endValue.toInt32();
            if (end < 0) {
                end = Math.max(strLength + end,0);
            } else {
                end = Math.min(end,strLength);
            }
            if (end < start || start > strLength) {
                return ESString.valueOf("");
            }
            return new ESString(str.substring(start, end));
        }
    }

    private static class StringPrototypeSearch extends CoercedStringFunction {
        private static final long serialVersionUID = -3110437308530985673L;

        StringPrototypeSearch(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue invoke(String str, ESValue[] arguments) throws EcmaScriptException {
            ESValue regexp = getArg(arguments,0);
            RegExpPrototype pattern;
            if (regexp instanceof RegExpPrototype) {
                pattern = (RegExpPrototype) regexp;
            } else {
                pattern = (RegExpPrototype) getEvaluator().getGlobalObject().getProperty("RegExp","RegExp".hashCode()).doConstruct(new ESValue[] { regexp });
            }
            Matcher matcher = pattern.getPattern().matcher(str);
            if (matcher.find()) {
                return ESNumber.valueOf(matcher.start());
            }
            return ESNumber.valueOf(-1);
        }
    }

    private static class StringPrototypeReplace extends CoercedStringFunction {
        private static final long serialVersionUID = -5972680989663620329L;

        StringPrototypeReplace(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 2);
        }

        @Override
        public ESValue invoke(String str, ESValue[] arguments)
                throws EcmaScriptException {
            ESValue searchValue = getArg(arguments,0);
            ESValue replaceValue = getArg(arguments,1);
            if (searchValue instanceof RegExpPrototype) {
                RegExpPrototype pattern = (RegExpPrototype) searchValue;
                Matcher matcher = pattern.getPattern().matcher(str);
                if (replaceValue.isCallable()) {
                    ESString string = new ESString(str);
                    StringBuilder sb = new StringBuilder();
                    int lastIndex = 0;
                    while (matcher.find()) {
                        int groupCount = matcher.groupCount();
                        ESValue[] params = new ESValue[groupCount+3];
                        params[0] = new ESString(matcher.group());
                        for (int i=1; i<=groupCount; i++) {
                            params[i] = new ESString(matcher.group(i));
                        }
                        params[groupCount+1] = ESNumber.valueOf(matcher.start());
                        params[groupCount+2] = string;
                        String replacement = replaceValue.callFunction(null, params).callToString();
                        if (lastIndex < matcher.start()) {
                            sb.append(str.substring(lastIndex, matcher.start()));
                        }
                        sb.append(replacement);
                        lastIndex = matcher.end();
                        if (!pattern.isGlobal()) {
                            break;
                        }
                    }
                    sb.append(str.substring(lastIndex,str.length()));
                    return new ESString(sb.toString());
                }
                return replaceUsingRegexpSimpleReplacement(str,replaceValue,
                        pattern, matcher);
            }
            return replaceSimpleString(str, searchValue, replaceValue);
        }

        public ESValue replaceUsingRegexpSimpleReplacement(String str,
                ESValue replaceValue, RegExpPrototype pattern, Matcher matcher)
                throws EcmaScriptException {
            String replacement = replaceValue.callToString();
            StringBuilder sb = new StringBuilder();
            int lastIndex = 0;
            while (matcher.find()) {
                if (lastIndex < matcher.start()) {
                    sb.append(str.substring(lastIndex, matcher.start()));
                }
                boolean inSubstitution = false;
                int groupIndex = -1;
                for ( char ch : replacement.toCharArray()) {
                    if (inSubstitution) {
                        switch ( ch ) {
                        case '$':
                            sb.append(ch);
                            break;
                        case '&':
                            sb.append(str.substring(matcher.start(),matcher.end()));
                            break;
                        case '`': // preceeding
                            sb.append(str.substring(0,matcher.start()));
                            break;
                        case '\'': // following
                            sb.append(str.substring(matcher.end()));
                            break;
                        case '0': groupIndex = 0; break;
                        case '1': groupIndex = 1; break;
                        case '2': groupIndex = 2; break;
                        case '3': groupIndex = 3; break;
                        case '4': groupIndex = 4; break;
                        case '5': groupIndex = 5; break;
                        case '6': groupIndex = 6; break;
                        case '7': groupIndex = 7; break;
                        case '8': groupIndex = 8; break;
                        case '9': groupIndex = 9; break;
                            
                        default:
                            sb.append('$');
                            sb.append(ch);
                        }
                        inSubstitution = false;
                    } else if (groupIndex != -1){
                        boolean isDigit = Character.isDigit(ch);
                        if (isDigit) {
                            groupIndex = groupIndex * 10 + (ch - '0');
                        }
                        if (groupIndex == 0) {
                            sb.append("$0");
                            if (ch == 0) {
                                sb.append('0');
                            }
                        } else {
                            sb.append(matcher.group(groupIndex));
                        }
                        if (!isDigit) {
                            if (ch == '$') {
                                inSubstitution = true;
                            } else {
                                sb.append(ch);
                            }
                        }
                        groupIndex = -1;
                    } else {
                        if (ch == '$') {
                            inSubstitution = true;
                        } else {
                            sb.append(ch);
                        }
                    }
                }
                if (groupIndex != -1) {
                    if (groupIndex == 0) {
                        sb.append("$0");
                    } else {
                        sb.append(matcher.group(groupIndex));
                    }
                } else if (inSubstitution) {
                    sb.append('$');
                }
                lastIndex = matcher.end();
                if (!pattern.isGlobal()) {
                    break;
                }
            }
            sb.append(str.substring(lastIndex,str.length()));
            return new ESString(sb.toString());
        }

        public ESValue replaceSimpleString(String str, ESValue searchValue,
                ESValue replaceValue) throws EcmaScriptException {
            String searchString = searchValue.callToString();
            int index = str.indexOf(searchString);
            if (index == -1) {
                return new ESString(str);
            }
            
            String replacementString = replaceValue.isCallable() ?
                replaceValue.callFunction(null, new ESValue[] { new ESString(searchString), ESNumber.valueOf(index), new ESString(str) }).callToString()
              : replaceValue.callToString();
            int endOfSearch = index + searchString.length();
            String replace = str.substring(0, index) + replacementString + str.substring(endOfSearch) ;
            return new ESString(replace);
        }
        
    }

    private static class StringPrototypeMatch extends BuiltinFunctionObject {
        private static final long serialVersionUID = -4817060573308387732L;

        StringPrototypeMatch(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
        }

        @Override
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
                ESObject resultArray = this.getEvaluator().createArray();
                resultArray.putProperty(StandardProperty.INDEXstring, ESNumber
                        .valueOf(matcher.start()), StandardProperty.INDEXhash);
                resultArray.putProperty(StandardProperty.INPUTstring, new ESString(str),
                        StandardProperty.INPUThash);
                for (int i = 0; i <= matcher.groupCount(); i++) {
                    resultArray.putProperty((long)i,
                            new ESString(matcher.group(i)));
                } // for
                return resultArray;
            }
            return ESNull.theNull;
        }
    }

    private static class StringPrototypeSplit extends CoercedStringFunction {
        private static final long serialVersionUID = 2991853591081656659L;

        StringPrototypeSplit(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 2);
        }

        private void split(Pattern pattern, ESObject matchList, CharSequence input, int limit) throws EcmaScriptException {
            int index = 0;
            boolean matchLimited = limit > 0;
            Matcher m = pattern.matcher(input);
            
            long matchListSize = matchList.getProperty(StandardProperty.LENGTHstring, StandardProperty.LENGTHhash).toUInt32();

            int groupCount = m.groupCount();
            // Add segments before each match found
            while(m.find()) {
                if (m.end() == 0 || m.start() == input.length()) {
                    continue;
                }
                String match = input.subSequence(index, m.start()).toString();
                matchList.putProperty(matchListSize++,new ESString(match));
                if (matchListSize == limit) {
                    return;
                }
                for (int i=1; i<=groupCount; i++) {
                    String group = m.group(i);
                    matchList.putProperty(matchListSize++,(group == null)?ESUndefined.theUndefined:new ESString(group));
                    if (matchListSize == limit) {
                        return;
                    }
                }
                index = m.end();
            }

            // If no match was found, return complete string
            if (index == 0) {
                matchList.putProperty(matchListSize++,new ESString(input.toString()));
            } else {
                // Add remaining segment
                if (!matchLimited || matchListSize < limit) {
                    matchList.putProperty(matchListSize++,new ESString(input.subSequence(index, input.length()).toString()));
                }
            }

        }
        @Override
        public ESValue invoke(String str, ESValue[] arguments)
                throws EcmaScriptException {
            ESObject theArray = this.getEvaluator().createArray();
            if (arguments.length <= 0) {
                theArray.putProperty(0L, new ESString(str));
            } else {
                if (arguments[0] instanceof RegExpPrototype) {
                    RegExpPrototype regexp = (RegExpPrototype) arguments[0];
                    ESValue limitValue = getArg(arguments,1);
                    int limit = (limitValue.getTypeOf() == EStypeUndefined) ? Integer.MAX_VALUE : limitValue.toInt32();
                    if (limit != 0) {
                        split(regexp.getPattern(),theArray,str,limit);
                    }
                } else { // ! instanceof ESJavaRegExp, using "normal" split
                    String sep = arguments[0].callToString();
                    if (sep.length() == 0) {
                        int l = str.length();
                        for (int i = 0; i < l; i++) {
                            theArray.putProperty((long)i,new ESString(str.substring(i, i + 1)));
                        }
                    } else {
                        int i = 0;
                        int start = 0;
                        while (start < str.length()) {
                            int pos = str.indexOf(sep, start);
                            if (pos < 0)
                                pos = str.length();
                            theArray.putProperty((long)i,new ESString(str.substring(start, pos)));
                            start = pos + sep.length();
                            i++;
                        }
                    }
                } 
            }
            return theArray;
        }

    }


    
    private static class StringPrototypeValueOf extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringPrototypeValueOf(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            if (thisObject instanceof ESString) {
                return thisObject;
            }
            if (thisObject instanceof StringPrototype) {
                return ((StringPrototype) thisObject).value;
            }
            throw new TypeError("String.prototype.valueOf can only be applied to a String");
        }
    }

    private static class StringPrototypeCharAt extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringPrototypeCharAt(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            String str = thisObject.callToString();
            int pos = 0;
            if (arguments.length > 0) {
                pos = (int)arguments[0].toInteger();
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
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            String str = thisObject.callToString();
            int pos = 0;
            if (arguments.length > 0) {
                pos = (int)arguments[0].toInteger();
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
        StringPrototypeConcat(String name, Evaluator evaluator, FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
        }
        
        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            IAppendable appendable = getEvaluator().createAppendable(arguments.length + 1, 1000);
            appendable.append(thisObject.callToString());
            for (ESValue argument : arguments) {
                appendable.append(argument.callToString());
            }
            return new ESString(appendable);
        }
    }
    
    private static class StringPrototypeIndexOf extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringPrototypeIndexOf(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            String str = thisObject.callToString();
            int pos = 0;
            if (arguments.length <= 0) {
                return ESNumber.valueOf(-1);
            }
            String searched = getArg(arguments,0).callToString();
            if (arguments.length > 1) {
                pos = (int)getArg(arguments,1).toInteger();
            }
            int res = str.indexOf(searched, pos);
            return ESNumber.valueOf(res);
        }
    }

    private static class StringPrototypeLastIndexOf extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringPrototypeLastIndexOf(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            String str = thisObject.callToString();
            int pos = str.length();
            if (arguments.length <= 0) {
                return ESNumber.valueOf(-1);
            }
            String searched = getArg(arguments,0).callToString();
            if (arguments.length > 1) {
                ESValue arg = getArg(arguments,1);
                double p = arg.doubleValue();
                if (!Double.isNaN(p)) {
                    pos = (int)arg.toInteger();
                }
            }
            int res = str.lastIndexOf(searched, pos);
            return ESNumber.valueOf(res);
        }
    }

    private static class StringPrototypeSubstring extends CoercedStringFunction {
        private static final long serialVersionUID = 1L;

        StringPrototypeSubstring(String name, Evaluator evaluator, FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 2);
        }

        @Override
        public ESValue invoke(String str, ESValue[] arguments) throws EcmaScriptException {
            int start = (int)getArg(arguments,0).toInteger();
            if (start < 0) {
                start = 0;
            } else if (start > str.length()) {
                start = str.length();
            }
            ESValue endValue = getArg(arguments,1);
            int end = (endValue.getTypeOf() == EStypeUndefined) ? str.length() : (int)endValue.toInteger();
            if (end < 0) {
                end = 0;
            } else if (end > str.length()) {
                end = str.length();
            }
            if (start > end) {
                int x = start;
                start = end;
                end = x;
            }
            return new ESString(str.substring(start, end));
        }
    }

    private static class StringPrototypeToLowerCase extends CoercedStringFunction {
        private static final long serialVersionUID = 1L;

        StringPrototypeToLowerCase(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue invoke(String str, ESValue[] arguments) throws EcmaScriptException {
            return new ESString(str.toLowerCase(Locale.ENGLISH));
        }
    }

    private static class StringPrototypeToLocaleLowerCase extends CoercedStringFunction {
        private static final long serialVersionUID = 1L;

        StringPrototypeToLocaleLowerCase(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue invoke(String str, ESValue[] arguments) throws EcmaScriptException {
            return new ESString(str.toLowerCase());
        }
    }

    private static class StringPrototypeToUpperCase extends CoercedStringFunction {
        private static final long serialVersionUID = 1L;

        StringPrototypeToUpperCase(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue invoke(String str, ESValue[] arguments) throws EcmaScriptException {
            return new ESString(str.toUpperCase(Locale.ENGLISH));
        }
    }

    private static class StringPrototypeToLocaleUpperCase extends CoercedStringFunction {
        private static final long serialVersionUID = 1L;

        StringPrototypeToLocaleUpperCase(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue invoke(String str, ESValue[] arguments) throws EcmaScriptException {
            return new ESString(str.toUpperCase());
        }
    }

    private static class StringPrototypeLocaleCompare extends CoercedStringFunction implements ILocaleListener {
        private static final long serialVersionUID = 1L;
        private Collator collator;

        StringPrototypeLocaleCompare(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
            evaluator.setLocaleListener(this);
            collator = Collator.getInstance(evaluator.getDefaultLocale());
        }

        @Override
        public ESValue invoke(String str, ESValue[] arguments) throws EcmaScriptException {
            ESValue arg = getArg(arguments, 0);
            String that = arg.toESString().toString();
            return ESNumber.valueOf(collator.compare(str, that));
        }

        public void notify(Locale locale) {
            collator = Collator.getInstance(locale);
        }
    }

    // For stringObject
    private static class StringObjectFromCharCode extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        StringObjectFromCharCode(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
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

    private StringObject(ESObject prototype, Evaluator evaluator) throws EcmaScriptException {
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
    public ESObject doConstruct(ESValue[] arguments)
            throws EcmaScriptException {
        StringPrototype theObject = null;
        ESObject sp = getEvaluator().getStringPrototype();
        theObject = new StringPrototype(sp, getEvaluator());
        if (arguments.length > 0) {
            theObject.value = (ESString)arguments[0].toESString();
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
     * @throws EcmaScriptException 
     */
    public static StringObject makeStringObject(Evaluator evaluator,
            ObjectPrototype objectPrototype, FunctionPrototype functionPrototype) throws EcmaScriptException {

        ESObject stringPrototype = new StringPrototype(objectPrototype,
                evaluator);
        StringObject stringObject = new StringObject(functionPrototype,
                evaluator);

        try {
            stringObject.putProperty(StandardProperty.PROTOTYPEstring, 0, stringPrototype);
            stringObject.putProperty("length", 0, ESNumber.valueOf(1));
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
            stringPrototype.putHiddenProperty("slice",
                    new StringPrototypeSlice("slice", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("split",
                    new StringPrototypeSplit("split", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("substring",
                    new StringPrototypeSubstring("substring", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("toLowerCase",
                    new StringPrototypeToLowerCase("toLowerCase", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("toLocaleLowerCase",
                    new StringPrototypeToLocaleLowerCase("toLocaleLowerCase", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("toUpperCase",
                    new StringPrototypeToUpperCase("toUpperCase", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("toLocaleUpperCase",
                    new StringPrototypeToLocaleUpperCase("toLocaleUpperCase", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("trim", new StringPrototypeTrim("trim", evaluator, functionPrototype));
            stringPrototype.putHiddenProperty("localeCompare",
                    new StringPrototypeLocaleCompare("localeCompare", evaluator, functionPrototype));
            
        } catch (EcmaScriptException e) {
            e.printStackTrace();
            throw new ProgrammingError(e.getMessage());
        }

        evaluator.setStringPrototype(stringPrototype);

        return stringObject;
    }
}
