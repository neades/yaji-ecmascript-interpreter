// ESArrayWrapper.java
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

import java.lang.reflect.Array;
import java.util.Enumeration;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.ProgrammingError;
import FESI.Exceptions.ReferenceError;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.ScopeChain;

// Class to wrap a Java array as an EcmaScript object
public class ESArrayWrapper extends ESObject {

    private static final long serialVersionUID = -3203904964317381261L;
    // The java array
    protected Object javaArray;

    /**
     * Create a new array wrapper
     * 
     * @param javaArray
     *            the java array to wrap
     * @param evaluator
     *            the evaluator
     */
    public ESArrayWrapper(Object javaArray, Evaluator evaluator) {
        super(null, evaluator);
        this.javaArray = javaArray;
        if (!javaArray.getClass().isArray()) {
            throw new ProgrammingError("Array wrapper used on non array object");
        }
    }

    // overrides
    @Override
    public ESObject getPrototype() {
        throw new ProgrammingError("Cannot get prototype of Array Wrapper");
    }

    // overrides
    @Override
    public String getESClassName() {
        return "Java Array";
    }

    // overrides
    @Override
    public int getTypeOf() {
        return EStypeObject;
    }

    // overrides
    @Override
    public void putProperty(String propertyName, ESValue propertyValue, int hash)
            throws EcmaScriptException {
        if (propertyName.equals("length")) {
            int length = (int) (((ESPrimitive) propertyValue).doubleValue());
            if (length < 0) {
                throw new EcmaScriptException("Invalid length value: "
                        + propertyValue);
            }
            throw new EcmaScriptException("length of Java Arrays is immutable");
        }
        int index = -1; // indicates not a valid index value
        try {
            index = Integer.parseInt(propertyName); // should be uint
        } catch (NumberFormatException e) {
            // do nothing
        }
        if (index < 0) {
            throw new EcmaScriptException(
                    "Java Arrays accept only index properties");
        }
        putProperty(index, propertyValue);

    }

    // overrides
    @Override
    public void putProperty(int index, ESValue propertyValue)
            throws EcmaScriptException {

        int l = Array.getLength(javaArray);
        if (index >= l || index < 0) {
            throw new EcmaScriptException("Index " + index
                    + " outside of Java Arrays size of " + l);
        }
        Object obj = propertyValue.toJavaObject();
        try {
            Array.set(javaArray, index, obj);
        } catch (IllegalArgumentException e) {
            String type = "null";
            if (obj != null) {
                type = ESLoader.typeName(obj.getClass());
            }
            throw new EcmaScriptException("Cannot store a " + type
                    + " in the java array "
                    + ESLoader.typeName(javaArray.getClass()));
        }
    }

    // overrides
    @Override
    public ESValue getPropertyInScope(String propertyName,
            ScopeChain previousScope, int hash) throws EcmaScriptException {
        if (propertyName.equals("length")) {
            return ESNumber.valueOf(Array.getLength(javaArray));
        }
        // Do not examine the integer values...
        if (previousScope == null) {
            throw new ReferenceError("Variable '" + propertyName
                    + "' does not exist in the scope chain");
        }
        return previousScope.getValue(propertyName, hash);

    }

    // overrides
    @Override
    public ESValue getPropertyIfAvailable(String propertyName, int hash)
            throws EcmaScriptException {
        if (propertyName.equals("length")) {
            return ESNumber.valueOf(Array.getLength(javaArray));
        }
        int index = -1; // indicates not a valid index value
        try {
            index = Integer.parseInt(propertyName); // should be uint
        } catch (NumberFormatException e) {
            // do nothing
        }
        if (index < 0) {
            throw new EcmaScriptException(
                    "Java Arrays accept only index properties");
        }
        return getPropertyIfAvailable(index);
    }

    // overrides
    @Override
    public ESValue getPropertyIfAvailable(int index) throws EcmaScriptException {
        Object theElement = null;
        int l = Array.getLength(javaArray);
        if (index >= l || index < 0) {
            throw new EcmaScriptException("Java Array index " + index
                    + " is out of range " + l);
        }
        theElement = Array.get(javaArray, index);
        return ESLoader.normalizeValue(theElement, getEvaluator());
    }

    // Skip elements which were never set (are null), as Netscape
    @Override
    public Enumeration<String> getProperties() {
        return new Enumeration<String>() {
            int nextIndex = 0;
            int length = Array.getLength(javaArray);

            public boolean hasMoreElements() {
                while ((nextIndex < length)
                        && (Array.get(javaArray, nextIndex) == null)) {
                    nextIndex++;
                }
                return nextIndex < length;
            }

            public String nextElement() {
                if (hasMoreElements()) {
                    return Integer.toString(nextIndex++);
                }
                throw new java.util.NoSuchElementException();

            }
        };
    }

    /**
     * Get all properties (including hidden ones), for the command
     * 
     * @listall of the interpreter.
     *          <P>
     *          An ESArrayWrapper has no prototype, but it has the hidden
     *          property LENGTH.
     * 
     * @return An enumeration of all properties (visible and hidden).
     */
    @Override
    public Enumeration<String> getAllProperties() {
        return new Enumeration<String>() {
            String[] specialProperties = getSpecialPropertyNames();
            int specialEnumerator = 0;
            Enumeration<String> props = getPropertyMap().keys(); // all of
                                                                 // object
                                                                 // properties
            String currentKey = null;
            int nextIndex = 0;
            int length = Array.getLength(javaArray);

            public boolean hasMoreElements() {
                // OK if we already checked for a property and one exists
                if (currentKey != null) {
                    return true;
                }
                // loop on index properties
                if (nextIndex < length) {
                    while ((nextIndex < length)
                            && (Array.get(javaArray, nextIndex) == null)) {
                        // ignore null entries
                        nextIndex++;
                    }
                    if (nextIndex < length) {
                        currentKey = Integer.toString(nextIndex);
                        nextIndex++;
                        return true;
                    }
                }
                // Loop on special properties first
                if (specialEnumerator < specialProperties.length) {
                    currentKey = specialProperties[specialEnumerator];
                    specialEnumerator++;
                    return true;
                }
                // loop on standard or prototype properties
                if (props.hasMoreElements()) {
                    currentKey = props.nextElement();
                    return true;
                }
                return false;
            }

            public String nextElement() {
                if (hasMoreElements()) {
                    String key = currentKey;
                    currentKey = null;
                    return key;
                }
                throw new java.util.NoSuchElementException();
            }
        };
    }

    // overrides
    @Override
    public String[] getSpecialPropertyNames() {
        String[] ns = { "length" };
        return ns;
    }

    // overrides
    @Override
    public boolean isHiddenProperty(String propertyName, int hash) {
        return false;
    }

    // overrides
    @Override
    public void putHiddenProperty(String propertyName, ESValue propertyValue)
            throws EcmaScriptException {
        throw new ProgrammingError("Cannot put hidden property in " + this);
    }

    // overrides
    @Override
    public boolean deleteProperty(String propertyName, int hash)
            throws EcmaScriptException {
        return !hasProperty(propertyName, hash); // none can be deleted
    }

    // overrides
    @Override
    public ESValue getDefaultValue(int hint) throws EcmaScriptException {
        if (hint == EStypeString) {
            return new ESString(javaArray.toString());
        }
        throw new EcmaScriptException("No default value for " + this
                + " and hint " + hint);

    }

    @Override
    public ESValue getDefaultValue() throws EcmaScriptException {
        return this.getDefaultValue(EStypeString);
    }

    // overrides
    @Override
    public double doubleValue() {
        double d = Double.NaN;
        return d;
    }

    // overrides
    @Override
    public boolean booleanValue() {
        return true;
    }

    // overrides
    @Override
    public String toString() {
        return (javaArray == null) ? "<?Array Wrapper to null?>"
                : "[object JavaArray]";
    }

    // overrides
    @Override
    public Object toJavaObject() {
        return javaArray;
    }

    // public String getTypeofString() {
    // return "JavaArray";
    // }

    // overrides
    @Override
    public String toDetailString() {
        return "ES:[" + getESClassName() + ":" + javaArray.toString() + "]";
    }

}
