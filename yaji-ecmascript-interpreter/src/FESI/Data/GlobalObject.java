// GlobalObject.java
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yaji.data.SparseArrayConstructor;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.EcmaScriptParseException;
import FESI.Exceptions.ProgrammingError;
import FESI.Exceptions.URIError;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.ScopeChain;

/**
 * Implements the EmcaScript 'global' object
 */
public class GlobalObject extends ObjectPrototype {
    private static final long serialVersionUID = -4033899977752030036L;
    public static boolean useSparse = true;

    private static class GlobalObjectParseFloat extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        private static Pattern decimalValidator = Pattern.compile("^[+-]?(([0-9]+\\.?[0-9]*)|(\\.?[0-9]+))([eE][+-]?[0-9]+)?");
        
        GlobalObjectParseFloat(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            if (arguments.length < 1) {
                return ESNumber.NaN;
            }
            String s = ESStringPrimitive.trim(arguments[0].callToString());
            if (s.startsWith("Infinity") || s.startsWith("+Infinity")) {
                return ESNumber.valueOf(Double.POSITIVE_INFINITY);
            }
            if (s.startsWith("-Infinity")) {
                return ESNumber.valueOf(Double.NEGATIVE_INFINITY);
            }
            Matcher matcher = decimalValidator.matcher(s);
            
            double d = Double.NaN;
            if (matcher.find()) {
                matcher.end();
                s = s.substring(0, matcher.end());
                try {
                    d = Double.parseDouble(s);
                } catch (NumberFormatException e) {
                    // will still be NaN
                }
            }
            return ESNumber.valueOf(d);
        }
    }
    
    private static class GlobalObjectParseInt extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        GlobalObjectParseInt(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 2);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            if (arguments.length < 1) {
                return ESNumber.NaN;
            }
            String s = ESStringPrimitive.trim(arguments[0].callToString());
            int radix = 10;
            int start = 0;
            radix = (int) getArg(arguments,1).toUInt32();
            if (radix == 0) {
                radix = 10;
                if (s.startsWith("0x") || s.startsWith("0X")) {
                    radix = 16;
                    start = 2;
                }
            } else {
                if (radix < 2 || radix > 36) {
                    return ESNumber.valueOf(Double.NaN);
                }
                if (radix == 16) {
                    if (s.startsWith("0x") || s.startsWith("0X")) {
                        start = 2;
                    }
                }
            }
            double d = Double.NaN;
            if (s.length() > 0) {
                int sign = 1;
                if (s.charAt(0) == '-') {
                    sign = -1;
                    start = 1;
                } else if (s.charAt(0) == '+') {
                    start = 1;
                }

                for (int i = start; i < s.length(); i++) {
                    char c = s.charAt(i);
                    int digit = 0;
                    if (c >= '0' && c <= '9') {
                        digit = c - '0';
                    } else if (c >= 'a' && c <= 'z') {
                        digit = c - 'a' + 10;
                    } else if (c >= 'A' && c <= 'Z') {
                        digit = c - 'A' + 10;
                    } else {
                        break;
                    }
                    if (digit >= radix) {
                        break;
                    }
                    if (d != d) {
                        d = digit;
                    } else {
                        d = d * radix + digit;
                    }
                }
                d *= sign;
            }
            return ESNumber.valueOf(d);
        }
    }

    @Override
    public ESValue doIndirectCall(Evaluator evaluator, ESObject target,
            String functionName, ESValue[] arguments)
                    throws EcmaScriptException, NoSuchMethodException {
        if (functionName.equals("eval")) {
            getEvaluator().setDirectCallToEval(true);
        }
        return super.doIndirectCall(evaluator, target, functionName, arguments);
    }

    @Override
    public ESValue doIndirectCallInScope(Evaluator evaluator,
            ScopeChain previousScope, ESObject thisObject, String functionName,
            int hash, ESValue[] arguments) throws EcmaScriptException {
        if (functionName.equals("eval")) {
            getEvaluator().setDirectCallToEval(true);
        }
        return super.doIndirectCallInScope(evaluator, previousScope, thisObject,
                functionName, hash, arguments);
    }
    
    private GlobalObject(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator);
    }

    /**
     * Create the single global object
     * 
     * @param evaluator
     *            theEvaluator
     * @return the 'global' singleton
     */
    static public GlobalObject makeGlobalObject(Evaluator evaluator) {

        GlobalObject go = null;
        try {

            // For objectPrototype
            // For GlobalObject
            class GlobalObjectThrowError extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                GlobalObjectThrowError(String name, Evaluator evaluator,
                        FunctionPrototype fp) throws EcmaScriptException {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    ObjectObject.createObject(this.getEvaluator());
                    if (arguments.length < 1) {
                        throw new EcmaScriptException(
                                "Exception thrown by throwError");
                    }
                    if (arguments[0] instanceof ESWrapper) {
                        Object o = ((ESWrapper) arguments[0]).getJavaObject();
                        if (o instanceof Throwable) {
                            throw new EcmaScriptException(o.toString(),
                                    (Throwable) o);
                        }
                        throw new EcmaScriptException(o.toString());

                    }
                    String text = arguments[0].toString();
                    throw new EcmaScriptException(text);
                }
            }

            class GlobalObjectTryEval extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                GlobalObjectTryEval(String name, Evaluator evaluator,
                        FunctionPrototype fp) throws EcmaScriptException {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    ESObject result = ObjectObject.createObject(this
                            .getEvaluator());
                    if (arguments.length < 1) {
                        result.putProperty(StandardProperty.ERRORstring, ESNull.theNull,
                                StandardProperty.ERRORhash);
                        return result;
                    }
                    if (!(arguments[0] instanceof ESString)) {
                        result.putProperty(StandardProperty.VALUEstring, arguments[0],
                                        StandardProperty.VALUEhash);
                        result.putProperty(StandardProperty.ERRORstring, ESNull.theNull,
                                StandardProperty.ERRORhash);
                        return result;
                    }
                    String program = arguments[0].toString();
                    ESValue value = ESUndefined.theUndefined;
                    try {
                        value = this.getEvaluator().evaluateEvalString(program);
                    } catch (EcmaScriptParseException e) {
                        e.setNeverIncomplete();
                        if (arguments.length > 1) {
                            result.putProperty(StandardProperty.VALUEstring, arguments[1],
                                    StandardProperty.VALUEhash);
                        }
                        result.putProperty(StandardProperty.ERRORstring, ESLoader
                                .normalizeValue(e, this.getEvaluator()),
                                StandardProperty.ERRORhash);
                        return result;
                    } catch (EcmaScriptException e) {
                        if (arguments.length > 1) {
                            result.putProperty(StandardProperty.VALUEstring, arguments[1],
                                    StandardProperty.VALUEhash);
                        }
                        result.putProperty(StandardProperty.ERRORstring, ESLoader
                                .normalizeValue(e, this.getEvaluator()),
                                StandardProperty.ERRORhash);
                        return result;
                    }
                    result.putProperty(StandardProperty.VALUEstring, value, StandardProperty.VALUEhash);
                    result.putProperty(StandardProperty.ERRORstring, ESNull.theNull, StandardProperty.ERRORhash);
                    return result;
                }
            }

            class GlobalObjectEval extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                GlobalObjectEval(String name, Evaluator evaluator,
                        FunctionPrototype fp) throws EcmaScriptException {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    if (arguments.length < 1)
                        return ESUndefined.theUndefined;
                    if (!(arguments[0] instanceof ESString))
                        return arguments[0];
                    String program = arguments[0].toString();
                    ESValue value = ESUndefined.theUndefined;
                    Evaluator evaluator = this.getEvaluator();
                    try {
                        value = evaluator.evaluateEvalString(program);
                    } catch (EcmaScriptParseException e) {
                        e.setNeverIncomplete();
                        throw e;
                    }
                    return value;
                }
            }


            class GlobalObjectEscape extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                GlobalObjectEscape(String name, Evaluator evaluator,
                        FunctionPrototype fp) throws EcmaScriptException {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    if (arguments.length <= 0) {
                        return ESUndefined.theUndefined;
                    }
                    String src = arguments[0].toString();
                    final int srcLength = src.length();
                    StringBuilder dst = new StringBuilder(
                            srcLength > 16 ? srcLength : 16);
                    for (int i = 0; i < srcLength; i++) {
                        char c = src.charAt(i);
                        if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')
                                || ('0' <= c && c <= '9') || c == '@'
                                || c == '*' || c == '_' || c == '+' || c == '-'
                                || c == '.' || c == '/') {
                            dst.append(c);
                        } else if (c <= (char) 0xF) {
                            dst.append("%0" + Integer.toHexString(c));
                        } else if (c <= (char) 0xFF) {
                            dst.append("%" + Integer.toHexString(c));
                        } else if (c <= (char) 0xFFF) {
                            dst.append("%u0" + Integer.toHexString(c));
                        } else {
                            dst.append("%u" + Integer.toHexString(c));
                        }
                    }
                    return new ESString(dst.toString());

                }
            }

            class GlobalObjectUnescape extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                GlobalObjectUnescape(String name, Evaluator evaluator,
                        FunctionPrototype fp) throws EcmaScriptException {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    if (arguments.length <= 0) {
                        return ESUndefined.theUndefined;
                    }
                    StringBuilder dst = new StringBuilder();
                    String src = arguments[0].toString();
                    for (int i = 0; i < src.length(); i++) {
                        char c = src.charAt(i);
                        if (c == '%') {
                            StringBuilder d = new StringBuilder();
                            c = src.charAt(++i); // May raise exception
                            if (c == 'u' || c == 'U') {
                                d.append(src.charAt(++i)); // May raise
                                // exception
                                d.append(src.charAt(++i)); // May raise
                                // exception
                                d.append(src.charAt(++i)); // May raise
                                // exception
                                d.append(src.charAt(++i)); // May raise
                                // exception
                            } else {
                                d.append(src.charAt(i)); // May raise exception
                                d.append(src.charAt(++i)); // May raise
                                // exception
                            }
                            c = (char) Integer.parseInt(d.toString(), 16);

                        }
                        dst.append(c);
                    }
                    return new ESString(dst.toString());

                }
            }

            class GlobalObjectIsNaN extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                GlobalObjectIsNaN(String name, Evaluator evaluator,
                        FunctionPrototype fp) throws EcmaScriptException {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    if (arguments.length < 1)
                        return ESUndefined.theUndefined;
                    double d = arguments[0].toESNumber().doubleValue();
                    return ESBoolean.valueOf(Double.isNaN(d));
                }
            }
            class GlobalObjectIsFinite extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                GlobalObjectIsFinite(String name, Evaluator evaluator,
                        FunctionPrototype fp) throws EcmaScriptException {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    if (arguments.length < 1)
                        return ESUndefined.theUndefined;
                    double d = arguments[0].toESNumber().doubleValue();
                    return ESBoolean.valueOf(!Double.isInfinite(d) && !Double.isNaN(d));
                }
            }
            class GlobalObjectURIHandler extends BuiltinFunctionObject {
                public static final int ENCODE = 0x01;
                public static final int DECODE = 0x02;
                public static final int COMPONENT = 0x04;

                private static final long serialVersionUID = 1L;

                private final int opts;

                GlobalObjectURIHandler(String name, Evaluator evaluator,
                        FunctionPrototype fp, int opts) throws EcmaScriptException {
                    super(fp, evaluator, name, 1);
                    this.opts = opts;
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    if (arguments.length >= 1) {
                        String s = getArg(arguments,0).callToString();
                        switch (opts) {
                        case ENCODE:
                        case ENCODE | COMPONENT:
                            return new ESString(URIHandler.encode(s,
                                    (opts & COMPONENT) == 0));
                        case DECODE:
                        case DECODE | COMPONENT:
                            return new ESString(URIHandler.decode(s,
                                    (opts & COMPONENT) == 0));
                        }
                    }
                    return ESUndefined.theUndefined;
                }
            }

            
            // Create object (not yet usable!) in right order for
            // property chain
            ObjectPrototype objectPrototype = new ObjectPrototype(null,
                    evaluator);
            FunctionPrototype functionPrototype = new FunctionPrototype(
                    objectPrototype, evaluator, "[Function Prototype]", 0);
            ObjectObject objectObject = new ObjectObject(functionPrototype,
                    evaluator);
            evaluator.setFunctionPrototype(functionPrototype);
            FunctionObject functionObject = new FunctionObject(
                    functionPrototype, evaluator);

            StringObject stringObject = StringObject.makeStringObject(
                    evaluator, objectPrototype, functionPrototype);
            NumberObject numberObject = NumberObject.makeNumberObject(
                    evaluator, objectPrototype, functionPrototype);
            BooleanObject booleanObject = BooleanObject.makeBooleanObject(
                    evaluator, objectPrototype, functionPrototype);
            ESObject arrayObject;
            if (useSparse) {
                arrayObject = SparseArrayConstructor.makeArrayObject(evaluator, objectPrototype, functionPrototype);
            } else {
                arrayObject = ArrayObject.makeArrayObject(evaluator, objectPrototype, functionPrototype);
            }
            DateObject dateObject = DateObject.makeDateObject(evaluator,
                    objectPrototype, functionPrototype);

            go = new GlobalObject(objectPrototype, evaluator);

            // Set built-in properties
            objectObject.putProperty(StandardProperty.PROTOTYPEstring, 0, objectPrototype);

            objectPrototype.initialise(objectObject, evaluator, functionPrototype);
            
            ErrorObject errorObject = ErrorObject.make(evaluator, objectPrototype, functionPrototype);
            ErrorPrototype errorPrototype = errorObject.getPrototypeProperty();

            ESObject esRegExpPrototype = new RegExpPrototype(objectPrototype, evaluator);
            ESObject globalObjectRegExp = new RegExpObject(StandardProperty.REG_EXPstring,
                    evaluator, functionPrototype, esRegExpPrototype);
            globalObjectRegExp.putProperty(StandardProperty.PROTOTYPEstring, 0, esRegExpPrototype);

            esRegExpPrototype.putHiddenProperty("constructor", globalObjectRegExp);
        
            // Save system object so that they can be quickly found
            evaluator.setObjectPrototype(objectPrototype);
            evaluator.setFunctionObject(functionObject);
            evaluator.setRegExpPrototype(esRegExpPrototype);

            // Populate the global object
            go.putHiddenProperty("throwError", new GlobalObjectThrowError(
                    "throwError", evaluator, functionPrototype));
            go.putHiddenProperty("tryEval", new GlobalObjectTryEval("tryEval",
                    evaluator, functionPrototype));
            go.putHiddenProperty("eval", new GlobalObjectEval("eval",
                    evaluator, functionPrototype));
            go.putHiddenProperty("parseInt", new GlobalObjectParseInt(
                    "parseInt", evaluator, functionPrototype));
            go.putHiddenProperty("parseFloat", new GlobalObjectParseFloat(
                    "parseFloat", evaluator, functionPrototype));
            go.putHiddenProperty("escape", new GlobalObjectEscape("escape",
                    evaluator, functionPrototype));
            go.putHiddenProperty("unescape", new GlobalObjectUnescape(
                    "unescape", evaluator, functionPrototype));
            go.putHiddenProperty("isNaN", new GlobalObjectIsNaN("isNaN",
                    evaluator, functionPrototype));
            go.putHiddenProperty("isFinite", new GlobalObjectIsFinite(
                    "isFinite", evaluator, functionPrototype));

            go.putHiddenProperty("encodeURI", 
                    new GlobalObjectURIHandler("encodeURI", evaluator, 
                            functionPrototype, 
                            GlobalObjectURIHandler.ENCODE));
            go.putHiddenProperty("encodeURIComponent", 
                    new GlobalObjectURIHandler("encodeURIComponent", evaluator, 
                            functionPrototype, 
                            GlobalObjectURIHandler.ENCODE | 
                            GlobalObjectURIHandler.COMPONENT));
            go.putHiddenProperty("decodeURI", 
                    new GlobalObjectURIHandler("decodeURI", evaluator, 
                            functionPrototype, 
                            GlobalObjectURIHandler.DECODE));
            go.putHiddenProperty("decodeURIComponent", 
                    new GlobalObjectURIHandler("decodeURIComponent", evaluator,
                            functionPrototype, 
                            GlobalObjectURIHandler.DECODE | 
                            GlobalObjectURIHandler.COMPONENT));

            go.putHiddenProperty("Object", objectObject);
            go.putHiddenProperty("Function", functionObject);
            go.putHiddenProperty("String", stringObject);
            go.putHiddenProperty("Number", numberObject);
            go.putHiddenProperty("Boolean", booleanObject);
            go.putHiddenProperty(StandardProperty.ARRAYstring, arrayObject);
            go.putHiddenProperty("Date", dateObject);
            go.putHiddenProperty(StandardProperty.REG_EXPstring, globalObjectRegExp);
            
            go.putHiddenProperty("Error", errorObject);
            go.putHiddenProperty(NativeErrorObject.EVAL_ERROR,NativeErrorObject.make(NativeErrorObject.EVAL_ERROR,evaluator,errorPrototype,functionPrototype));
            go.putHiddenProperty(NativeErrorObject.RANGE_ERROR,NativeErrorObject.make(NativeErrorObject.RANGE_ERROR,evaluator,errorPrototype,functionPrototype));
            go.putHiddenProperty(NativeErrorObject.REFERENCE_ERROR,NativeErrorObject.make(NativeErrorObject.REFERENCE_ERROR,evaluator,errorPrototype,functionPrototype));
            go.putHiddenProperty(NativeErrorObject.SYNTAX_ERROR,NativeErrorObject.make(NativeErrorObject.SYNTAX_ERROR,evaluator,errorPrototype,functionPrototype));
            go.putHiddenProperty(NativeErrorObject.TYPE_ERROR,NativeErrorObject.make(NativeErrorObject.TYPE_ERROR,evaluator,errorPrototype,functionPrototype));
            go.putHiddenProperty(NativeErrorObject.URI_ERROR,NativeErrorObject.make(NativeErrorObject.URI_ERROR,evaluator,errorPrototype,functionPrototype));

            go.putProperty("NaN", 0, ESNumber.valueOf(Double.NaN));
            go.putProperty("undefined", 0, ESUndefined.theUndefined);
            go.putProperty("Infinity", 0, ESNumber
                    .valueOf(Double.POSITIVE_INFINITY));
            go.putHiddenProperty("Math", MathObject.makeMathObject(evaluator,
                    objectPrototype, functionPrototype));
            go.putHiddenProperty("JSON", JsonObject.makeJsonObject(evaluator, objectPrototype, functionPrototype));
        } catch (EcmaScriptException e) {
            e.printStackTrace();
            throw new ProgrammingError(e.getMessage());
        }
        return go;
    }
}

class URIHandler {
    private static final String ENCODING = "UTF-8";
    private static final BitSet UNESCAPED_SET = new BitSet();
    private static final BitSet RESERVED_SET = new BitSet();
    private static final BitSet UNESCAPED_AND_RESERVED_SET = new BitSet();
    private static final BitSet EMPTY_SET = new BitSet();

    static {
        for (int i = 'a'; i <= 'z'; i++) {
            UNESCAPED_SET.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            UNESCAPED_SET.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            UNESCAPED_SET.set(i);
        }
        UNESCAPED_SET.set('-');
        UNESCAPED_SET.set('_');
        UNESCAPED_SET.set('.');
        UNESCAPED_SET.set('!');
        UNESCAPED_SET.set('~');
        UNESCAPED_SET.set('*');
        UNESCAPED_SET.set('\'');
        UNESCAPED_SET.set('(');
        UNESCAPED_SET.set(')');

        RESERVED_SET.set(';');
        RESERVED_SET.set('/');
        RESERVED_SET.set('?');
        RESERVED_SET.set(':');
        RESERVED_SET.set('@');
        RESERVED_SET.set('&');
        RESERVED_SET.set('=');
        RESERVED_SET.set('+');
        RESERVED_SET.set('$');
        RESERVED_SET.set(',');
        RESERVED_SET.set('#');

        UNESCAPED_AND_RESERVED_SET.or(UNESCAPED_SET);
        UNESCAPED_AND_RESERVED_SET.or(RESERVED_SET);
    }
    
    public static String encode(String s, boolean escapeReserved)
            throws EcmaScriptException {
        int len = s.length();

        // 6 bytes should cater for all surrogate pairs
        ByteArrayOutputStream buf = new ByteArrayOutputStream(6);
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(buf, ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new URIError(e.getMessage());
        }
        StringBuilder sb = new StringBuilder(len);
        
        for (int i = 0; i < len; i++) {
            int c = s.charAt(i);
            if (escapeReserved ? UNESCAPED_AND_RESERVED_SET.get(c)
                    : UNESCAPED_SET.get(c)) {
                sb.append((char) c);
            } else {
                if (c >= 0xDC00 && c <= 0xDFFF) {
                    throw new URIError("Invalid Unicode Character");
                }
                try {
                    writer.write(c);
                    if (c >= 0xD800 && c <= 0xDBFF) {
                        if ((i + 1) < len) {
                            /*
                             * 'c' may be the first code unit of a Unicode
                             * surrogate pair (high surrogate). 'c2' should be
                             * the other half (low surrogate), but only if it is
                             * in range. Otherwise, just continue and pick it up
                             * as the next character.
                             */
                            int c2 = s.charAt(i + 1);
                            if (c2 >= 0xDC00 && c2 <= 0xDFFF) {
                                writer.write(c2);
                                i++;
                            } else {
                                throw new URIError("Invalid unicode surrogate pair");
                            }
                        } else {
                            throw new URIError("Unterminated unicode surrogate pair");
                        }
                    }
                    writer.flush();
                } catch (IOException e) {
                    buf.reset();
                    continue;
                }
               
                // Convert to hex
                for (byte b : buf.toByteArray()) {
                    sb.append("%"+String.format("%02X", Byte.valueOf(b)));
                }
                buf.reset();
            }
        }
        return sb.toString();
    }

    
    private static class Decoder {
        private StringBuilder sb = new StringBuilder();
        private char[] string;
        private final BitSet reservedSet;
        private int k;
        
        public Decoder(String s, BitSet reservedSet) {
            this.reservedSet = reservedSet;
            string = s.toCharArray();
            k = 0;
        }
        
        public String decode() throws URIError {
            while (k < string.length) {
                char c = string[k];
                if (c != '%') {
                    sb.append(c);
                } else {
                    int start = k;
                    int b = decodeHexEscape();
                    k += 2;
                    if ((b & 0x80) == 0) {
                        if (reservedSet.get(b)) {
                            sb.append(string,start,k-start+1);
                        } else {
                            sb.append((char)b);
                        }
                    } else {
                        int n = 1;
                        while ( ((b << n) & 0x80) != 0) {
                            n++;
                        }
                        if (n == 1 || n > 4) {
                            throw newException("Invalid UTF sequence");
                        }
                        byte [] octets = new byte[n];
                        octets[0] = (byte)b;
                        if (k + (3 * (n - 1)) >= string.length) {
                            throw newException("Incomplete multi-byte escape");
                        }
                        for(int j=1; j<n; j++) {
                            k++;
                            if (string[k] != '%') {
                                throw newException("Incomplete multi-byte escape");
                            }
                            b = decodeHexEscape();
                            if ( (b & 0xc0) != 0x80) {
                                throw newException("Invalid UTF sequence");
                            }
                            octets[j] = (byte)b;
                            k += 2;
                        }
                        int v = utf8transformFrom(octets);
                        if (v < 0x10000) {
                            if (reservedSet.get(v)) {
                                sb.append(string,start,start-k+1);
                            } else {
                                sb.append((char)v);
                            }
                        } else {
                            int l = ((v - 0x10000) & 0x3ff) | 0xDC00;
                            int h = (((v - 0x10000)>>10) & 0x3ff) | 0xD800;
                            sb.append((char)h).append((char)l);
                        }
                    }
                }
                k++;
            }
            return sb.toString();
        }

        /*
           Char. number range  |        UTF-8 octet sequence
              (hexadecimal)    |              (binary)
           --------------------+---------------------------------------------
           0000 0000-0000 007F | 0xxxxxxx
           0000 0080-0000 07FF | 110xxxxx 10xxxxxx
           0000 0800-0000 FFFF | 1110xxxx 10xxxxxx 10xxxxxx
           0001 0000-0010 FFFF | 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
 

         */
        private int utf8transformFrom(byte[] octets) throws URIError {
            byte b = octets[0];
            if ( (b & 0x80) == 0) {
                return b;
            }
            if ((b & 0xE0) == 0xC0) {
                int h = b & 0x1f;
//                if (h == 0) {
//                    throw newException("Invalid UTF-8 encoding");
//                }
                int l = bits(octets[1]);
                return (h<<6)+l;
            }
            if ((b & 0xF0) == 0xE0) {
                int h = b & 0xf;
//                if (h == 0) {
//                    throw newException("Invalid UTF-8 encoding");
//                }
                int m = bits(octets[1]);
                int l = bits(octets[2]);
                return (h<<12)+(m<<6)+l;
            }
            if ((b & 0xf8) != 0xF0) {
                throw newException("Invalid UTF-8 encoding");
            }
            int h = b & 7;
//            if (h == 0) {
//                throw newException("Invalid UTF-8 encoding");
//            }
            int m1 = bits(octets[1]);
            int m2 = bits(octets[2]);
            int l = bits(octets[3]);
            return (h<<18)+(m1<<12)+(m2<<6)+l;
        }
        
        int bits(int b) throws URIError {
            if ((b & 0xc0) != 0x80) {
                throw newException("Invalid UTF-8 encoding");
            }
            return b & 0x3f;
        }

        public URIError newException(String reason) {
            return new URIError(reason+" in URI "+(new String(string)) + " position "+k);
        }

        public int decodeHexEscape() throws URIError {
            if ((k+2) >= string.length) {
                throw new URIError("Incomplete hex literal found at end of URI");
            }
            int k1 = toHexDigit(string[k+1]);
            int k2 = toHexDigit(string[k+2]);
            if (k1 == -1 || k2 == -1){
                throw new URIError("Invalid hex literal found in URI "+(new String(string))+" position "+k);
            }
            int b = 16*k1 + k2;
            return b;
        }

        private int toHexDigit(char c) {
            switch(c) {
            case '0':
                return 0;
            case '1':
                return 1;
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'a':
            case 'A':
                return 10;
            case 'b':
            case 'B':
                return 11;
            case 'c':
            case 'C':
                return 12;
            case 'd':
            case 'D':
                return 13;
            case 'e':
            case 'E':
                return 14;
            case 'f':
            case 'F':
                return 15;
            }
            return -1;
        }
    }
    public static String decode(String s, boolean unescapeReserved)
            throws EcmaScriptException {
        return new Decoder(s,unescapeReserved?RESERVED_SET:EMPTY_SET).decode();
    }

}
