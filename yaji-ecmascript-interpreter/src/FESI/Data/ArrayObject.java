// ArrayObject.java
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
 * Implements the Array EcmaScript object. This is a singleton
 */
public class ArrayObject extends BuiltinFunctionObject {
    private static final long serialVersionUID = 1737676943108087235L;
    static final String JOINstring = ("join").intern();
    static final int JOINhash = JOINstring.hashCode();
    private static final String ZEROstring = ("0").intern();
    private static final int ZEROhash = ZEROstring.hashCode();

    /**
     * Create a new Array object - used by makeArrayObject
     * 
     * @param prototype
     *            Must be an ArrayPrototype
     * @param evaluator
     *            the evaluator
     */
    private ArrayObject(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator, "Array", 1);
    }

    public static ArrayPrototype createArray(Evaluator evaluator) {
        ESObject ap = evaluator.getArrayPrototype();
        return new ArrayPrototype(ap, evaluator);
    }
    // overrides
    @Override
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        return doConstruct(arguments);
    }

    // overrides
    @Override
    public ESObject doConstruct(ESValue[] arguments)
            throws EcmaScriptException {
        ESObject ap = getEvaluator().getArrayPrototype();
        ArrayPrototype theArray = new ArrayPrototype(ap, getEvaluator());
        if (arguments.length > 1) {
            for (int i = 0; i < arguments.length; i++) {
                String iString = Integer.toString(i);
                theArray.putProperty(iString, arguments[i], iString.hashCode());
            }
        } else if (arguments.length == 1) {
            ESValue firstArg = arguments[0];
            // Not clear in standard:
            if (firstArg.isNumberValue()) {
                theArray.putProperty(StandardProperty.LENGTHstring, firstArg, StandardProperty.LENGTHhash);
            } else {
                theArray.putProperty(ZEROstring, firstArg, ZEROhash);
            }
        }
        return theArray;
    }
    
    public ESValue isArray(ESValue[] args) throws EcmaScriptException {
        if (args.length >= 1) {
            if (args[0] instanceof ESObject 
                    && "Array".equals(((ESObject)args[0]).getESClassName())) {
                return ESBoolean.valueOf(true);
            }
        }
        return ESBoolean.valueOf(false);
    }

    /**
     * Utility function to create the single Array object
     * 
     * @param evaluator
     *            the Evaluator
     * @param objectPrototype
     *            The Object prototype attached to the evaluator
     * @param functionPrototype
     *            The Function prototype attached to the evaluator
     * 
     * @return The Array singleton
     */

    public static ArrayObject makeArrayObject(Evaluator evaluator,
            final ObjectPrototype objectPrototype, FunctionPrototype functionPrototype) {

        ArrayPrototype arrayPrototype = new ArrayPrototype(objectPrototype,
                evaluator);
        ArrayObject arrayObject = new ArrayObject(functionPrototype, evaluator);

        try {
            /*
             * ES5 15.4.4 - Properties of the Array Prototype Object
             */
            class ArrayPrototypeToString extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeToString(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject, ESValue[] arguments) throws EcmaScriptException {
                    ESValue function = thisObject.toESObject(getEvaluator()).getProperty(JOINstring, JOINhash);

                    if (!(function instanceof FunctionPrototype)) {
                        function = getEvaluator().getObjectPrototype().getProperty(StandardProperty.TOSTRINGstring, StandardProperty.TOSTRINGhash);
                        System.out.println(function.toString());
                    }
                    return function.callFunction(thisObject, EMPTY_ARRAY);
                }
            }

            class ArrayPrototypeJoin extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeJoin(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisValue,
                        ESValue[] arguments) throws EcmaScriptException {
                    StringBuilder buffer = new StringBuilder();
                    String separator = ",";
                    if (arguments.length > 0) {
                        separator = arguments[0].toString();
                    }
                    ESObject thisObject = thisValue.toESObject(getEvaluator());
                    int length = (thisObject.getProperty(
                            StandardProperty.LENGTHstring, StandardProperty.LENGTHhash))
                            .toInt32();
                    for (int i = 0; i < length; i++) {
                        if (i > 0)
                            buffer.append(separator);
                        String iString = Integer.toString(i);
                        ESValue value = thisObject.getProperty(iString, iString
                                .hashCode());
                        if (value != ESUndefined.theUndefined
                                && value != ESNull.theNull) {
                            buffer.append(value.toString());
                        }
                    }
                    return new ESString(buffer.toString());
                }
            }
            class ArrayPrototypeReverse extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeReverse(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).reverse();
                }
            }
            class ArrayPrototypeSort extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeSort(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    ESValue compareFn = null;
                    if (arguments.length > 0)
                        compareFn = arguments[0];
                    return ((ArrayPrototype) thisObject).sort(compareFn);
                }
            }
            class ArrayPrototypePop extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypePop(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).pop();
                }
            }
            class ArrayPrototypePush extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypePush(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).push(arguments);
                }
            }
            class ArrayPrototypeShift extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeShift(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).shift();
                }
            }
            class ArrayPrototypeUnshift extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeUnshift(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).unshift(arguments);
                }
            }
            class ArrayPrototypeSlice extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeSlice(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 2);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).slice(arguments);
                }
            }
            class ArrayPrototypeSplice extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeSplice(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 2);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).splice(arguments);
                }
            }
            class ArrayPrototypeIndexOf extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeIndexOf(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).indexOf(arguments);
                }
            }
            class ArrayPrototypeLastIndexOf extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeLastIndexOf(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).lastIndexOf(arguments);
                }
            }
            class ArrayPrototypeConcat extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeConcat(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).concat(arguments);
                }
            }
            class ArrayPrototypeReduce extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeReduce(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).reduce(arguments);
                }
            }
            class ArrayPrototypeReduceRight extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeReduceRight(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).reduceRight(arguments);
                }
            }
            class ArrayPrototypeEvery extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeEvery(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).every(arguments);
                }
            }
            class ArrayPrototypeSome extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeSome(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).some(arguments);
                }
            }
            class ArrayPrototypeForEach extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeForEach(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).forEach(arguments);
                }
            }
            class ArrayPrototypeMap extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeMap(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).map(arguments);
                }
            }
            class ArrayPrototypeFilter extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayPrototypeFilter(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayPrototype) thisObject).filter(arguments);
                }
            }
            arrayPrototype.putHiddenProperty("constructor", arrayObject);
            arrayPrototype.putHiddenProperty("toString",
                    new ArrayPrototypeToString("toString", evaluator,
                            functionPrototype));
            arrayPrototype.putHiddenProperty("join", new ArrayPrototypeJoin(
                    "join", evaluator, functionPrototype));
            arrayPrototype.putHiddenProperty("reverse",
                    new ArrayPrototypeReverse("reverse", evaluator,
                            functionPrototype));
            arrayPrototype.putHiddenProperty("sort", new ArrayPrototypeSort(
                    "sort", evaluator, functionPrototype));
            arrayPrototype.putHiddenProperty("pop", new ArrayPrototypePop(
                    "pop", evaluator, functionPrototype));
            arrayPrototype.putHiddenProperty("push", new ArrayPrototypePush(
                    "push", evaluator, functionPrototype));
            arrayPrototype.putHiddenProperty("shift", new ArrayPrototypeShift(
                    "shift", evaluator, functionPrototype));
            arrayPrototype.putHiddenProperty("unshift",
                    new ArrayPrototypeUnshift("unshift", evaluator,
                            functionPrototype));
            arrayPrototype.putHiddenProperty("slice", new ArrayPrototypeSlice(
                    "slice", evaluator, functionPrototype));
            arrayPrototype.putHiddenProperty("splice",
                    new ArrayPrototypeSplice("splice", evaluator,
                            functionPrototype));
            arrayPrototype.putHiddenProperty("indexOf",
                    new ArrayPrototypeIndexOf("indexOf", evaluator,
                            functionPrototype));
            arrayPrototype.putHiddenProperty("lastIndexOf",
                    new ArrayPrototypeLastIndexOf("lastIndexOf", evaluator,
                            functionPrototype));
            arrayPrototype.putHiddenProperty("concat",
                    new ArrayPrototypeConcat("concat", evaluator,
                            functionPrototype));
            arrayPrototype.putHiddenProperty("reduce",
                    new ArrayPrototypeReduce("reduce", evaluator,
                            functionPrototype));
            arrayPrototype.putHiddenProperty("reduceRight",
                    new ArrayPrototypeReduceRight("reduceRight", evaluator,
                            functionPrototype));
            arrayPrototype.putHiddenProperty("every", new ArrayPrototypeEvery(
                    "every", evaluator, functionPrototype));
            arrayPrototype.putHiddenProperty("some", new ArrayPrototypeSome(
                    "some", evaluator, functionPrototype));
            arrayPrototype.putHiddenProperty("forEach",
                    new ArrayPrototypeForEach("forEach", evaluator,
                            functionPrototype));
            arrayPrototype.putHiddenProperty("map", new ArrayPrototypeMap(
                    "map", evaluator, functionPrototype));
            arrayPrototype.putHiddenProperty("filter",
                    new ArrayPrototypeFilter("filter", evaluator,
                            functionPrototype));

            /*
             * ES5 15.4.3 - Properties of the Array Constructor
             */
            class ArrayIsArray extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                ArrayIsArray(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    return ((ArrayObject) thisObject).isArray(arguments);
                }
            }
            arrayObject.putHiddenProperty("prototype", arrayPrototype);
            arrayObject.putHiddenProperty("isArray", new ArrayIsArray(
                    "isArray", evaluator, functionPrototype));
        } catch (EcmaScriptException e) {
            e.printStackTrace();
            throw new ProgrammingError(e.getMessage());
        }

        evaluator.setArrayPrototype(arrayPrototype);

        return arrayObject;
    }
}
