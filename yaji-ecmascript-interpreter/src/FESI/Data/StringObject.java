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

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.ProgrammingError;
import FESI.Interpreter.Evaluator;

/**
 * Implemements the EcmaScript String singleton.
 */
public class StringObject extends BuiltinFunctionObject {

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
            // For stringPrototype
            class StringPrototypeToString extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                StringPrototypeToString(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((StringPrototype) thisObject).value;
                }
            }

            class StringPrototypeValueOf extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                StringPrototypeValueOf(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((StringPrototype) thisObject).value;
                }
            }

            class StringPrototypeCharAt extends BuiltinFunctionObject {
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

            class StringPrototypeCharCodeAt extends BuiltinFunctionObject {
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

            class StringPrototypeIndexOf extends BuiltinFunctionObject {
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

            class StringPrototypeLastIndexOf extends BuiltinFunctionObject {
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

            // This code is replaced by the ReegExp variant when RegExp is
            // loaded
            class StringPrototypeSplit extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                StringPrototypeSplit(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    String str = thisObject.toString();
                    ESObject ap = this.getEvaluator().getArrayPrototype();
                    ArrayPrototype theArray = new ArrayPrototype(ap, this
                            .getEvaluator());
                    if (arguments.length <= 0) {
                        theArray.setSize(1);
                        theArray.setElementAt(thisObject, 0);
                    } else {
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
                                if (pos < 0) {
                                    pos = str.length();
                                }
                                // System.out.println("start: " + start +
                                // ", pos: " + pos);
                                theArray.setSize(i + 1);
                                theArray.setElementAt(new ESString(str
                                        .substring(start, pos)), i);
                                start = pos + sep.length();
                                i++;
                            }
                        }
                    }
                    return theArray;
                }
            }

            class StringPrototypeSubstring extends BuiltinFunctionObject {
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

            class StringPrototypeToLowerCase extends BuiltinFunctionObject {
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

            class StringPrototypeToUpperCase extends BuiltinFunctionObject {
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
            class StringObjectFromCharCode extends BuiltinFunctionObject {
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

            stringObject.putHiddenProperty("prototype", stringPrototype);
            stringObject.putHiddenProperty("length", ESNumber.valueOf(1));
            stringObject.putHiddenProperty("fromCharCode",
                    new StringObjectFromCharCode("fromCharCode", evaluator,
                            functionPrototype));

            stringPrototype.putHiddenProperty("constructor", stringObject);
            stringPrototype.putHiddenProperty("toString",
                    new StringPrototypeToString("toString", evaluator,
                            functionPrototype));
            stringPrototype.putHiddenProperty("valueOf",
                    new StringPrototypeValueOf("valueOf", evaluator,
                            functionPrototype));
            stringPrototype.putHiddenProperty("charAt",
                    new StringPrototypeCharAt("charAt", evaluator,
                            functionPrototype));
            stringPrototype.putHiddenProperty("charCodeAt",
                    new StringPrototypeCharCodeAt("charCodeAt", evaluator,
                            functionPrototype));
            stringPrototype.putHiddenProperty("indexOf",
                    new StringPrototypeIndexOf("indexOf", evaluator,
                            functionPrototype));
            stringPrototype.putHiddenProperty("lastIndexOf",
                    new StringPrototypeLastIndexOf("lastIndexOf", evaluator,
                            functionPrototype));
            stringPrototype.putHiddenProperty("split",
                    new StringPrototypeSplit("split", evaluator,
                            functionPrototype));
            stringPrototype.putHiddenProperty("substring",
                    new StringPrototypeSubstring("substring", evaluator,
                            functionPrototype));
            stringPrototype.putHiddenProperty("toLowerCase",
                    new StringPrototypeToLowerCase("toLowerCase", evaluator,
                            functionPrototype));
            stringPrototype.putHiddenProperty("toUpperCase",
                    new StringPrototypeToUpperCase("toUpperCase", evaluator,
                            functionPrototype));
        } catch (EcmaScriptException e) {
            e.printStackTrace();
            throw new ProgrammingError(e.getMessage());
        }

        evaluator.setStringPrototype(stringPrototype);

        return stringObject;
    }
}
