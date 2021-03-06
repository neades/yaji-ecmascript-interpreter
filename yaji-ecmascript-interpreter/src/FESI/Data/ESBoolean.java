// ESBoolean.java
// FESI Copyright (c) Jean-Marc Lugrin, 1999
// Advanced FESI Copyright (c) Graham Technology, 2002
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

import java.io.ObjectStreamException;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

/**
 * Boolean primitive value
 */
public final class ESBoolean extends ESPrimitive {

    // There is only one true and one false value (allowing
    // efficient comparisons)
    public static final ESBoolean TRUE = new ESBoolean();
    public static final ESBoolean FALSE = new ESBoolean();

    private ESBoolean() {
        // do nothing
    }

    /**
     * Create a boolean primitive (either true or false) by returning the
     * predefined (unique) true or false values
     * 
     * @deprecated Replaced by ESBoolean.valueOf
     * @return either trueValue or falseValue
     */
    @Deprecated
    static public ESBoolean makeBoolean(boolean value) {
        return valueOf(value);
    }

    /**
     * Create a boolean primitive (either true or false) by returning the
     * predefined (unique) true or false values
     * 
     * @return either trueValue or falseValue
     */
    static public ESBoolean valueOf(boolean value) {
        return value ? TRUE : FALSE;
    }
    
    // overrides
    @Override
    public String toDetailString() {
        return "ES:<" + (this == TRUE ? "true" : "false") + ">";
    }

    // overrides
    @Override
    public int getTypeOf() {
        return EStypeBoolean;
    }

    // overrides
    @Override
    public String getTypeofString() {
        return "boolean";
    }

    // overrides
    @Override
    public String toString() {
        return this == TRUE ? "true" : "false";
    }

    // overrides
    @Override
    public double doubleValue() {
        return this == TRUE ? 1 : 0;
    }

    // overrides
    @Override
    public boolean booleanValue() {
        return this == TRUE;
    }

    // overrides
    @Override
    public ESValue toESBoolean() {
        return this;
    }

    // overrides
    @Override
    public ESObject toESObject(Evaluator evaluator) throws EcmaScriptException {
        BooleanPrototype theObject = null;
        ESObject bp = evaluator.getBooleanPrototype();
        theObject = new BooleanPrototype(bp, evaluator);
        theObject.value = this;
        return theObject;
    }

    // overrides
    @Override
    public Object toJavaObject() {
        return Boolean.valueOf(this == TRUE);
    }

    // overrides
    /**
     * returns true as we implement booleanValue without an evaluator.
     * 
     * @return true
     */
    @Override
    public boolean isBooleanValue() {
        return true;
    }

    /**
     * Advanced FESI GT Modified: 5/10/2002 Serialisation of ESBoolean objects.
     * writeReplace() and readResolve ensure that the reinstantiated ESBoolean
     * will be the ESBoolean.trueValue or the ESBoolean.falseValue object
     * 
     * @throws ObjectStreamException
     */
    public Object writeReplace() throws ObjectStreamException {
        return new ESBooleanReplace(this == TRUE);
    }

    private static class ESBooleanReplace implements java.io.Serializable {
        private static final long serialVersionUID = -123640878171914112L;
        private boolean value = false;

        ESBooleanReplace(boolean value) {
            this.value = value;
        }

        /**
         * @throws ObjectStreamException
         */
        public Object readResolve() throws ObjectStreamException {
            return ESBoolean.valueOf(value);
        }
    }

    /**
     * Advanced FESI GT Modified: 5/10/2004 Support for subtypes (storing values
     * in hashset)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return booleanValue() ? 1 : 0;
    }

    /**
     * Advanced FESI GT Modified: 12/10/2004 Support for subtypes (storing
     * values in hashset)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ESValue) {
            // handle the special cases of ESNull and ESUndefined
            if (o == ESNull.theNull || o == ESUndefined.theUndefined) {
                return false;
            }

            try {
                return booleanValue() == ((ESValue) o).booleanValue();
            } catch (EcmaScriptException e) {
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean equalsSameType(ESValue v2) throws EcmaScriptException {
        boolean b1 = booleanValue();
        boolean b2 = v2.booleanValue();
        return b1 == b2;
    }
    
    @Override
    public boolean canJson() {
        return true;
    }
    
    @Override
    protected boolean sameValueTypeChecked(ESValue other) throws EcmaScriptException {
        return booleanValue() == other.booleanValue();
    }
    
    @Override
    public ESValue toESNumber() throws EcmaScriptException {
        return this == TRUE ? ESNumber.valueOf(1) : ESNumber.valueOf(0);
    }
}
