// StringPrototype.java
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
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.ScopeChain;

class StringPrototype extends ESObject {
    private static final long serialVersionUID = 1434073915774458544L;

    ESString value = new ESString("");

    StringPrototype(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator);
    }

    public String getESClassName() {
        return "String";
    }

    public String toString() {
        if (value == null)
            return super.toString();
        return value.toString();
    }

    public ESValue toESString() {
        return value;
    }

    public boolean booleanValue() throws EcmaScriptException {
        return value.booleanValue();
    }

    public double doubleValue() throws EcmaScriptException {
        return value.doubleValue();
    }

    public ESValue getPropertyInScope(String propertyName,
            ScopeChain previousScope, int hash) throws EcmaScriptException {
        if (hash == StandardProperty.LENGTHhash && propertyName.equals(StandardProperty.LENGTHstring)) {
            return ESNumber.valueOf(value.getStringLength());
        }
        return super.getPropertyInScope(propertyName, previousScope, hash);
    }

    @Override
    public ESValue getPropertyIfAvailable(String propertyName, int hash)
            throws EcmaScriptException {
        if (hash == StandardProperty.LENGTHhash && propertyName.equals(StandardProperty.LENGTHstring)) {
            return ESNumber.valueOf(value.getStringLength());
        }
        ESValue definedProperty = super.getPropertyIfAvailable(propertyName, hash);
        if (definedProperty == null) {
            if (isAllDigits(propertyName)) {
                int index = Integer.parseInt(propertyName);
                if (index < value.getStringLength()) {
                    return ESString.valueOf(value.toString().substring(index, index+1));
                }
            }
        }
        return definedProperty;
    }

    private boolean isAllDigits(String propertyName) {
        char[] charArray = propertyName.toCharArray();
        boolean allDigits = true;
        for (char c : charArray) {
            allDigits &= Character.isDigit(c);
        }
        return allDigits;
    }

    public String[] getSpecialPropertyNames() {
        String[] ns = { StandardProperty.LENGTHstring };
        return ns;
    }

    public Object toJavaObject() {
        return value.toString();
    }

    public String toDetailString() {
        return "ES:[Object: builtin " + this.getClass().getName() + ":"
                + ((value == null) ? "null" : value.toString()) + "]";
    }

    /**
     * Information routine to check if a value is a string if true, must
     * implement toString without a evaluator.
     * 
     * @return true
     */
    public boolean isStringValue() {
        return true;
    }

}