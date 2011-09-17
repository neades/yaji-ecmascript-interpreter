// ESObject.java
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
import FESI.Exceptions.ProgrammingError;
import FESI.Exceptions.ReferenceError;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.FesiHashtable;
import FESI.Interpreter.FesiHashtable.IReporter;
import FESI.Interpreter.ScopeChain;
import FESI.Util.EvaluatorAccess;

public abstract class ESObject extends ESValue {

    private static final long serialVersionUID = 3620418273409576807L;

    /** Contains the properties of this object */
    private FesiHashtable properties;

    /** The evaluator owning this object */
    private transient Evaluator evaluator;

    /** The context of this object */
    protected transient Object context;

    protected long objectId;

    /**
     * The prototype of this object ([[prototype]] in the standard, not the
     * "prototype" property of functions!)
     */
    ESObject prototype = null;

    private final int initialSize;

    // Callback used to log profiling data
    static IObjectProfiler profilingCallback = null;

    // This class is used to tell us that this type is being GC'ed
    @SuppressWarnings("unused")
    private transient ESObjectFinalizerListener finalizerListener = null;

    // 5th Edition - 15.2.3.9 - if false no properties may be added to object
    private boolean extensible = true;

    /**
     * Create an object with a specific prototype (which may be null) in the
     * context of a specific evaluator (which may not be null) Uses the default
     * hashTable size.
     * 
     * @param prototype
     *            The prototype ESObject - may be null
     * @param evaluator
     *            The evaluator - must not be null
     */
    protected ESObject(ESObject prototype, Evaluator evaluator) {
        this(prototype, evaluator, null);
    }

    protected ESObject(ESObject prototype, Evaluator evaluator, Object context) {
        this(prototype, evaluator, context, 27);
    }

    protected ESObject(ESObject prototype, Evaluator evaluator, Object context,
            int initialSize) {
        this.prototype = prototype;
        this.evaluator = evaluator; // It will crash somewhere if null...
        this.context = context;

        if (evaluator != null) {
            this.objectId = evaluator.generateObjectId();
        }

        if (profilingCallback != null) {
            finalizerListener = new ESObjectFinalizerListener(this, context);
        }

        this.initialSize = initialSize;
    }

    /**
     * Create an object with a specific prototype (which may be null) in the
     * context of a specific evaluator (which may not be null) Uses the
     * specified hashTable size, which should be a prime. size is usefull for
     * very small (arguments) or very large objects.
     * 
     * @param prototype
     *            The prototype ESObject - may be null
     * @param evaluator
     *            The evaluator - must not be null
     */
    protected ESObject(ESObject prototype, Evaluator evaluator, int initialSize) {
        this(prototype, evaluator, null, initialSize);
    }

    /**
     * Get the evaluator for this object
     * 
     * @return the evaluator Graham Technology: 19/4/2007 Removed final modifier
     *         to allow mocking out of this method
     */
    public Evaluator getEvaluator() {
        return evaluator != null ? evaluator : EvaluatorAccess.getEvaluator();
    }

    /**
     * Get the context for this object
     * 
     * @return the context
     */
    public final Object getESObjectContext() {
        return context;
    }

    /**
     * All objects and thir subclasses are non primitive
     * 
     * @return false
     */
    @Override
    public final boolean isPrimitive() {
        return false;
    }

    /**
     * Implements the [[prototype]] property (see 8.6.2)
     * 
     * @return The prototype object or null
     */
    public ESObject getPrototype() {
        return prototype;
    }

    /**
     * Return the name of the class of objects ([[class]]), as used in the
     * default toString method of objects (15.2.4.2)
     * 
     * @return the [[Class]] property of this object
     */
    public String getESClassName() {
        return "Object";
    }

    /**
     * Return a code indicating the type of the object for the implementation of
     * the "==" operator.
     * 
     * @return A type code
     */
    @Override
    public int getTypeOf() {
        return EStypeObject;
    }

    /**
     * Either return the property value of the specified property in the current
     * object, or lookup the next object in the scope chain if there is one. If
     * there is nones, generate an error message.
     * <P>
     * This routine must be overriden by subclass which change the
     * implementation of getProperty.
     * 
     * @param propertyName
     *            The property to look for
     * @param previousScope
     *            The previous scope or null
     * @param hash
     *            The hashCode of propertyName
     * @return The value of the specified variable
     * @exception EcmaScriptException
     *                if not found in any scope
     */
    public ESValue getPropertyInScope(String propertyName,
            ScopeChain previousScope, int hash) throws EcmaScriptException {        
        ESValue value = getOwnProperty(propertyName, hash);
        if (value == null) {
            if (prototype == null) {
                if (previousScope == null) {
                    throw new ReferenceError("Variable '" + propertyName
                            + "' does not exist in the scope chain");
                }
                value = previousScope.getValue(propertyName, hash);
            } else {
                value = prototype.getPropertyInScope(propertyName,
                        previousScope, hash);
            }

        }
        return value;
    }

    /**
     * Get the property by name (see 8.6.2.1) propagating to the prototype if
     * required
     * 
     * @param propertyName
     *            The string naming the property
     * @param hash
     *            The hashCode of propertyName
     * @return The property or <em>undefined</em>
     * @exception EcmaScriptException
     *                Error in host objects ?
     */
    public ESValue getPropertyIfAvailable(String propertyName, int hash)
            throws EcmaScriptException {
        ESValue value = getOwnProperty(propertyName, hash);
        
        if (value == null) {
            return prototype == null 
                    ? null
                    : prototype.getPropertyIfAvailable(propertyName, hash);
        }

        if (isAccessorDescriptor(value)) {
            ESValue getter = value.get;
            return getter == ESUndefined.theUndefined ? getter : getter
                    .callFunction(this, EMPTY_ARRAY);
        }

        return value;
    }
    
    /**
     * Get the property by name (see 8.6.2.1) propagating to
     * the prototype if required
     *
     * Returns ESUndefined.theUndefined if the property was not found.
     *
     * @param   propertyName  The string naming the property
     * @param   hash  The hashCode of propertyName
     * @return     The property or <em>undefined</em>
     * @exception   EcmaScriptException  Error in host objects ?
     */
    public final ESValue getProperty(String propertyName, int hash) throws EcmaScriptException {
        ESValue value = getPropertyIfAvailable(propertyName, hash);

        if (value == null) {
            value = ESUndefined.theUndefined;
        }

        return value;
    }


    public ESValue getOwnProperty(String propertyName, int hash)
            throws EcmaScriptException {
        ESValue property = hasNoPropertyMap() ? null : getPropertyMap().get(propertyName, hash);
        return property;
    }

    /**
     * Get the property by index value. By default the index is converted to a
     * string, but this can be optimized for arrays.
     * <P>
     * This is not the same as the indexed properties of the first version of
     * JavaScript and does not allow to access named properties other than the
     * property using the integer string representation as a name.
     * 
     * @param index
     *            The property name as an integer.
     * @return The property or <em>undefined</em>
     * @exception EcmaScriptException
     *                Error in host objects ?
     */
    public final ESValue getProperty(int index) throws EcmaScriptException {
        ESValue value = getPropertyIfAvailable(index);

        if (value == null) {
            return ESUndefined.theUndefined;
        }

        return value;
    }
    
    /**
     * Get the property by index value. By default the index is
     * converted to a string, but this can be optimized for arrays.
     * <P>This is not the same as the indexed properties of the first
     * version of JavaScript and does not allow to access named
     * properties other than the property using the integer string
     * representation as a name.
     *
     * Returns null if the property was not found.
     *
     * @param   index  The property name as an integer.
     * @return     The property or <em>null</em>
     * @exception   EcmaScriptException  Error in host objects ?
     */
    public ESValue getPropertyIfAvailable(int index) throws EcmaScriptException {
        String iString = Integer.toString(index);
        return getPropertyIfAvailable(iString, iString.hashCode());
    }

    public final boolean hasProperty(String propertyName, int hash)
            throws EcmaScriptException {
        return getPropertyIfAvailable(propertyName, hash) != null;
    }

    public boolean isHiddenProperty(String propertyName, int hash) {
        return properties != null
                && getPropertyMap().isHidden(propertyName, hash);
    }

    /**
     * Indicates that the getProperties return an enumerator to the index rather
     * rather than to the value index (see ESWrapper).
     * 
     * @return false
     */
    public boolean isDirectEnumerator() {
        return false;
    }

    /**
     * Returns an enumerator for the key elements of this object, that is all is
     * enumerable properties and the (non hidden) ones of its prototype, etc...
     * As used for the for in statement.
     * 
     * @return the enumerator
     */
    public Enumeration<String> getProperties() {
        return new Enumeration<String>() {
            Enumeration<String> props = hasNoPropertyMap() ? null
                    : getPropertyMap().keys();
            String currentKey = null;
            int currentHash = 0;
            boolean inside = false;

            public boolean hasMoreElements() {
                if (currentKey != null) {
                    return true;
                }
                while (props != null && props.hasMoreElements()) {
                    currentKey = props.nextElement();
                    currentHash = currentKey.hashCode();
                    if (inside) {
                        if (getPropertyMap().containsKey(currentKey,
                                currentHash)) {
                            continue;
                        }
                    } else {
                        if (isHiddenProperty(currentKey, currentHash)) {
                            continue;
                        }
                    }
                    return true;
                }
                if (!inside && prototype != null) {
                    inside = true;
                    props = prototype.getProperties();
                    while (props.hasMoreElements()) {
                        currentKey = props.nextElement();
                        currentHash = currentKey.hashCode();
                        if (getPropertyMap().containsKey(currentKey,
                                currentHash)) {
                            continue;
                        }
                        return true;
                    }
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

    /**
     * Get all properties (including hidden ones), for the command
     * 
     * @listall of the interpreter. Include the visible properties of the
     *          prototype (that is the one added by the user) but not the hidden
     *          ones of the prototype (otherwise this would list all functions
     *          for any object).
     * 
     * @return An enumeration of all properties (visible and hidden).
     */
    public Enumeration<String> getAllProperties() {
        return new Enumeration<String>() {
            String[] specialProperties = getSpecialPropertyNames();
            int specialEnumerator = 0;
            Enumeration<String> props = hasNoPropertyMap() ? null
                    : getPropertyMap().keys(); // all of object properties
            String currentKey = null;
            int currentHash = 0;
            boolean inside = false; // true when examing prototypes properties

            public boolean hasMoreElements() {
                // OK if we already checked for a property and one exists
                if (currentKey != null) {
                    return true;
                }
                // Loop on special properties first
                if (specialEnumerator < specialProperties.length) {
                    currentKey = specialProperties[specialEnumerator];
                    currentHash = currentKey.hashCode();
                    specialEnumerator++;
                    return true;
                }
                // loop on standard or prototype properties
                while (props != null && props.hasMoreElements()) {
                    currentKey = props.nextElement();
                    currentHash = currentKey.hashCode();
                    if (inside) {
                        if (getPropertyMap().containsKey(currentKey,
                                currentHash)) {
                            continue;
                            // SHOULD CHECK IF NOT IN SPECIAL
                        }
                    }
                    return true;
                }
                // If prototype properties have not yet been examined, look for
                // them
                if (!inside && prototype != null) {
                    inside = true;
                    props = prototype.getProperties();
                    while (props.hasMoreElements()) {
                        currentKey = props.nextElement();
                        currentHash = currentKey.hashCode();
                        if (getPropertyMap().containsKey(currentKey,
                                currentHash)) {
                            continue;
                        }
                        return true;
                    }
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

    /**
     * Put the property by name (see 8.6.2.2), ignoring if read only (integrate
     * functionality of canPut) and creating it if needed and possible.
     * <P>
     * The routine implement the functionality of the canPut attribute.
     * 
     * @param propertyName
     *            The string naming the property
     * @param propertyValue
     *            The value to put
     * @exception EcmaScriptException
     *                Error in host objects ?
     */
    public void putProperty(String propertyName, ESValue propertyValue, int hash)
            throws EcmaScriptException {
        if (!canPut(propertyName, hash)) {
            if (isStrictMode()) {
                throw new TypeError("Property " + propertyName
                        + " cannot be modified");
            }
        } else {
            ESValue desc = getOwnProperty(propertyName, hash);
            if (hasSetAccessorDescriptor(desc)) {
                ESValue setter = desc.set;
                if (setter != ESUndefined.theUndefined) {
                    setter.callFunction(this, new ESValue[] {propertyValue});
                }
            } else {
                getPropertyMap().put(propertyName, hash, false, false,
                    propertyValue, true);
            }
        }
    }

    private boolean isStrictMode() {
        final Evaluator evaluator = getEvaluator();
        if (evaluator == null) {
            return true;
        }
        return evaluator.isStrictMode();
    }

    private boolean canPut(String propertyName, int hash) {
        return !getPropertyMap().isReadonly(propertyName, hash, extensible);
    }

    /**
     * Put the property by index value. By default the index is converted to a
     * string, but this can be optimized for arrays.
     * <P>
     * This is not the same as the indexed properties of the first version of
     * JavaScript and does not allow to access named properties other than the
     * property using the integer string representation as a name.
     * 
     * @param index
     *            The property name as an integer.
     * @param propertyValue
     *            The value to put
     * @exception EcmaScriptException
     *                Error in host objects ?
     */
    public void putProperty(int index, ESValue propertyValue)
            throws EcmaScriptException {
        String iString = Integer.toString(index);
        putProperty(iString, propertyValue, iString.hashCode());
    }

    /**
     * Separated from putProperty to allow read only fields to be initialised at
     * ESObject level this simply behaves the same as putProperty - it is
     * intended to be overridden by subclasses of ESObject.
     * 
     * @param index
     * @param propertyValue
     * @throws EcmaScriptException
     */
    public void initializeProperty(String propertyName, ESValue propertyValue,
            int hash) throws EcmaScriptException {
        getPropertyMap().put(propertyName, hash, false, false, propertyValue, true);
    }

    /**
     * Put the property as hidden. This is mostly used by initialisation code,
     * so a hash value is computed locally and the string is interned.
     * 
     * @param propertyName
     *            The name of the property
     * @param propertyValue
     *            Its value
     * @exception EcmaScriptException
     *                Not used
     */
    public void putHiddenProperty(String propertyName, ESValue propertyValue)
            throws EcmaScriptException {
        propertyName = propertyName.intern();
        int hash = propertyName.hashCode();
        getPropertyMap().put(propertyName, hash, true, false, propertyValue, true);
    }

    /**
     * Implements the [[delete]] function (8.6.2.5), only called by the DELETE
     * operator. Should return true if the propery does not exist any more (or
     * did not exist at all) after the return.
     * <P>
     * This routine must implement the DontDelete attribue too.
     * 
     * @param propertyName
     *            The name of the property
     * @return true if the property is not present anymore
     * @exception EcmaScriptException
     *                Not used
     */
    public boolean deleteProperty(String propertyName, int hash)
            throws EcmaScriptException {
        if (hasPropertyMap()) {
            getPropertyMap().remove(propertyName, hash, isStrictMode());
        }
        return true; // either it did not exist or was deleted !
    }

    /**
     * Implements [[DefaultValue]] with hint
     * 
     * @param hint
     *            A type hint (only string or number)
     * @exception EcmaScriptException
     *                Propagated or bad hint
     * @return the default value of this object
     */
    public ESValue getDefaultValue(int hint) throws EcmaScriptException {
        ESValue theResult = null;
        ESValue theFunction = null;

        if (hint == ESValue.EStypeString) {
            theFunction = this.getProperty(StandardProperty.TOSTRINGstring, StandardProperty.TOSTRINGhash);
            if (theFunction instanceof ESObject) {
                theResult = theFunction.callFunction(this, new ESValue[0]);
                if (theResult.isPrimitive()) {
                    return theResult;
                }
            }
            theFunction = this.getProperty(StandardProperty.VALUEOFstring, StandardProperty.VALUEOFhash);
            if (theFunction instanceof ESObject) {
                theResult = theFunction.callFunction(this, new ESValue[0]);
                if (theResult.isPrimitive()) {
                    return theResult;
                }
            }
            // Throw errror on super to avoid evaluating this with as a string,
            // as this is exactly what we cannot do.
            throw new EcmaScriptException("No default value for "
                    + super.toString() + " and hint " + hint);
        } else if (hint == ESValue.EStypeNumber) {
            theFunction = this.getProperty(StandardProperty.VALUEOFstring, StandardProperty.VALUEOFhash);
            if (theFunction instanceof ESObject) {
                theResult = theFunction.callFunction(this, new ESValue[0]);
                if (theResult.isPrimitive()) {
                    return theResult;
                }
            }
            theFunction = this.getProperty(StandardProperty.TOSTRINGstring, StandardProperty.TOSTRINGhash);
            if (theFunction instanceof ESObject) {
                theResult = theFunction.callFunction(this, new ESValue[0]);
                if (theResult.isPrimitive()) {
                    return theResult;
                }
            }
        }
        throw new EcmaScriptException("No default value for " + this
                + " and hint " + hint);
    }

    /**
     * Implements [[DefaultValue]] with no hint
     * <P>
     * The default is different for dates
     * 
     * @exception EcmaScriptException
     *                Propagated
     * @return the default value of this object
     */
    public ESValue getDefaultValue() throws EcmaScriptException {

        return this.getDefaultValue(EStypeNumber);
    }

    /**
     * Call a function object - not implemented for default objecr
     * 
     * @param thisObject
     *            The current object
     * @param arguments
     *            The arguments to the function
     * @return The calculated value
     * @exception EcmaScriptException
     *                thrown because the function is not implemented
     */
    @Override
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        throw new EcmaScriptException("No function defined on: " + this);
    }

    /**
     * A construct as thisObject.functionName() was detected, The functionName
     * is looked up, then a call is made. This avoid creating a dummy function
     * object when one does not exists, like for the ESWrapper objects (where
     * functions are really java methods).
     * <P>
     * Only method which do not use the standard EcmaScript function evaluation
     * mechanism need to override this method.
     * 
     * @param evaluator
     *            The evaluator
     * @param target
     *            The original target (for recursive calls)
     * @param functionName
     *            The name of the function property
     * @param arguments
     *            The arguments of the function
     * @return The result of calling the function
     * @exception EcmaScriptException
     *                Function not defined
     * @exception NoSuchMethodException
     *                Method not found
     */
    public ESValue doIndirectCall(Evaluator evaluator, ESObject target,
            String functionName, ESValue[] arguments)
            throws EcmaScriptException, NoSuchMethodException {
        ESValue theFunction = hasNoPropertyMap() ? null : getPropertyMap().get(
                functionName, functionName.hashCode());
        if (theFunction == null) {
            if (prototype == null) {
                throw new TypeError("The function '" + functionName
                        + "' is not defined for object '" + target.toString()
                        + "'");
            }
            return prototype.doIndirectCall(evaluator, target, functionName,
                    arguments);

        }
        return theFunction.callFunction(target, arguments);

    }

    // A routine which may return a function as the value of a builtin
    // property must override this function
    public ESValue doIndirectCallInScope(Evaluator evaluator,
            ScopeChain previousScope, ESObject thisObject, String functionName,
            int hash, ESValue[] arguments) throws EcmaScriptException {
        ESValue theFunction = hasNoPropertyMap() ? null : getPropertyMap().get(
                functionName, hash);
        if (theFunction == null) {
            if (previousScope == null) {
                throw new EcmaScriptException("no global function named '"
                        + functionName + "'");
            }
            return previousScope.doIndirectCall(evaluator, thisObject,
                    functionName, hash, arguments);

        }
        return theFunction.callFunction(thisObject, arguments);
    }

    /**
     * Call the constructor - not implemented on default object
     * 
     * @param thisObject
     *            The current object
     * @param arguments
     *            Arguments to new
     * @return The created obbjecr
     * @exception EcmaScriptException
     *                thrown because this function is not implemented
     */
    @Override
    public ESObject doConstruct(ESObject thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        throw new TypeError("No constructor defined on: " + this);
    }

    /**
     * Return a double value for this object if possible
     * 
     * @return The double value
     * @exception EcmaScriptException
     *                If not a suitable primitive
     */
    @Override
    public double doubleValue() throws EcmaScriptException {
        ESValue value = ESUndefined.theUndefined;
        double d = Double.NaN;
        try {
            value = toESPrimitive(EStypeNumber);
            d = value.doubleValue();
        } catch (EcmaScriptException e) {
            throw new ProgrammingError(e.getMessage());
        }
        return d;
    }

    /**
     * Return the boolean value of this object if possible
     * 
     * @return the boolean value
     * @exception EcmaScriptException
     *                If not a suitable primitive
     */
    @Override
    public boolean booleanValue() throws EcmaScriptException {
        return true;
    }

    @Override
    public String toString() {
        ESValue value = ESUndefined.theUndefined;
        String string = null;
        try {
            value = toESPrimitive(EStypeString);
        } catch (EcmaScriptException e) {
            return this.toDetailString();
        }
        string = value.toString();
        return string;
    }

    /**
     * @param context
     */
    public String getScopedName(Object context) {
        return getESClassName();
    }

    /**
     * Convert to an object
     * 
     * @param evaluator
     *            The evaluator
     * @return This
     * @exception EcmaScriptException
     *                not thrown
     */
    @Override
    public final ESObject toESObject(Evaluator evaluator)
            throws EcmaScriptException {
        return this;
    }

    /**
     * Convert to a primitive
     * 
     * @param preferedType
     *            For string or number
     * @return The primitive value
     * @exception EcmaScriptException
     *                If no suitable default value
     */
    @Override
    public final ESValue toESPrimitive(int preferedType)
            throws EcmaScriptException {
        return getDefaultValue(preferedType);
    }

    /**
     * Convert to a primitive
     * 
     * @return The primitive value
     * @exception EcmaScriptException
     *                If no suitable default value
     */
    @Override
    public final ESValue toESPrimitive() throws EcmaScriptException {
        return getDefaultValue();
    }

    /**
     * Return a Java object which is the object to pass to Java routines called
     * by FESI. By default wrap the ESObject in a wrapper object, used by the
     * jslib. Overriden by subclass if a better type can be found.
     * 
     * @return a wrapper object over this ESObject.
     */
    @Override
    public Object toJavaObject() {
        return new JSWrapper(this, getEvaluator());
    }

    /**
     * Return the name of the type of the object for the typeof operator
     * 
     * @return The name of the type as a String
     */
    @Override
    public String getTypeofString() {
        return "object";
    }

    @Override
    public String toDetailString() {
        return "ES:[" + getESClassName() + "]";
    }

    /**
     * Return true to indicate that this value is composite.
     * 
     * @return true
     */
    @Override
    public boolean isComposite() {
        return true;
    }

    /**
     * Return the list of proprties which are not listed by getAll, that is all
     * special properties handled directly by getProperty, which are not in the
     * property hash table (they are considered hidden) Must be overriden by a
     * subclass which overrides getProperty!
     * 
     * return The array of special property names
     */
    public String[] getSpecialPropertyNames() {
        return new String[0];
    }

    protected Enumeration<String> getAllDescriptionKeys() {
        final String[] sp = getSpecialPropertyNames();
        return sp == null ? null : new Enumeration<String>() {
            String[] specialProperties = sp;
            int specialEnumerator = 0;
            Enumeration<String> props = hasNoPropertyMap() ? null
                    : getPropertyMap().keys();
            String currentKey = null;

            public boolean hasMoreElements() {
                // If we have one already, send it
                if (currentKey != null) {
                    return true;
                }
                // Loop on special properties first
                if (specialEnumerator < specialProperties.length) {
                    currentKey = specialProperties[specialEnumerator];
                    specialEnumerator++;
                    return true;
                }
                // Otherwise check in current enumeration
                while (props != null && props.hasMoreElements()) {
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

    /**
     * Get an enumeration of the description of various aspects of the object,
     * including all properties.
     */
    @Override
    public Enumeration<ValueDescription> getAllDescriptions() {
        return new Enumeration<ValueDescription>() {
            String[] specialProperties = getSpecialPropertyNames();
            int specialEnumerator = 0;
            Enumeration<String> props = hasNoPropertyMap() ? null
                    : getPropertyMap().keys();
            String currentKey = null;
            boolean inside = false;
            boolean inSpecial = true;
            ESObject proto = prototype;

            public boolean hasMoreElements() {
                // If we have one already, send it
                if (currentKey != null) {
                    return true;
                }
                // Loop on special properties first
                if (specialEnumerator < specialProperties.length) {
                    currentKey = specialProperties[specialEnumerator];
                    specialEnumerator++;
                    return true;
                }
                inSpecial = false;
                // Otherwise check in current enumeration
                while (props != null && props.hasMoreElements()) {
                    currentKey = props.nextElement();
                    // if (inside) {
                    // if (properties.containsKey(currentKey, currentHash))
                    // continue;
                    // }
                    return true;
                }
                // Got to prototype enumeration if needed
                if (proto != null) {
                    inside = true;
                    props = proto.getAllDescriptionKeys();
                    if (props == null) {
                        return false;
                    }

                    proto = proto.getPrototype();
                    while (props.hasMoreElements()) {
                        currentKey = props.nextElement();
                        // if (properties.containsKey(currentKey, currentHash))
                        // continue;
                        return true;
                    }
                }
                return false;
            }

            public ValueDescription nextElement() {
                if (hasMoreElements()) {
                    String key = currentKey;
                    int hash = key.hashCode();
                    currentKey = null;
                    ESValue value = null;
                    try {
                        value = ESObject.this.getProperty(key, hash);
                    } catch (EcmaScriptException e) {
                        throw new ProgrammingError("Unexpected exception " + e);
                    }
                    String propertyKind;
                    if (inSpecial) {
                        propertyKind = "HIDDEN";
                    } else if (inside
                            && getPropertyMap().containsKey(key, hash)) {
                        propertyKind = "INVISIBLE";
                    } else {
                        propertyKind = isHiddenProperty(key, hash) ? "HIDDEN"
                                : "VISIBLE";
                    }
                    propertyKind += (inside ? " PROTOTYPE" : " OBJECT");
                    propertyKind += " PROPERTY";
                    return new ValueDescription(key, propertyKind, value
                            .toString());
                }
                throw new java.util.NoSuchElementException();

            }
        };
    }

    /**
     * Returns a full description of the value, with the specified name.
     * 
     * @param name
     *            The name of the value to describe
     * 
     * @return the description of this value
     */
    @Override
    public ValueDescription getDescription(String name) {
        return new ValueDescription(name, "OBJECT", this.toString());
    }

    public static void setObjectProfiler(IObjectProfiler op) {
        profilingCallback = op;
    }

    public static IObjectProfiler getObjectProfiler() {
        return profilingCallback;
    }

    class ESObjectFinalizerListener {
        private final ESObject object;
        private Object myContext = null;

        public ESObjectFinalizerListener(ESObject object) {
            this.object = object;
            if (profilingCallback != null) {
                writeProfile("C");
            }
        }

        public ESObjectFinalizerListener(ESObject object, Object context) {
            this.object = object;
            this.myContext = context;
            if (profilingCallback != null) {
                writeProfile("C");
            }
        }

        @Override
        protected void finalize() throws Throwable {
            if (profilingCallback != null) {
                writeProfile("D");
            }
        }

        private void writeProfile(String state) {
            long currentTime = System.currentTimeMillis();

            profilingCallback.write(getEvaluator(), currentTime, object,
                    myContext, state);
        }
    }

    // save some space with serialization by not writing empty hashtables
    private void writeObject(java.io.ObjectOutputStream out)
            throws java.io.IOException {
        FesiHashtable tmp = properties;
        if (hasPropertyMap() && this.getPropertyMap().size() == 0) {
            this.setPropertyMap(null);
        }
        out.defaultWriteObject();
        this.setPropertyMap(tmp);
    }

    protected boolean hasPropertyMap() {
        return !hasNoPropertyMap();
    }

    public long getObjectId() {
        return objectId;
    }

    protected void setPropertyMap(FesiHashtable properties) {
        this.properties = properties;
    }

    protected FesiHashtable getPropertyMap() {
        if (hasNoPropertyMap()) {
            setPropertyMap(new FesiHashtable(initialSize));
        }
        return properties;
    }

    protected boolean hasNoPropertyMap() {
        return properties == null;
    }

    public void freeze() {
        extensible = false;
        if (!hasNoPropertyMap()) {
            getPropertyMap().setAllNonConfigurable(true);
        }
        evaluator = null;
    }

    public boolean isFrozen() {
        boolean frozen = !extensible;
        if (frozen && !hasNoPropertyMap()) {
            frozen = getPropertyMap().isAllReadOnly();
        }
        return frozen;
    }

    public boolean hasInstance(ESValue v1) throws EcmaScriptException {
        throw new TypeError("hasInstance not implemented");
    }
    public void putProperty(String propertyName, ESValue value) throws EcmaScriptException {
        putProperty(propertyName, value, propertyName.hashCode());
    }

    ESValue getProperty(String propertyName) throws EcmaScriptException {
        return getProperty(propertyName,propertyName.hashCode());
    }

    public ESValue getOwnPropertyDescriptor(String propertyName) throws EcmaScriptException {
        return hasNoPropertyMap() ? ESUndefined.theUndefined : getPropertyMap().getOwnPropertyDescriptor(propertyName,getEvaluator());
    }

    public ESValue defineProperty(final String propertyName, ESObject desc) throws EcmaScriptException {
        getPropertyMap().defineProperty(propertyName,desc.getPropertyMap(), new IReporter() {
            
            public boolean reject(String message) throws TypeError{
                throw new TypeError("Cannot define property "+propertyName+" on Object : "+message);
            }
        }, isExtensible(), getEvaluator());
        return this;
    }

    private boolean isExtensible() {
        return extensible;
    }

    public Enumeration<String> getOwnPropertyNames() {
        return getPropertyMap().keys();
    }
}
