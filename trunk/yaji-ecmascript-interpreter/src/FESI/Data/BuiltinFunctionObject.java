// BuiltinFunctionObject.java
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
 * Implement the common functionality of all built-in functions
 */
public abstract class BuiltinFunctionObject extends FunctionPrototype {

    private static final long serialVersionUID = 8812972956505265898L;

    protected BuiltinFunctionObject(ESObject functionPrototype,
            Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
        super(functionPrototype, evaluator, functionName, length);
    }

    // overrides
    @Override
    public void putProperty(String propertyName, ESValue propertyValue, int hash)
            throws EcmaScriptException {
        if (!propertyName.equals(StandardProperty.PROTOTYPEstring)) {
            super.putProperty(propertyName, propertyValue, hash);
        } // Allowed via putHiddenProperty, used internally !
    }

    /**
     * get the string defining the function
     * 
     * @return a string indicating that the function is native
     */
    @Override
    public String getFunctionImplementationString() {
        return "{[native: " + this.getClass().getName() + "]}";
    }

    /**
     * Get the function parameter description as a string
     * 
     * @return the function parameter string as (a,b,c)
     */
    @Override
    public String getFunctionParametersString() {
        return "(<" + getLengthProperty() + " args>)";
    }

    // overrides
    @Override
    public String toString() {
        return "<" + this.getFunctionName() + ":" + this.getClass().getName()
                + ">";
    }

    protected ESValue getArg(ESValue[] arguments, int index) {
        return (index<arguments.length)?arguments[index]:ESUndefined.theUndefined;
    }

    protected ESValue getCoercibleArg(ESValue[] arguments, int index) throws TypeError {
        ESValue arg = getArg(arguments,index);
        if (!arg.isObjectCoercible()) {
            throw new TypeError(getFunctionName()+" expects argument "+(index+1)+" to be convertable to Object");
        }
        return arg;
    }

    protected ESObject getArgAsObject(ESValue[] arguments, int index) throws TypeError {
        ESValue object = getArg(arguments,index);
        if (object instanceof ESObject) {
            return (ESObject)object;
        }
        throw new TypeError(getFunctionName()+" expects argument "+(index+1)+" to be an Object");
    }

    protected void checkThisObjectCoercible(ESValue thisObject) throws TypeError {
        if (!thisObject.isObjectCoercible()) {
            throw new TypeError(getFunctionName()+" cannot be applied to null or undefined");
        }
    }

    protected int getArgAsInt32(ESValue[] arguments, int i) throws EcmaScriptException {
        ESValue value = getArg(arguments, i);
        return value.toInt32();
    }

    protected double getArgAsInteger(ESValue[] arguments, int i) throws EcmaScriptException {
        ESValue value = getArg(arguments, i);
        return value.toInteger();
    }

}