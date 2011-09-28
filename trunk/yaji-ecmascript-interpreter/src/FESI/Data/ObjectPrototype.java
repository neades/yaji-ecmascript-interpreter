// ObjectPrototype.java
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

import java.io.IOException;

import org.yaji.json.JsonState;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

/**
 * Implements the prototype and is the class of all Object objects.
 * <P>
 * All functionality of objects is implemented in the superclass ESObject.
 */
public class ObjectPrototype extends ESObject {

    private static final long serialVersionUID = -4836187608032396557L;
    private static class ObjectPrototypeToString extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        ObjectPrototypeToString(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            String result;
            if (thisObject == ESUndefined.theUndefined) {
                result = "[object Undefined]";
            } else if (thisObject == ESNull.theNull) {
                result = "[object Null]";
            }
            ESObject esObject = thisObject.toESObject(getEvaluator());
            result = "[object " + esObject.getESClassName()
                    + "]";
            return new ESString(result);
        }
    }
    
    private static class ObjectPrototypeValueOf extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        ObjectPrototypeValueOf(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            return thisObject;
        }
    }

    private static class ObjectPrototypeToLocaleString extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        ObjectPrototypeToLocaleString(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            ESObject object = thisObject.toESObject(getEvaluator());
            ESValue toStringFunction = object.getProperty(StandardProperty.TOSTRINGstring,StandardProperty.TOSTRINGhash);
            return toStringFunction.callFunction(thisObject, ESValue.EMPTY_ARRAY);
        }
    }

    private static class ObjectPrototypeHasOwnProperty extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        ObjectPrototypeHasOwnProperty(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            String propertyName = getArg(arguments,0).toESString().toString();
            ESObject object = thisObject.toESObject(getEvaluator());
            return ESBoolean.valueOf(object.getOwnProperty(propertyName, propertyName.hashCode()) != null);
        }
    }

    private static class ObjectPrototypeIsPrototypeOf extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        public ObjectPrototypeIsPrototypeOf(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            boolean isPrototype = false;
            ESValue V = getArg(arguments,0);
            if (V instanceof ESObject) {
                ESObject v = (ESObject) V;
                ESObject o = thisObject.toESObject(getEvaluator());
                do {
                    v = v.getPrototype();
                    isPrototype = v == o;
                } while (v != null && !isPrototype);
            }
            return ESBoolean.valueOf(isPrototype);
        }
    }



    /**
     * Create a new Object with a specific prototype. This should be used by
     * routine implementing object with another prototype than Object. To create
     * an EcmaScript Object use ObjectObject.createObject()
     * 
     * @param prototype
     *            the prototype of the new object
     * @param evaluator
     *            The evaluator
     */
    public ObjectPrototype(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator);
    }
    
    @Override
    public boolean canJson() {
        return true;
    }

    @Override
    public void toJson(Appendable appendable, JsonState state, String parentPropertyName) throws IOException, EcmaScriptException {
        state.pushCyclicCheck(this);
        ESValue toJsonFunction = getPropertyIfAvailable(StandardProperty.TOJSONstring, StandardProperty.TOJSONhash);
        if (toJsonFunction != null && toJsonFunction instanceof FunctionPrototype) {
            ESValue value = toJsonFunction.callFunction(this, new ESValue[] { ESString.valueOf(parentPropertyName) });
            if (value instanceof ObjectPrototype) {
                ((ObjectPrototype)value).toJsonString(appendable, state);
            } else {
                value.toJson(appendable, state, parentPropertyName);
            }
        } else {
            toJsonString(appendable, state);
        }
        state.popCyclicCheck();
    }

    private void toJsonString(Appendable appendable, JsonState state) throws IOException, EcmaScriptException {
        appendable.append('{');
        if (!hasNoPropertyMap()) {
            getPropertyMap().toJson(appendable, state, this);
        }
        appendable.append('}');
    }

    public void initialise(ESValue objectObject, Evaluator evaluator, FunctionPrototype functionPrototype) throws EcmaScriptException {
        putHiddenProperty("constructor", objectObject);
        putHiddenProperty(StandardProperty.TOSTRINGstring,
                new ObjectPrototypeToString(StandardProperty.TOSTRINGstring, evaluator,
                        functionPrototype));
        putHiddenProperty(StandardProperty.VALUEOFstring,
                new ObjectPrototypeValueOf(StandardProperty.VALUEOFstring, evaluator,
                        functionPrototype));
        putHiddenProperty(StandardProperty.TOLOCALESTRINGstring,
                new ObjectPrototypeToLocaleString(StandardProperty.TOLOCALESTRINGstring, evaluator,
                        functionPrototype));
        putHiddenProperty(StandardProperty.HASOWNPROPERTYstring,
                new ObjectPrototypeHasOwnProperty(StandardProperty.HASOWNPROPERTYstring, evaluator,
                        functionPrototype));
        putHiddenProperty(StandardProperty.ISPROTOTYPEOFstring,
                new ObjectPrototypeIsPrototypeOf(StandardProperty.ISPROTOTYPEOFstring, evaluator,
                        functionPrototype));
    }
}