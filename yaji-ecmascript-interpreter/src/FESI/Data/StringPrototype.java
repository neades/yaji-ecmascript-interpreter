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

import java.util.Enumeration;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.ScopeChain;

class StringPrototype extends ESObject {
    private static final long serialVersionUID = 1434073915774458544L;

    ESString value = new ESString("");

    StringPrototype(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator);
    }

    @Override
    public String getESClassName() {
        return "String";
    }

    @Override
    public String toString() {
        if (value == null)
            return super.toString();
        return value.toString();
    }

    @Override
    public ESValue toESString() {
        return value;
    }

    @Override
    public boolean booleanValue() throws EcmaScriptException {
        return value.booleanValue();
    }

    @Override
    public double doubleValue() throws EcmaScriptException {
        return value.doubleValue();
    }

    @Override
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
                int index = toUInt32(propertyName);
                if (index < value.getStringLength() && index >= 0) {
                    return ESString.valueOf(value.toString().substring(index, index+1));
                }
            }
        }
        return definedProperty;
    }

    @Override
    public String[] getSpecialPropertyNames() {
        String[] ns = { StandardProperty.LENGTHstring };
        return ns;
    }

    @Override
    public Object toJavaObject() {
        return value.toString();
    }

    @Override
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
    @Override
    public boolean isStringValue() {
        return true;
    }

    @Override
    public Enumeration<String> getOwnPropertyNames() {
        return new ArrayPropertyNamesEnumeration(super.getOwnPropertyNames(), value.getStringLength());
    }
}
