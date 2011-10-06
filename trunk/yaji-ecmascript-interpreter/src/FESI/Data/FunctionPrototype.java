// FunctionPrototype.java
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
import FESI.Interpreter.ScopeChain;

/**
 * Implements the prototype and is the class of all Function objects
 */
public class FunctionPrototype extends ESObject {
    private static final long serialVersionUID = 5228262867569754052L;
    private String functionName = null;
    private int length = 0;

    FunctionPrototype(ESObject prototype, Evaluator evaluator,
            String functionName, int length) {
        super(prototype, evaluator);
        this.functionName = functionName;
        this.length = length;
    }

    FunctionPrototype(ESObject prototype, Evaluator evaluator, int length) {
        super(prototype, evaluator);
        this.length = length;
    }

    // overrides
    @Override
    public String getESClassName() {
        return "Function";
    }

    public String getFunctionName() {
        if (functionName == null) {
            return "anonymous";
        }
        return functionName;
    }

    /**
     * get the string defining the function
     * 
     * @return a String indicating that this is the function prototype
     */
    public String getFunctionImplementationString() {
        return "{<FunctionPrototype (" + this.getClass().getName() + ")>}";
    }

    /**
     * get the string defining the function
     * 
     * @return a string indicating that the function prototype has no argument
     */
    public String getFunctionParametersString() {
        return "()";
    }

    /**
     * Get the number of arguments property
     */
    public int getLengthProperty() {
        return length;
    }

    // overrides
    @Override
    public ESValue getPropertyInScope(String propertyName,
            ScopeChain previousScope, int hash) throws EcmaScriptException {
        if (hash == StandardProperty.LENGTHhash && propertyName.equals(StandardProperty.LENGTHstring)) {
            return ESNumber.valueOf(length);
        }
        return super.getPropertyInScope(propertyName, previousScope, hash);
    }

    @Override
    public ESValue getPropertyIfAvailable(String propertyName, int hash)
            throws EcmaScriptException {
        if (hash == StandardProperty.LENGTHhash && propertyName.equals(StandardProperty.LENGTHstring)) {
            return ESNumber.valueOf(length);
        }
        return super.getPropertyIfAvailable(propertyName, hash);
    }

    // overrides
    @Override
    public void putProperty(String propertyName, ESValue propertyValue, int hash)
            throws EcmaScriptException {
        if (!(hash == StandardProperty.LENGTHhash && propertyName.equals(StandardProperty.LENGTHstring))) {
            super.putProperty(propertyName, propertyValue, hash);
        } // Allowed via putHiddenProperty, used internally !
    }

    // overrides
    @Override
    public String[] getSpecialPropertyNames() {
        String[] ns = { StandardProperty.LENGTHstring };
        return ns;
    }

    // overrides
    @Override
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        return ESUndefined.theUndefined;
    }

    // overrides
    @Override
    public String getTypeofString() {
        return "function";
    }

    // overrides
    @Override
    public String toString() {
        return "<" + getESClassName() + ":" + this.getFunctionName() + ">";
    }
    
    @Override
    public boolean hasInstance(ESValue v1) throws EcmaScriptException {
        if (! (v1 instanceof ESObject)) {
            return false;
        }
        ESObject v = (ESObject)v1;
        ESValue prototype = getProperty("prototype", "prototype".hashCode());
        if (! (prototype instanceof ESObject)) {
            throw new TypeError("prototype of Function expected to be an Object");
        }
        while (v != null) {
            v = v.getPrototype();
            if (v == prototype) {
                return true;
            }
        } 
        return false;
    }
}
