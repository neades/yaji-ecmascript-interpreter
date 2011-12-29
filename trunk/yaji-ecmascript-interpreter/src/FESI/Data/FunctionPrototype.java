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

/**
 * Implements the prototype and is the class of all Function objects
 */
public class FunctionPrototype extends ESObject {
    private static final long serialVersionUID = 5228262867569754052L;
    private String functionName = null;

    FunctionPrototype(ESObject prototype, Evaluator evaluator,
            String functionName, int length) throws EcmaScriptException {
        super(prototype, evaluator);
        this.functionName = functionName;
        putProperty(StandardProperty.LENGTHstring, 0, ESNumber.valueOf(length));
    }

    FunctionPrototype(ESObject prototype, Evaluator evaluator, int length) throws EcmaScriptException {
        super(prototype, evaluator);
        putProperty(StandardProperty.LENGTHstring, 0, ESNumber.valueOf(length));
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
        try {
            return getProperty(StandardProperty.LENGTHstring, StandardProperty.LENGTHhash).toInt32();
        } catch ( EcmaScriptException e ) {
            return 0;
        }
    }

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
    
    @Override
    public boolean isCallable() {
        return true;
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
        ESValue prototype = getProperty(StandardProperty.PROTOTYPEstring, StandardProperty.PROTOTYPEhash);
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
