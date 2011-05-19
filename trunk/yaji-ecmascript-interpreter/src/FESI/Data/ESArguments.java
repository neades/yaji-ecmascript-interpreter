// ESArguments.java
// FESI Copyright (c) Jean-Marc Lugrin, 1999
// YAJI Copyright (c) Graham Technology, 2002
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
import FESI.Interpreter.ScopeChain;

/**
 * Implements the "arguments" object for function call
 */
public class ESArguments extends ESObject {
    private static final long serialVersionUID = -5368828714964961343L;
    private final ESObject callee; // Called object
    private final int length; // Number of arguments
    protected String[] argumentNames; // Argument names from 0 to n

    // (not readily available) int [] hashCodes; // Argument hash codes from 0
    // to n

    /**
     * Create a new arguments object - only called by makeNewESArgument
     * 
     * @param evaluator
     *            The evaluator
     * @param argumentNames
     *            The array of argument names
     * @param length
     *            The number of arguments (max of names and values
     * @param callee
     *            The called object
     */
    protected ESArguments(Evaluator evaluator, String[] argumentNames,
            int length, ESObject callee) {
        super(evaluator.getObjectPrototype(), evaluator, (length < 3) ? 5 : 11); // limit
                                                                                 // space
                                                                                 // requirements
        this.argumentNames = argumentNames;
        this.length = length;
        this.callee = callee;
    }

    // overrides
    @Override
    public ESValue getPropertyInScope(String propertyName,
            ScopeChain previousScope, int hash) throws EcmaScriptException {
        if (propertyName.equals("callee")) {
            return callee;
        } else if (propertyName.equals("arguments")) {
            return this;
        } else if (propertyName.equals("length")) {
            return ESNumber.valueOf(length);
        }
        // directly test on get
        if (super.hasProperty(propertyName, hash)) {
            return super.getProperty(propertyName, hash);
        }
        int index = -1; // indicates not a valid index value
        try {
            char c = propertyName.charAt(0);
            if ('0' <= c && c <= '9') {
                index = Integer.parseInt(propertyName);
            }
        } catch (NumberFormatException e) {
            // do nothing
        } catch (StringIndexOutOfBoundsException e) { // for charAt
            // do nothing
        }
        if (index >= 0 && index < argumentNames.length) {
            propertyName = argumentNames[index];
            hash = propertyName.hashCode();
            return super.getProperty(propertyName, hash); // will be defined
        }
        if (previousScope == null) {
            throw new EcmaScriptException("global variable '" + propertyName
                    + "' does not have a value");
        }
        return previousScope.getValue(propertyName, hash);

    }

    // overrides
    @Override
    public ESValue doIndirectCallInScope(Evaluator evaluator,
            ScopeChain previousScope, ESObject thisObject, String functionName,
            int hash, ESValue[] arguments) throws EcmaScriptException {
        if (functionName.equals("callee")) {
            return callee.callFunction(thisObject, arguments);
        }
        return super.doIndirectCallInScope(evaluator, previousScope,
                thisObject, functionName, hash, arguments);

    }

    // overrides
    @Override
    public ESValue getProperty(String propertyName, int hash)
            throws EcmaScriptException {

        if (propertyName.equals("callee")) {
            return callee;
        } else if (propertyName.equals("arguments")) {
            return this;
        } else if (propertyName.equals("length")) {
            return ESNumber.valueOf(length);
        } else {
            // Assume that it is more likely a name than a number
            if (super.hasProperty(propertyName, hash)) {
                return super.getProperty(propertyName, hash);
            }
            int index = -1; // indicates not a valid index value
            try {
                char c = propertyName.charAt(0);
                if ('0' <= c && c <= '9') {
                    index = Integer.parseInt(propertyName);
                }
            } catch (NumberFormatException e) {
                // do nothing
            }
            if (index >= 0 && index < argumentNames.length) {
                propertyName = argumentNames[index];
                hash = propertyName.hashCode();
            }
            return super.getProperty(propertyName, hash);
        }
    }

    // overrides
    @Override
    public ESValue getProperty(int index) throws EcmaScriptException {
        if (index >= 0 && index < argumentNames.length) {
            String propertyName = argumentNames[index];
            return super.getProperty(propertyName, propertyName.hashCode());
        }
        String iString = Integer.toString(index);
        return getProperty(iString, iString.hashCode());

    }

    // overrides
    @Override
    public boolean hasProperty(String propertyName, int hash)
            throws EcmaScriptException {
        if (propertyName.equals("callee")) {
            return true;
        } else if (propertyName.equals("arguments")) {
            return true;
        } else if (propertyName.equals("length")) {
            return true;
        } else if (super.hasProperty(propertyName, hash)) {
            return true;
        } else {
            int index = -1; // indicates not a valid index value
            try {
                char c = propertyName.charAt(0);
                if ('0' <= c && c <= '9') {
                    index = Integer.parseInt(propertyName);
                }
            } catch (NumberFormatException e) {
                // do nothing
            }
            if (index >= 0 && index < argumentNames.length) {
                return true;
            }
            return false;
        }
    }

    /**
     * Make a new ESArgument from names and values - the number of names and
     * values do not have to be identical.
     * 
     * @param evaluator
     *            theEvaluator
     * @param callee
     *            the called function
     * @param argumentNames
     *            the names of the arguments of the function
     * @param argumentValues
     *            the values of the argument.
     * @return the new ESArguments
     */
    public static ESArguments makeNewESArguments(Evaluator evaluator,
            ESObject callee, String[] argumentNames, ESValue[] argumentValues) {
        /**
         * Advanced FESI GT Modified: 17/5/2005 Allow setting of number of
         * parameters passed
         **/
        return makeNewESArguments(evaluator, callee, argumentNames,
                argumentValues, argumentValues.length);
    }

    /**
     * Make a new ESArgument from names and values - the number of names and
     * values do not have to be identical.
     * 
     * @param evaluator
     *            theEvaluator
     * @param callee
     *            the called function
     * @param argumentNames
     *            the names of the arguments of the function
     * @param argumentValues
     *            the values of the argument.
     * @param noArgumentsPassed
     *            The number of arguments passed
     * @return the new ESArguments
     */
    public static ESArguments makeNewESArguments(Evaluator evaluator,
            ESObject callee, String[] argumentNames, ESValue[] argumentValues,
            int noArgumentsPassed) {

        ESArguments args = new ESArguments(evaluator, argumentNames,
                noArgumentsPassed, callee);
        args.setArguments(argumentNames, argumentValues);

        return args;
    }

    protected void setArguments(String[] argumentNames, ESValue[] argumentValues)
            throws ProgrammingError {
        try {
            // keep looking until we run out of either variable names or values,
            // which ever happens 2nd
            int noIterations = Math.max(argumentValues.length,
                    argumentNames.length);
            for (int i = 0; i < noIterations; i++) {

                // if we ran out of values first, use undefined
                ESValue val = i < argumentValues.length ? argumentValues[i]
                        : ESUndefined.theUndefined;

                // if we ran out of names first, start numbering them (ie
                // myFunction.1 etc)
                if (i < argumentNames.length) {
                    super.putProperty(argumentNames[i], val, argumentNames[i]
                            .hashCode());
                } else {
                    String iString = Integer.toString(i);
                    super.putProperty(iString, val, iString.hashCode()); // arguments
                                                                         // after
                                                                         // name
                                                                         // use
                                                                         // index
                                                                         // as
                                                                         // name
                }
            }
        } catch (EcmaScriptException e) {
            e.printStackTrace();
            throw new ProgrammingError(e.getMessage());
        }
    }

    // overrides
    @Override
    public boolean deleteProperty(String propertyName, int hash)
            throws EcmaScriptException {
        return !hasProperty(propertyName, hash); // none can be deleted...
    }

    // overrides
    @Override
    public ESValue getDefaultValue(int hint) throws EcmaScriptException {
        return callee.getDefaultValue(hint);
    }

    // overrides
    @Override
    public int getTypeOf() {
        return callee.getTypeOf();
    }

    // overrides
    @Override
    public Object toJavaObject() {
        return callee.toJavaObject();
    }

    // overrides
    @Override
    public String getTypeofString() {
        return callee.getTypeofString();
    }

    // overrides
    @Override
    public String toString() {
        if (callee == null) {
            return super.toString();
        }
        return callee.toString();
    }

    // overrides
    @Override
    public String toDetailString() {
        return callee.toDetailString();
    }

    // overrides
    @Override
    public String[] getSpecialPropertyNames() {
        String[] ns = { "arguments", "callee", "length" };
        return ns;
    }

}
