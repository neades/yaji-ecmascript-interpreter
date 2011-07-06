// ObjectObject.java
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
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;

/**
 * Implemements the EcmaScript Object singleton.
 */
public class ObjectObject extends BuiltinFunctionObject {
    private static final long serialVersionUID = -1936792376718129590L;

    public ObjectObject(ESObject prototype, Evaluator evaluator)
            throws EcmaScriptException {
        super(prototype, evaluator, "Object", 1);
        putHiddenProperty("isFrozen", new BuiltinFunctionObject(prototype,
                evaluator, "isFrozen", 1) {

            private static final long serialVersionUID = 1L;

            public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                if (arguments.length == 0
                        || !(arguments[0] instanceof ESObject)) {
                    throw new TypeError("Object.isFrozen() should be passed an Object");
                }
                return ESBoolean.valueOf(((ESObject) arguments[0])
                        .isFrozen());
            }
        });
        putHiddenProperty("freeze", new BuiltinFunctionObject(prototype,
                evaluator, "freeze", 1) {

            private static final long serialVersionUID = 1L;

            public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                if (arguments.length == 0
                        || !(arguments[0] instanceof ESObject)) {
                    throw new TypeError("Object.freeze() should be passed an Object");
                }
                ((ESObject) arguments[0]).freeze();
                return arguments[0];
            }
        });
    }

    // overrides
    public String toString() {
        return "<Object>";
    }

    /**
     * Create an EcmaScript Object for a specified evaluator
     * 
     * @param evaluator
     *            the Evaluator
     * @return the new object
     */
    static public ObjectPrototype createObject(Evaluator evaluator) {
        ESObject op = evaluator.getObjectPrototype();
        return new ObjectPrototype(op, evaluator);
    }

    // overrides
    public ESObject doConstruct(ESObject thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        ESValue theValue;
        if (arguments.length == 0) {
            theValue = createObject(getEvaluator());
        } else {
            if (arguments[0] == ESNull.theNull
                    || arguments[0] == ESUndefined.theUndefined) {
                theValue = createObject(getEvaluator());
            } else {
                theValue = arguments[0].toESObject(getEvaluator());
            }
        }
        return (ESObject) theValue;
    }

    // overrides
    public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        ESValue theValue;
        if (arguments.length == 0) {
            theValue = createObject(getEvaluator());
        } else {
            if (arguments[0] == ESNull.theNull
                    || arguments[0] == ESUndefined.theUndefined) {
                theValue = createObject(getEvaluator());
            } else {
                theValue = arguments[0].toESObject(getEvaluator());
            }
        }
        return theValue;
    }

}
