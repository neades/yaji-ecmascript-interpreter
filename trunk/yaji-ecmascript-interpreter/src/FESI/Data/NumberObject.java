// NumberObject.java
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
import FESI.Exceptions.RangeError;
import FESI.Interpreter.Evaluator;

/**
 * Implemements the EcmaScript Number singleton.
 */
public class NumberObject extends BuiltinFunctionObject {
    private static final long serialVersionUID = -4773796736269945208L;

    private NumberObject(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator, "Number", 1);
    }

    // overrides
    public String toString() {
        return "<Number>";
    }

    // overrides
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        if (arguments.length == 0) {
            return ESNumber.valueOf(0.0);
        }
        return ESNumber.valueOf(arguments[0].doubleValue());

    }

    // overrides
    public ESObject doConstruct(ESValue[] arguments)
            throws EcmaScriptException {
        NumberPrototype theObject = null;
        ESObject np = getEvaluator().getNumberPrototype();
        theObject = new NumberPrototype(np, getEvaluator());
        if (arguments.length > 0) {
            theObject.value = ESNumber.valueOf(arguments[0].doubleValue());
        } else {
            theObject.value = ESNumber.valueOf(0.0);
        }
        return theObject;
    }


    // For numberPrototype
    private static abstract class NumberPrototypeFunctionObject extends BuiltinFunctionObject {
        private static final long serialVersionUID = -1279666619140046881L;

        public NumberPrototypeFunctionObject(FunctionPrototype fp, Evaluator evaluator, String name, int length) {
            super(fp,evaluator,name,length);
        }
        
        @Override
        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            return invoke((NumberPrototype)thisObject, arguments);
        }

        protected abstract ESValue invoke(NumberPrototype thisObject, ESValue[] arguments) throws EcmaScriptException;
    }
    private static class NumberPrototypeToString extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        NumberPrototypeToString(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            ESValue v = ((NumberPrototype) thisObject).value;
            String s = v.toString();
            if (arguments.length > 0) {
                double d = arguments[0].doubleValue();
                if (!Double.isNaN(d)) {
                    s = Long
                            .toString(((long) v.doubleValue()), (int) d);
                }
            }
            return new ESString(s);
        }
    }
    
    private static class NumberPrototypeValueOf extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        NumberPrototypeValueOf(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            return ((NumberPrototype) thisObject).value;
        }
    }
    
    private static class NumberPrototypeToPrecision extends NumberPrototypeFunctionObject {
        private static final long serialVersionUID = 1L;

        NumberPrototypeToPrecision(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue invoke(NumberPrototype thisNumber,
                ESValue[] arguments) throws EcmaScriptException {
            Double d = thisNumber.doubleValue();
            ESValue precision = getArg(arguments,0);
            String s;
            if (d.isNaN() || d.isInfinite() || precision.getTypeOf() == EStypeUndefined) {
                s = thisNumber.toString();
            } else {
                int p = precision.toInt32();
                if (p<1 || p > 21) {
                    throw new RangeError("precision should be in range 1-21");
                }
                s = thisNumber.toString(p);
            }
            return new ESString(s);
        }
    }
    
    /**
     * Utility function to create the single Number object
     * 
     * @param evaluator
     *            the Evaluator
     * @param objectPrototype
     *            The Object prototype attached to the evaluator
     * @param functionPrototype
     *            The Function prototype attached to the evaluator
     * 
     * @return the Number singleton
     */
    public static NumberObject makeNumberObject(Evaluator evaluator,
            ObjectPrototype objectPrototype, FunctionPrototype functionPrototype) {

        NumberPrototype numberPrototype = new NumberPrototype(objectPrototype,
                evaluator);
        NumberObject numberObject = new NumberObject(functionPrototype,
                evaluator);

        try {
            

            numberObject.putHiddenProperty("prototype", numberPrototype);
            numberObject.putHiddenProperty("length", ESNumber.valueOf(1));
            numberObject.putHiddenProperty("MAX_VALUE", ESNumber
                    .valueOf(Double.MAX_VALUE));
            numberObject.putHiddenProperty("MIN_VALUE", ESNumber
                    .valueOf(Double.MIN_VALUE));
            numberObject.putHiddenProperty("NaN", ESNumber.valueOf(Double.NaN));
            numberObject.putHiddenProperty("NEGATIVE_INFINITY", ESNumber
                    .valueOf(Double.NEGATIVE_INFINITY));
            numberObject.putHiddenProperty("POSITIVE_INFINITY", ESNumber
                    .valueOf(Double.POSITIVE_INFINITY));

            numberPrototype.putHiddenProperty("constructor", numberObject);
            numberPrototype.putHiddenProperty("toString",
                    new NumberPrototypeToString("toString", evaluator,
                            functionPrototype));
            numberPrototype.putHiddenProperty("valueOf",
                    new NumberPrototypeValueOf("valueOf", evaluator,
                            functionPrototype));
            numberPrototype.putHiddenProperty("toPrecision",
                    new NumberPrototypeToPrecision("toPrecision", evaluator,
                            functionPrototype));
        } catch (EcmaScriptException e) {
            e.printStackTrace();
            throw new ProgrammingError(e.getMessage());
        }

        evaluator.setNumberPrototype(numberPrototype);

        return numberObject;
    }
}
