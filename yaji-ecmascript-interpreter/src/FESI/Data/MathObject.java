// MathObject.java
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

import java.util.Arrays;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.ProgrammingError;
import FESI.Interpreter.Evaluator;

/**
 * Implemements the EcmaScript Math singleton.
 */
public class MathObject extends ObjectPrototype {
    private static final long serialVersionUID = 3759859709073220773L;

    private MathObject(ESObject prototype, Evaluator evaluator,
            FunctionPrototype functionPrototype) throws EcmaScriptException {
        super(prototype, evaluator);

        // Initialization used to be in makeMathObject, but this caused
        // some problemsto the users of JBuilder. So it is moved in
        // the constructor

        putHiddenProperty("E", ESNumber.valueOf(Math.E));
        putHiddenProperty("LN10", ESNumber.valueOf(Math.log(10.0d)));
        putHiddenProperty("LN2", ESNumber.valueOf(Math.log(2.0d)));
        putHiddenProperty("LOG2E", ESNumber.valueOf(1.0d / Math.log(2.0d)));
        putHiddenProperty("LOG10E", ESNumber.valueOf(1.0d / Math.log(10.0d)));
        putHiddenProperty("PI", ESNumber.valueOf(Math.PI));
        putHiddenProperty("SQRT1_2", ESNumber.valueOf(1.0d / Math.sqrt(2.0d)));
        putHiddenProperty("SQRT2", ESNumber.valueOf(Math.sqrt(2.0d)));

        putHiddenProperty("abs", new BuiltinMathFunctionOne("abs", evaluator,
                functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double arg) {
                return Math.abs(arg);
            }
        });
        putHiddenProperty("acos", new BuiltinMathFunctionOne("acos", evaluator,
                functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double arg) {
                return Math.acos(arg);
            }
        });
        putHiddenProperty("asin", new BuiltinMathFunctionOne("asin", evaluator,
                functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double arg) {
                return Math.asin(arg);
            }
        });
        putHiddenProperty("atan", new BuiltinMathFunctionOne("atan", evaluator,
                functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double arg) {
                return Math.atan(arg);
            }
        });
        putHiddenProperty("atan2", new BuiltinMathFunctionTwo("atan2",
                evaluator, functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double arg1, double arg2) {
                return Math.atan2(arg1, arg2);
            }
        });
        putHiddenProperty("ceil", new BuiltinMathFunctionOne("ceil", evaluator,
                functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double arg) {
                return Math.ceil(arg);
            }
        });
        putHiddenProperty("cos", new BuiltinMathFunctionOne("cos", evaluator,
                functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double arg) {
                return Math.cos(arg);
            }
        });
        putHiddenProperty("exp", new BuiltinMathFunctionOne("exp", evaluator,
                functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double arg) {
                return Math.exp(arg);
            }
        });
        putHiddenProperty("floor", new BuiltinMathFunctionOne("floor",
                evaluator, functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double arg) {
                return Math.floor(arg);
            }
        });
        putHiddenProperty("log", new BuiltinMathFunctionOne("log", evaluator,
                functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double arg) {
                return Math.log(arg);
            }
        });
        putHiddenProperty("max", new BuiltinMathFunctionN("max", evaluator,
                functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double[] arguments) {
                if (arguments.length == 0) {
                    return Double.NEGATIVE_INFINITY;
                }
                Arrays.sort(arguments);
                return arguments[arguments.length-1];
            }
        });
        putHiddenProperty("min", new BuiltinMathFunctionN("min", evaluator,
                functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double[] arguments) {
                if (arguments.length == 0) {
                    return Double.POSITIVE_INFINITY;
                }
                Arrays.sort(arguments);
                return arguments[0];
            }
        });
        putHiddenProperty("pow", new BuiltinMathFunctionTwo("pow", evaluator,
                functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double arg1, double arg2) {
                double d = Double.NaN;
                try {
                    d = Math.pow(arg1, arg2);
                } catch (ArithmeticException e) {
                    // return NaN
                }
                return d;
            }
        });
        putHiddenProperty("random", new BuiltinMathFunctionZero("random",
                evaluator, functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction() {
                return Math.random();
            }
        });
        putHiddenProperty("round", new BuiltinMathFunctionOne("round",
                evaluator, functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double arg) {
                return Math.round(arg);
            }
        });
        putHiddenProperty("sin", new BuiltinMathFunctionOne("sin", evaluator,
                functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double arg) {
                return Math.sin(arg);
            }
        });
        putHiddenProperty("sqrt", new BuiltinMathFunctionOne("sqrt", evaluator,
                functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double arg) {
                return Math.sqrt(arg);
            }
        });
        putHiddenProperty("tan", new BuiltinMathFunctionOne("tan", evaluator,
                functionPrototype) {
            private static final long serialVersionUID = 1L;

            @Override
            public double applyMathFunction(double arg) {
                return Math.tan(arg);
            }
        });
    }

    // overrides
    @Override
    public String getESClassName() {
        return "Math";
    }

    // class of nilary functions
    abstract class BuiltinMathFunctionZero extends BuiltinFunctionObject {
        private static final long serialVersionUID = -403656694341333710L;

        BuiltinMathFunctionZero(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 0);
        }

        abstract double applyMathFunction();

        @Override
        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            return ESNumber.valueOf(applyMathFunction());
        }
    }

    // class of unary functions
    abstract class BuiltinMathFunctionOne extends BuiltinFunctionObject {
        private static final long serialVersionUID = -4281492306473644175L;

        BuiltinMathFunctionOne(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
        }

        abstract double applyMathFunction(double arg);

        @Override
        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            double arg = (arguments.length > 0) ? arguments[0].doubleValue()
                    : Double.NaN;
            if (Double.isNaN(arg)) {
                return ESNumber.valueOf(Double.NaN);
            }
            return ESNumber.valueOf(applyMathFunction(arg));
        }
    }

    // class of dyadic functions
    abstract class BuiltinMathFunctionTwo extends BuiltinFunctionObject {
        private static final long serialVersionUID = 978692357189557419L;

        BuiltinMathFunctionTwo(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 2);
        }

        abstract double applyMathFunction(double arg1, double arg2);

        @Override
        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            if (arguments.length < 2) {
                throw new EcmaScriptException("Missing parameter in function "
                        + this);
            }
            double arg1 = (arguments.length > 0) ? arguments[0].doubleValue()
                    : Double.NaN;
            double arg2 = (arguments.length > 1) ? arguments[1].doubleValue()
                    : Double.NaN;
            if (Double.isNaN(arg1) || Double.isNaN(arg2)) {
                return ESNumber.valueOf(Double.NaN);
            }
            return ESNumber.valueOf(applyMathFunction(arg1, arg2));
        }
    }

    // class of n-ary functions
    abstract class BuiltinMathFunctionN extends BuiltinFunctionObject {
        private static final long serialVersionUID = 978692357189557419L;

        BuiltinMathFunctionN(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, (int)Double.POSITIVE_INFINITY);
        }

        abstract double applyMathFunction(double[] arguments);

        @Override
        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            double[] doubles = new double[arguments.length];
            for (int i = 0; i < arguments.length; i++){
                if (Double.isNaN(arguments[i].doubleValue())){
                    return ESNumber.valueOf(Double.NaN);
                }
                doubles[i] = arguments[i].doubleValue();
            }
            return ESNumber.valueOf(applyMathFunction(doubles));
        }
    }

    /**
     * Utility function to create the Math single object
     * 
     * @param evaluator
     *            the Evaluator
     * @param prototype
     *            The Object prototype attached to the evaluator
     * @param functionPrototype
     *            The Function prototype attached to the evaluator
     * 
     * @return the Math singleton
     */
    static public ESObject makeMathObject(Evaluator evaluator,
            ObjectPrototype prototype, FunctionPrototype functionPrototype) {
        try {
            MathObject mo = new MathObject(prototype, evaluator,
                    functionPrototype);
            return mo;
        } catch (EcmaScriptException e) {
            e.printStackTrace();
            throw new ProgrammingError(e.getMessage());
        }
    }
}
