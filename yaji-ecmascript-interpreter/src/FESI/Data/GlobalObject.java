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
    static final String VALUEstring = ("value").intern();
    static final int VALUEhash = VALUEstring.hashCode();
    static final String ERRORstring = ("error").intern();
    static final int ERRORhash = ERRORstring.hashCode();

    private GlobalObject(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator);
    }

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
            class ObjectPrototypeToString extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ObjectPrototypeToString(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                public ESValue callFunction(ESObject thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    String result = "[object " + thisObject.getESClassName()
                            + "]";
                    return new ESString(result);
                }
            }
            class ObjectPrototypeValueOf extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ObjectPrototypeValueOf(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                public ESValue callFunction(ESObject thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return thisObject;
                }
            }

            // For functionPrototype
            class FunctionPrototypeToString extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                FunctionPrototypeToString(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                public ESValue callFunction(ESObject thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    String s = "function "
                            + ((FunctionPrototype) thisObject)
                                    .getFunctionName()
                            + ((FunctionPrototype) thisObject)
                                    .getFunctionParametersString()
                            + ((FunctionPrototype) thisObject)
                                    .getFunctionImplementationString();
                    return new ESString(s);
                }
            }

            // For GlobalObject
            class GlobalObjectThrowError extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                GlobalObjectThrowError(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                public ESValue callFunction(ESObject thisObject,
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
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                public ESValue callFunction(ESObject thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    ESObject result = ObjectObject.createObject(this
                            .getEvaluator());
                    if (arguments.length < 1) {
                        result.putProperty(ERRORstring, ESNull.theNull,
                                ERRORhash);
                        return result;
                    }
                    if (!(arguments[0] instanceof ESString)) {
                        result
                                .putProperty(VALUEstring, arguments[0],
                                        VALUEhash);
                        result.putProperty(ERRORstring, ESNull.theNull,
                                ERRORhash);
                        return result;
                    }
                    String program = arguments[0].toString();
                    ESValue value = ESUndefined.theUndefined;
                    try {
                        value = this.getEvaluator().evaluateEvalString(program);
                    } catch (EcmaScriptParseException e) {
                        e.setNeverIncomplete();
                        if (arguments.length > 1) {
                            result.putProperty(VALUEstring, arguments[1],
                                    VALUEhash);
                        }
                        result.putProperty(ERRORstring, ESLoader
                                .normalizeValue(e, this.getEvaluator()),
                                ERRORhash);
                        return result;
                    } catch (EcmaScriptException e) {
                        if (arguments.length > 1) {
                            result.putProperty(VALUEstring, arguments[1],
                                    VALUEhash);
                        }
                        result.putProperty(ERRORstring, ESLoader
                                .normalizeValue(e, this.getEvaluator()),
                                ERRORhash);
                        return result;
                    }
                    result.putProperty(VALUEstring, value, VALUEhash);
                    result.putProperty(ERRORstring, ESNull.theNull, ERRORhash);
                    return result;
                }
            }

            class GlobalObjectEval extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                GlobalObjectEval(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                public ESValue callFunction(ESObject thisObject,
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
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 2);
                }

                public ESValue callFunction(ESObject thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    if (arguments.length < 1)
                        return ESUndefined.theUndefined;
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
                        } else if (s.startsWith("0")) {
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
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                public ESValue callFunction(ESObject thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    if (arguments.length < 1)
                        return ESUndefined.theUndefined;
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
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                public ESValue callFunction(ESObject thisObject,
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
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                public ESValue callFunction(ESObject thisObject,
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
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                public ESValue callFunction(ESObject thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    if (arguments.length < 1)
                        return ESUndefined.theUndefined;
                    double d = arguments[0].doubleValue();
                    return ESBoolean.makeBoolean(Double.isNaN(d));
                }
            }

            class GlobalObjectIsFinite extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                GlobalObjectIsFinite(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                public ESValue callFunction(ESObject thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    if (arguments.length < 1)
                        return ESUndefined.theUndefined;
                    double d = arguments[0].doubleValue();
                    return ESBoolean.makeBoolean(!Double.isInfinite(d));
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
            FunctionObject functionObject = new FunctionObject(
                    functionPrototype, evaluator);

            StringObject stringObject = StringObject.makeStringObject(
                    evaluator, objectPrototype, functionPrototype);
            NumberObject numberObject = NumberObject.makeNumberObject(
                    evaluator, objectPrototype, functionPrototype);
            BooleanObject booleanObject = BooleanObject.makeBooleanObject(
                    evaluator, objectPrototype, functionPrototype);
            ArrayObject arrayObject = ArrayObject.makeArrayObject(evaluator,
                    objectPrototype, functionPrototype);
            DateObject dateObject = DateObject.makeDateObject(evaluator,
                    objectPrototype, functionPrototype);

            go = new GlobalObject(objectPrototype, evaluator);

            // Set built-in properties
            objectObject.putHiddenProperty("prototype", objectPrototype);

            objectPrototype.putHiddenProperty("constructor", objectObject);
            objectPrototype.putHiddenProperty("toString",
                    new ObjectPrototypeToString("toString", evaluator,
                            functionPrototype));
            objectPrototype.putHiddenProperty("valueOf",
                    new ObjectPrototypeValueOf("valueOf", evaluator,
                            functionPrototype));

            functionPrototype.putHiddenProperty("constructor", functionObject);
            functionPrototype.putHiddenProperty("toString",
                    new FunctionPrototypeToString("toString", evaluator,
                            functionPrototype));

            functionObject.putHiddenProperty("prototype", functionPrototype);
            functionObject.putHiddenProperty("length", ESNumber.valueOf(1));

            // Save system object so that they can be quickly found
            evaluator.setObjectPrototype(objectPrototype);
            evaluator.setFunctionPrototype(functionPrototype);
            evaluator.setFunctionObject(functionObject);

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

            go.putHiddenProperty("Object", objectObject);
            go.putHiddenProperty("Function", functionObject);
            go.putHiddenProperty("String", stringObject);
            go.putHiddenProperty("Number", numberObject);
            go.putHiddenProperty("Boolean", booleanObject);
            go.putHiddenProperty("Array", arrayObject);
            go.putHiddenProperty("Date", dateObject);

            go.putHiddenProperty("NaN", ESNumber.valueOf(Double.NaN));
            go.putHiddenProperty("Infinity", ESNumber
                    .valueOf(Double.POSITIVE_INFINITY));
            go.putHiddenProperty("Array", ArrayObject.makeArrayObject(
                    evaluator, objectPrototype, functionPrototype));
            go.putHiddenProperty("Math", MathObject.makeMathObject(evaluator,
                    objectPrototype, functionPrototype));
        } catch (EcmaScriptException e) {
            e.printStackTrace();
            throw new ProgrammingError(e.getMessage());
        }
        return go;
    }
}
