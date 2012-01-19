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

import java.util.Enumeration;

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
        
        ObjectPrototype p = new ObjectPrototype(null, evaluator);
        putProperty(StandardProperty.PROTOTYPEstring, 0, p);
        
        putHiddenProperty("isFrozen", new BuiltinFunctionObject(prototype,
                evaluator, "isFrozen", 1) {

            private static final long serialVersionUID = 1L;

            @Override
            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
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

            @Override
            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                if (arguments.length == 0
                        || !(arguments[0] instanceof ESObject)) {
                    throw new TypeError("Object.freeze() should be passed an Object");
                }
                ((ESObject) arguments[0]).freeze();
                return arguments[0];
            }
        });
        putHiddenProperty("getPrototypeOf", new BuiltinFunctionObject(prototype, evaluator, "getPrototypeOf", 1) {
            private static final long serialVersionUID = 2995052280653091683L;
            
            @Override
            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                ESObject objectToInterrogate = getArgAsObject(arguments, 0);
                ESObject prototype = objectToInterrogate.getPrototype();
                return prototype == null ? ESNull.theNull : prototype;
            }
        });
        putHiddenProperty("getOwnPropertyDescriptor", new BuiltinFunctionObject(prototype,evaluator, "getOwnPropertyDescriptor", 2) {
            private static final long serialVersionUID = 1L;

            @Override
            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                ESObject object = getArgAsObject(arguments,0);
                String propertyName = getArg(arguments,1).callToString();
                return object.getOwnPropertyDescriptor(propertyName);
            }
        });
        putHiddenProperty("defineProperty", new BuiltinFunctionObject(prototype,evaluator, "defineProperty", 3) {
            private static final long serialVersionUID = 1L;

            @Override
            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                ESObject object = getArgAsObject(arguments,0);
                String propertyName = getArg(arguments,1).callToString();
                ESObject desc = getArgAsObject(arguments, 2);
                return object.defineProperty(propertyName,desc);
            }
        });
        putHiddenProperty("defineProperties", new BuiltinFunctionObject(prototype,evaluator, "defineProperties", 2) {
            private static final long serialVersionUID = 1L;

            @Override
            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                ESObject object = getArgAsObject(arguments,0);
                ESValue p = getArg(arguments, 1);
                return defineProperties(object, p.toESObject(getEvaluator()));
            }
        });
        putHiddenProperty("getOwnPropertyNames", new BuiltinFunctionObject(prototype, evaluator, "getOwnPropertyNames", 1) {
            private static final long serialVersionUID = 7813753201649697905L;
            
            @Override
            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                ESObject object = getArgAsObject(arguments,0);
                ESObject array = getEvaluator().createArray();
                Enumeration<String> propertyNames = object.getOwnPropertyNames();
                for (long i=0L; propertyNames.hasMoreElements(); i++) {
                    String propertyName = propertyNames.nextElement();
                    array.putProperty(i,new ESString(propertyName));
                }
                return array;
            }
        });
        putHiddenProperty("create", new BuiltinFunctionObject(prototype, evaluator, "create", 2) {
            private static final long serialVersionUID = 7813753201649697905L;
            
            @Override
            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                ESValue arg0 = getArg(arguments,0);
                final Evaluator evaluator = getEvaluator();
                ESObject op = null;
                if (arg0 instanceof ESObject) {
                    op = (ESObject)arg0;
                } else if (arg0.getTypeOf() != EStypeNull) {
                    throw new TypeError("ObjectCreate must be supplied a prototype object as first parameter");
                }
                final ObjectPrototype objectPrototype = new ObjectPrototype(op, evaluator);
                ESValue p = getArg(arguments,1);
                if (p.getTypeOf() != EStypeUndefined) {
                    defineProperties(objectPrototype, p.toESObject(evaluator));
                }
                return objectPrototype;
            }
        });
        putHiddenProperty("seal", new BuiltinFunctionObject(prototype, evaluator, "seal", 1) {
            private static final long serialVersionUID = 7813753201649697905L;
            
            @Override
            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                ESObject object = getArgAsObject(arguments,0);
                object.seal();
                return object;
            }
        });
        putHiddenProperty("isExtensible", new BuiltinFunctionObject(prototype, evaluator, "isExtensible", 1) {
            private static final long serialVersionUID = 7813753201649697905L;
            
            @Override
            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                ESObject object = getArgAsObject(arguments,0);
                return ESBoolean.valueOf(object.isExtensible());
            }
        });
        putHiddenProperty("isSealed", new BuiltinFunctionObject(prototype, evaluator, "isSealed", 1) {
            private static final long serialVersionUID = 7813753201649697905L;
            
            @Override
            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                ESObject object = getArgAsObject(arguments,0);
                return ESBoolean.valueOf(object.isSealed());
            }
        });
        putHiddenProperty("preventExtensions", new BuiltinFunctionObject(prototype, evaluator, "preventExtensions", 1) {
            private static final long serialVersionUID = 7813753201649697905L;
            
            @Override
            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                ESObject object = getArgAsObject(arguments,0);
                object.setExtensible(false);
                return object;
            }
        });
        putHiddenProperty("keys", new BuiltinFunctionObject(prototype, evaluator, "keys", 1) {
            private static final long serialVersionUID = 7813753201649697905L;
            
            @Override
            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                ESObject object = getArgAsObject(arguments,0);
                Enumeration<String> propertyNames = object.keys();
                ESObject array = getEvaluator().createArray();
                long index = 0;
                while (propertyNames.hasMoreElements()) {
                    String propertyName = propertyNames.nextElement();
                    array.putProperty(index++,new ESString(propertyName));
                }
                return array;
            }
        });
    }

    // overrides
    @Override
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
    @Override
    public ESObject doConstruct(ESValue[] arguments)
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
    @Override
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
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

    private ESValue defineProperties(ESObject object, ESObject p)
            throws EcmaScriptException {
        Enumeration<String> ownPropertyNames = p.keys();
        while (ownPropertyNames.hasMoreElements()) {
            String propertyName = ownPropertyNames.nextElement();
            ESValue desc = p.getProperty(propertyName);
            if (!(desc instanceof ESObject)) {
                throw new TypeError("Object.defineProperties: property "+propertyName+" must have an object as its descriptor");
            }
            object.defineProperty(propertyName,desc.toESObject(getEvaluator()));
        }
        return object;
    }

}
