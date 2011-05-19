// NumberPrototype.java
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

/**
 * Implements the prototype and is the class of all Number objects
 */
class NumberPrototype extends ESObject {
    private static final long serialVersionUID = 5851526384066944881L;
    // The value
    protected ESNumber value = ESNumber.valueOf(0.0);

    /**
     * Create a new un-initialzed Number
     */
    NumberPrototype(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator);
    }

    // overrides
    public String getESClassName() {
        return "Number";
    }

    // overrides
    public boolean isNumberValue() {
        return true;
    }

    // overrides
    public double doubleValue() {
        return value.doubleValue();
    }

    // overrides
    public boolean booleanValue() {
        return value.booleanValue();
    }

    // overrides
    public String toString() {
        if (value == null)
            return super.toString();
        return value.toString();
    }

    // overrides
    public ESValue toESNumber() throws EcmaScriptException {
        return value;
    }

    // overrides
    public Object toJavaObject() {
        return new Double(value.doubleValue());
    }

    // overrides
    public String toDetailString() {
        return "ES:[Object: builtin " + this.getClass().getName() + ":"
                + ((value == null) ? "null" : value.toString()) + "]";
    }

}
