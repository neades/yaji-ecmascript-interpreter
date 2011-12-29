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

import org.yaji.data.SparseArrayConstructor;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.EcmaScriptParseException;
import FESI.Exceptions.ProgrammingError;
import FESI.Interpreter.EvaluationSource;
import FESI.Interpreter.Evaluator;

/**
 * Implements the EmcaScript 'global' object
 */
public class GlobalObject extends ObjectPrototype {
    private static final long serialVersionUID = -4033899977752030036L;
    public static boolean useSparse = true;
    
    private GlobalObject(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator);
    }

    @Override
    public void putProperty(String propertyName, ESValue propertyValue, int hash)
            throws EcmaScriptException {
        StringBuilder sb = new StringBuilder(64);
        sb.append("Warning, setting property [");
        sb.append(propertyName);
        sb.append("] on GLOBAL SCOPE within context [");
        sb.append(getEvaluator().getThisObject());
        sb.append("]\n");

        EvaluationSource currentEvaluationSource = getEvaluator()
                .getCurrentEvaluationSource();
        if (currentEvaluationSource != null) {
            sb.append(currentEvaluationSource.toString());
        }

        getEvaluator().getLog().asWarning(sb.toString());

        super.putProperty(propertyName, propertyValue, hash);
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
                        result
                                .putProperty(StandardProperty.VALUEstring, arguments[0],
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
                    try {
                        value = this.getEvaluator().evaluateEvalString(program);
                    } catch (EcmaScriptParseException e) {
                        e.setNeverIncomplete();
                        throw e;
                    }
                    return value;
                }
            }

            class GlobalObjectParseInt extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                GlobalObjectParseInt(String name, Evaluator evaluator,
                        FunctionPrototype fp) throws EcmaScriptException {
                    super(fp, evaluator, name, 2);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    if (arguments.length < 1)
                        return ESNumber.valueOf(Double.NaN);
                    int radix = 10;
                    String s = arguments[0].toString().trim();
                    if (arguments.length > 1) {
                        radix = arguments[1].toInt32();
                        if (radix < 2 || radix > 36)
                            return ESNumber.valueOf(Double.NaN);
                        if (radix == 16) {
                            if (s.startsWith("0x") || s.startsWith("0X")) {
                                s = s.substring(2);
                            }
                        }
                    } else {
                        if (s.startsWith("0x") || s.startsWith("0X")) {
                            s = s.substring(2);
                            radix = 16;
                        } else if (s.length() > 0 && s.charAt(0) == '0') {
                            radix = 8;
                        }
                    }
                    double d = Double.NaN;
                    int k = -1;
                    for (int i = 0; i < s.length() && k == -1; i++) {
                        char c = s.charAt(i);
                        switch (radix) {
                        case 2:
                            if (c < '0' || '1' < c)
                                k = i;
                            break;
                        case 8:
                            if (c < '0' || '7' < c)
                                k = i;
                            break;
                        case 10:
                            if (c < '0' || '9' < c)
                                k = i;
                            break;
                        case 16:
                            if ((c < '0' || '9' < c) && (c < 'a' || 'f' < c)
                                    && (c < 'A' || 'F' < c))
                                k = i;
                            break;
                        default:
                            throw new EcmaScriptException(
                                    "Only radix 2,8,10 and 16 supported");
                        }
                    }
                    if (k > 0)
                        s = s.substring(0, k);
                    if (s.length() > 0) {
                        try {
                            d = Long.parseLong(s, radix);
                        } catch (NumberFormatException e) {
                            // do nothing
                        }
                    }
                    return ESNumber.valueOf(d);
                }
            }

            class GlobalObjectParseFloat extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                GlobalObjectParseFloat(String name, Evaluator evaluator,
                        FunctionPrototype fp) throws EcmaScriptException {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    if (arguments.length < 1)
                        return ESNumber.valueOf(Double.NaN);
                    String s = arguments[0].toString().trim();
                    Double d = new Double(Double.NaN);
                    int i; // approximate look for a prefix
                    boolean efound = false;
                    boolean dotfound = false;
                    for (i = 0; i < s.length(); i++) {
                        char c = s.charAt(i);
                        if ('0' <= c && c <= '9')
                            continue;
                        if (c == '+' || c == '-')
                            continue; // accept sequences of signs...
                        if (c == 'e' || c == 'E') {
                            if (efound)
                                break;
                            efound = true;
                            continue;
                        }
                        if (c == '.') {
                            if (dotfound || efound)
                                break;
                            dotfound = true;
                            continue;
                        }
                        break;
                    }
                    // System.out.println("i="+i+", s="+s);
                    s = s.substring(0, i);
                    try {
                        d = Double.valueOf(s);
                    } catch (NumberFormatException e) {
                        // do nothing
                    }
                    return ESNumber.valueOf(d.doubleValue());
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
                    double d = arguments[0].doubleValue();
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
                    double d = arguments[0].doubleValue();
                    return ESBoolean.valueOf(!Double.isInfinite(d));
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
                        String s = String.valueOf(arguments[0]);
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
            globalObjectRegExp.putHiddenProperty("length", ESNumber.valueOf(1));

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
            throw new EcmaScriptException(e.getMessage());
        }
        StringBuilder sb = new StringBuilder(len);
        
        for (int i = 0; i < len; i++) {
            int c = s.charAt(i);
            if (escapeReserved ? UNESCAPED_AND_RESERVED_SET.get(c)
                    : UNESCAPED_SET.get(c)) {
                sb.append((char) c);
            } else {
                // Translate to UTF-8
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
                            }
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

    public static String decode(String s, boolean unescapeReserved)
            throws EcmaScriptException {
        int len = s.length();
        
        int i = 0;
        char c;
        byte[] buf = new byte[len];
        StringBuilder sb = new StringBuilder();
        
        while (i < len) {
            c = s.charAt(i);
            if (c != '%') {
                sb.append(c);
                i++;
            } else {
                try {
                    int j = 0;

                    while (((i + 2) < len) && (c == '%')) {
                        String hexStr = s.substring(i + 1, i + 3);
                        byte intVal = (byte) Integer.parseInt(hexStr, 16);
                        if (unescapeReserved && intVal > 0
                                && RESERVED_SET.get(intVal)) {
                            byte[] hexBytes = hexStr.getBytes();
                            buf[j++] = '%';
                            buf[j++] = hexBytes[0];
                            buf[j++] = hexBytes[1];
                        } else {
                            buf[j++] = intVal;
                        }

                        i += 3;
                        if (i < len) {
                            c = s.charAt(i);
                        }
                    }

                    if ((i < len) && (c == '%')) {
                        throw new EcmaScriptException(
                                "Incomplete hex literal found at end of URI");
                    }
                    
                    try {
                        sb.append(new String(buf, 0, j, ENCODING));
                    } catch (UnsupportedEncodingException e) {
                        throw new EcmaScriptException(e.getMessage());
                    }
                } catch (NumberFormatException e) {
                    throw new EcmaScriptException(e.getMessage());
                }
            }
        }
        return sb.toString();
    }
}
