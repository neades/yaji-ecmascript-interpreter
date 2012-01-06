// ESNull.java
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
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;

/**
 * Implements the NULL EcmaScript primitive value
 * <P>
 * There is only one value of this type which is referenced by ESNull.theNull.
 * In general it is not identical for a routine to return ESNull, ESUndefined or
 * the java null. They all have different purposes.
 */
public final class ESNull extends ESPrimitive {

    /**
     * the READ-ONLY null value
     */
    public static ESNull theNull = new ESNull();

    private ESNull() {
        // do nothing
    }

    // overrides
    @Override
    public String toDetailString() {
        return "ES:<null>";
    }

    // overrides
    @Override
    public int getTypeOf() {
        return EStypeNull;
    }

    // overrides
    @Override
    public String toString() {
        return "null";
    }

    // overrides
    @Override
    public String getTypeofString() {
        return "object";
    }

    // overrides
    @Override
    public double doubleValue() {
        return 0;
    }

    // overrides
    @Override
    public boolean booleanValue() {
        return false;
    }

    // overrides
    @Override
    public Object toJavaObject() {
        return null;
    }

    /**
     * Advanced FESI GT Modified: 5/10/2002 Serialisation of ESNull objects.
     * writeReplace() and readResolve ensure that the reinstantiated ESNull will
     * be the ESNull.theNull object
     * 
     * @throws ObjectStreamException
     */
    public Object writeReplace() throws ObjectStreamException {
        return new ESNullReplace();
    }

    private static class ESNullReplace implements java.io.Serializable {
        private static final long serialVersionUID = -3321091467896792116L;

        ESNullReplace() {
            // do nothing
        }

        /**
         * @throws ObjectStreamException
         */
        public Object readResolve() throws ObjectStreamException {
            return ESNull.theNull;
        }
    }

    @Override
    public boolean equals(Object obj) {
        // We mean this - there is only one null
        return super.equals(obj);
    }
    
    /**
     * Advanced FESI GT Modified: 12/10/2004 Support for subtypes (storing
     * values in hashset)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equalsSameType(ESValue v2) {
        return true;
    }
    
    @Override
    public boolean canJson() {
        return true;
    }
    
    @Override
    boolean isObjectCoercible() {
        return false;
    }
    
    @Override
    public ESObject toESObject(Evaluator evaluator) throws EcmaScriptException {
        throw new TypeError("'null' cannot be converted to Object");
    }
    
    @Override
    protected boolean sameValueTypeChecked(ESValue other) {
        return true;
    }
}
