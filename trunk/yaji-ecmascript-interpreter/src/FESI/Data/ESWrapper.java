// ESWrapper.java
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

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;

import org.yaji.util.ArrayUtil;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.ProgrammingError;
import FESI.Exceptions.ReferenceError;
import FESI.Interpreter.ClassInfo;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.ScopeChain;

/**
 * Wrap a Java Object for use in EcmaScript
 */
public class ESWrapper extends ESObject {
    private static final long serialVersionUID = 6345193769772917533L;
    // For debugging
    static boolean debugEvent = false;

    public static void setDebugEvent(boolean b) {
        debugEvent = b;
    }

    static public boolean isDebugEvent() {
        return debugEvent;
    }

    /*
     * 2/5/2006 Graham Technology modification Made javaObject protected to make
     * sub-classing easier
     */
    protected Object javaObject; // the wrapped object
    boolean asBean = false; // true if created as a bean

    // A marker object never returned as a valid property !
    private static ESValue noPropertyMarker = null;

    /**
     * Wrap a java object in an EcmaScript object, use object and beans
     * reflection to access the java object fields.
     * 
     * @param javaObject
     *            the Java object to wrap
     * @param evaluator
     *            the evaluator
     */
    public ESWrapper(Object javaObject, Evaluator evaluator) {
        super(null, evaluator);
        this.javaObject = javaObject;
        if (javaObject.getClass().isArray()) {
            throw new ProgrammingError("Object wrapper used on array object");
        }
        // This should be done at startup
        if (noPropertyMarker == null) {
            noPropertyMarker = new ESString("No Property Marker");
        }
    }

    /**
     * Wrap a java object in an EcmaScript object, use only beans reflection to
     * access the java object fields if asBean is true.
     * 
     * @param javaObject
     *            the Java object to wrap
     * @param evaluator
     *            the evaluator
     * @param asBean
     *            if true always consider the object as a bean
     */
    public ESWrapper(Object javaObject, Evaluator evaluator, boolean asBean) {
        super(null, evaluator);
        this.javaObject = javaObject;
        this.asBean = asBean;
        if (javaObject.getClass().isArray()) {
            throw new ProgrammingError("Object wrapper used on array object");
        }
        // This should be done at startup
        if (noPropertyMarker == null) {
            noPropertyMarker = new ESString("No Property Marker");
        }
    }

    /**
     * Return the wraped object
     * 
     * @return the wraped object
     */
    public Object getJavaObject() {
        return javaObject;
    }

    /**
     * Return true if object must be considered as a java bean
     * 
     * @return true if bean
     */
    public boolean isBean() {
        return asBean;
    }

    // overrides
    @Override
    public ESObject getPrototype() {
        throw new ProgrammingError("Cannot get prototype of Wrapper");
    }

    // overrides
    @Override
    public String getESClassName() {
        return "Java Object";
    }

    // overrides
    @Override
    public int getTypeOf() {
        return EStypeObject;
    }

    // overrides
    // Get either a bean or an object property - for objects attempt bean access
    // if object access failed
    @Override
    public ESValue getPropertyInScope(String propertyName,
            ScopeChain previousScope, int hash) throws EcmaScriptException {
        ESValue value;
        // if (ESLoader.debugJavaAccess)
        // System.out.println("** Property searched in scope: " + propertyName);
        if (asBean) {
            value = getBeanProperty(propertyName);
            if (value != noPropertyMarker)
                return value; // found
        } else {
            value = getObjectProperty(propertyName);
            if (value == noPropertyMarker)
                value = getBeanProperty(propertyName);
            if (value == noPropertyMarker)
                value = getCorbaProperty(propertyName);
        }
        if (value == noPropertyMarker) {
            if (previousScope == null) {
                throw new ReferenceError("Variable '" + propertyName
                        + "' does not exist in the scope chain");
            }
            value = previousScope.getValue(propertyName, hash);

        }
        return value;
    }

    // Get either a bean or an object property - for objects attempt bean access
    // if object access failed
    @Override
    public ESValue getPropertyIfAvailable(String propertyName, int hash)
            throws EcmaScriptException {
        ESValue value;

        if (asBean) {
            // If declared as bean only examine using bean convention
            value = getBeanProperty(propertyName);
            if (value == noPropertyMarker) {
                return null;
            }
        } else {
            // Otherwise examine as java object, bean, or corba object
            value = getObjectProperty(propertyName);
            if (value == noPropertyMarker)
                value = getBeanProperty(propertyName);
            if (value == noPropertyMarker && javaObject instanceof Class<?>) {
                /*
                 * THIS CODE HAS BEEN REPLACED BY A HACK USING THE $ FORM OF THE
                 * NAME, SEE BELOW Class insideClasses[] = ((Class)
                 * javaObject).getDeclaredClasses(); //
                 * System.out.println("***** insideClasses.size: "
                 * +insideClasses.length); if (insideClasses.length>0) { throw
                 * newEcmaScriptException(
                 * "Handling of subclasses not implemented - sorry"); // return
                 * ESUndefined.theUndefined; } else { throw new
                 * EcmaScriptException("Subclass, field or property '" +
                 * propertyName + "' does not exists in class " + this +
                 * " - Subclass search not implemented in JDK 1.1"); //return
                 * ESUndefined.theUndefined; }
                 */
                // getDeclaredClasses is not implemented, use name to find
                // internal classes
                String className = ((Class<?>) javaObject).getName() + "$"
                        + propertyName;
                if (ESLoader.debugJavaAccess)
                    System.out
                            .println("** Check if inside class: " + className);
                Class<?> insideClass = null;
                try {
                    insideClass = Class.forName(className);
                } catch (ClassNotFoundException ex) {
                    throw new EcmaScriptException(
                            "Subclass, field or property '" + propertyName
                                    + "' does not exists in class " + this);
                    // return ESUndefined.theUndefined;
                }
                return new ESWrapper(insideClass, getEvaluator());
            }
            if (value == noPropertyMarker)
                value = getCorbaProperty(propertyName);
            if (value == noPropertyMarker) {
//                throw new EcmaScriptException("Field or property '"
//                        + propertyName + "' does not exists in object " + this);
                return null;
            }
        }

        return value;
    }

    /**
     * Get a bean property (static property of the class if applied to a Class
     * object)
     * 
     * @param propertyName
     *            the name of the property
     * 
     * @exception EcmaScriptException
     *                Error accessing the property value
     * @return an EcmaScript value
     */

    private ESValue getBeanProperty(String propertyName)
            throws EcmaScriptException {
        if (ESLoader.debugJavaAccess)
            System.out.println("** Bean property searched: " + propertyName);
        Class<? extends Object> cls = null;
        if (javaObject instanceof Class<?>) {
            cls = (Class<?>) javaObject;
        } else {
            cls = javaObject.getClass();
        }

        PropertyDescriptor descriptor = ClassInfo.lookupBeanField(propertyName,
                cls);
        if (descriptor == null) {
            return noPropertyMarker;
        }
        if (descriptor instanceof IndexedPropertyDescriptor) {
            Class<?> propCls = descriptor.getPropertyType();
            if (propCls == null)
                throw new EcmaScriptException("Bean property '" + propertyName
                        + "' does not have an array access method");
        }
        Method readMethod = descriptor.getReadMethod();
        if (readMethod == null) {
            throw new EcmaScriptException("No read method for property "
                    + propertyName);
        }
        if (ESLoader.debugJavaAccess)
            System.out.println("** Read method found for: " + propertyName);
        Object obj = null;
        try {
            obj = readMethod.invoke(javaObject, (Object[]) null);
        } catch (InvocationTargetException e) {
            throw new EcmaScriptException("Error int the getter for "
                    + propertyName, e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new EcmaScriptException("Access error invoking getter for "
                    + propertyName + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ProgrammingError(
                    "Inconsistent type of argument for property "
                            + propertyName + ": " + e.getMessage());
        }
        return ESLoader.normalizeValue(obj, getEvaluator());
    }

    /**
     * Get a corba property (use name of property as name of the routine)
     * 
     * @param propertyName
     *            the name of the property
     * 
     * @exception EcmaScriptException
     *                Error accessing the property value
     * @return an EcmaScript value
     */

    private ESValue getCorbaProperty(String propertyName)
            throws EcmaScriptException {
        if (ESLoader.debugJavaAccess)
            System.out.println("** CORBA property searched: " + propertyName);
        Class<? extends Object> cls = javaObject.getClass();
        Method readMethod = null;
        try {
            readMethod = cls.getMethod(propertyName, (Class[]) null);
            if (readMethod == null) {
                return noPropertyMarker;
            }
            Class<?> rt = readMethod.getReturnType();
            if (rt == null || rt == Void.TYPE
                    || readMethod.getParameterTypes().length != 0) {
                // If it is not a no argument value returning method, ignore
                return noPropertyMarker;
            }
        } catch (NoSuchMethodException ignore) {
            return noPropertyMarker;
        }

        if (ESLoader.debugJavaAccess)
            System.out.println("** CORBA read method found for: "
                    + propertyName);
        Object obj = null;
        try {
            obj = readMethod.invoke(javaObject, (Object[]) null);
        } catch (InvocationTargetException e) {
            throw new EcmaScriptException(
                    "Error in the CORBA getter function for " + propertyName, e
                            .getTargetException());
        } catch (IllegalAccessException e) {
            throw new EcmaScriptException(
                    "Access error invoking CORBA getter for " + propertyName
                            + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ProgrammingError(
                    "Inconsistent type of argument for property "
                            + propertyName + ": " + e.getMessage());
        }
        return ESLoader.normalizeValue(obj, getEvaluator());
    }

    /**
     * Get an object property (static property of the class if applied to a
     * Class object)
     * 
     * @param propertyName
     *            the name of the property
     * 
     * @exception EcmaScriptException
     *                Error accessing the property value
     * @return an EcmaScript value
     */
    private ESValue getObjectProperty(String propertyName)
            throws EcmaScriptException {
        if (ESLoader.debugJavaAccess)
            System.out
                    .println("** Java object field searched: " + propertyName);
        try {
            Class<? extends Object> cls = null;
            Object theObject = null; // means static
            if (javaObject instanceof Class<?>) {
                cls = (Class<?>) javaObject;
            } else {
                cls = javaObject.getClass();
                theObject = javaObject;
            }
            Field fld;
            try {
                if (theObject == null) {
                    fld = cls.getDeclaredField(propertyName); // static
                } else {
                    fld = cls.getField(propertyName);
                }
            } catch (NoSuchFieldException e) {
                return noPropertyMarker;
            }
            int modifiers = fld.getModifiers();

            /*
             * 17/5/2006 Graham Technology bug fix should be allowed to access
             * statics on an instance
             */
            if (theObject == null && !Modifier.isStatic(modifiers)) {
                throw new EcmaScriptException(
                        "Field mode (static) not correct for " + propertyName);
            }
            if (!Modifier.isPublic(modifiers)) {
                throw new EcmaScriptException("Field " + propertyName
                        + " not public");
            }
            Object obj = fld.get(theObject);
            return ESLoader.normalizeValue(obj, getEvaluator());
        } catch (IllegalAccessException e) {
            throw new EcmaScriptException("Cannot access java field "
                    + propertyName + " in " + this + ", error: " + e.toString());
        }
    }

    // overrides
    @Override
    public boolean isHiddenProperty(String propertyName, int hash) {
        return false;
    }

    // overrides
    // Put either a bean or an object property - for objects attempt bean access
    // if object access failed
    @Override
    public void putProperty(String propertyName, ESValue propertyValue, int hash)
            throws EcmaScriptException {
        if (propertyValue == ESUndefined.theUndefined) {
            throw new EcmaScriptException("Cannot set the field or property "
                    + propertyName
                    + " of a non EcmaScript object field to undefined");
        }

        if (asBean) {
            if (!putBeanProperty(propertyName, propertyValue)) {
                throw new EcmaScriptException("Cannot put value in property '"
                        + propertyName
                        + "' which does not exists in Java Bean '" + this + "'");
            }
        } else {
            if (putObjectProperty(propertyName, propertyValue))
                return;
            if (putBeanProperty(propertyName, propertyValue))
                return;
            if (putCorbaProperty(propertyName, propertyValue))
                return;

            throw new EcmaScriptException(
                    "Cannot put value in field or property '"
                            + propertyName
                            + "' which does not exists in Java or Corba object '"
                            + this + "'");
        }
    }

    /**
     * Put a property using the beans access functions.
     * 
     * @param propertyName
     *            the name of the property
     * @propertyValue the value of the property
     * @hash the hash code of the property name
     * @exception EcmaScriptException
     *                Error accessing the property value
     */
    private boolean putBeanProperty(String propertyName, ESValue propertyValue)
            throws EcmaScriptException {
        if (ESLoader.debugJavaAccess)
            System.out.println("** Bean property searched: " + propertyName);
        if (propertyValue == ESUndefined.theUndefined) {
            throw new ProgrammingError("Cannot set bean property "
                    + propertyName + " to undefined");
        }
        Class<? extends Object> cls = null;
        if (javaObject instanceof Class<?>) {
            cls = (Class<?>) javaObject;
        } else {
            cls = javaObject.getClass();
        }
        PropertyDescriptor descriptor = ClassInfo.lookupBeanField(propertyName,
                cls);
        if (descriptor == null) {
            return false;

        }
        Class<?> propClass = descriptor.getPropertyType();
        if (descriptor instanceof IndexedPropertyDescriptor) {
            if (propClass == null)
                throw new EcmaScriptException("Bean property '" + propertyName
                        + "' does not have an array access method");
        }
        Method writeMethod = descriptor.getWriteMethod();
        if (writeMethod == null) {
            throw new EcmaScriptException("No write method for Java property '"
                    + propertyName + "'");
        }
        if (ESLoader.debugJavaAccess)
            System.out.println("** Write method found for: " + propertyName);
        Object params[] = new Object[1];
        if (propClass.isArray()) {
            if (!(propertyValue.isArray())) {
                throw new EcmaScriptException(
                        "Argument should be Array for property '"
                                + propertyName + "'");
            }
            params[0] = ArrayUtil.arrayToJavaArray((ESObject) propertyValue,propClass.getComponentType());
        } else {
            params[0] = propertyValue.toJavaObject();
        }
        try {
            writeMethod.invoke(javaObject, params); // System will check
                                                    // consistency
        } catch (InvocationTargetException e) {
            throw new EcmaScriptException("Error in the setter for "
                    + propertyName, e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new EcmaScriptException("Access error invoking setter for "
                    + propertyName + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new EcmaScriptException(
                    "Type of argument not suitable for property "
                            + propertyName + ": " + e.getMessage());
        }
        if (ESLoader.debugJavaAccess)
            System.out.println("** Property set: " + propertyName);
        return true;
    }

    /**
     * Put a property using the CORBA convention.
     * 
     * @param propertyName
     *            the name of the property
     * @propertyValue the value of the property
     * @hash the hash code of the property name
     * @exception EcmaScriptException
     *                Error accessing the property value
     */
    private boolean putCorbaProperty(String propertyName, ESValue propertyValue)
            throws EcmaScriptException {
        if (ESLoader.debugJavaAccess)
            System.out.println("** Corba property searched: " + propertyName);
        if (propertyValue == ESUndefined.theUndefined) {
            throw new ProgrammingError("Cannot set non EcmaScript property "
                    + propertyName + " to undefined");
        }
        Object[] params = new Object[1];
        params[0] = propertyValue.toJavaObject();
        Method writeMethod = null;

        try {
            writeMethod = lookupMethod(propertyName, params, false);
            if (writeMethod == null) {
                // If it is not a no argument value returning method, ignore
                return false;
            }
            Class<?> rt = writeMethod.getReturnType();
            if (rt != null && rt == Void.class) {
                return false;
            }
        } catch (NoSuchMethodException ignore) {
            return false;
        }

        if (ESLoader.debugJavaAccess)
            System.out.println("** CORBA write method found for: "
                    + propertyName);

        try {
            writeMethod.invoke(javaObject, params); // System will check
                                                    // consistency
        } catch (InvocationTargetException e) {
            throw new EcmaScriptException("Error in the CORBA setter for "
                    + propertyName, e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new EcmaScriptException(
                    "Access error invoking CORBA setter for " + propertyName
                            + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new EcmaScriptException(
                    "Type of argument not suitable for CORBA property "
                            + propertyName + ": " + e.getMessage());
        }
        if (ESLoader.debugJavaAccess)
            System.out.println("** Property set: " + propertyName);
        return true;
    }

    /**
     * Put a property using the beans access functions. If not found and starts
     * by "on" assume that it is an event handler.
     * 
     * @param propertyName
     *            the name of the property
     * @propertyValue the value of the property
     * @hash the hash code of the property name
     * @exception EcmaScriptException
     *                Error accessing the property value
     */
    private boolean putObjectProperty(String propertyName, ESValue propertyValue)
            throws EcmaScriptException {
        if (ESLoader.debugJavaAccess)
            System.out.println("** Object field searched: " + propertyName);
        if (propertyValue == ESUndefined.theUndefined) {
            throw new ProgrammingError("Cannot set java object field "
                    + propertyName + " to undefined");
        }
        Object theObject = null; // means static
        Class<? extends Object> cls = null;
        if (javaObject instanceof Class<?>) {
            cls = (Class<?>) javaObject;
        } else {
            cls = javaObject.getClass();
            theObject = javaObject;
        }

        Field fld;
        try {
            if (theObject == null) {
                fld = cls.getDeclaredField(propertyName); // static
            } else {
                fld = cls.getField(propertyName); // include fields in
                                                  // superclass
            }
        } catch (NoSuchFieldException e) {
            return false;
        }

        int modifiers = fld.getModifiers();
        // if ((theObject == null) != Modifier.isStatic(modifiers)) {
        // throw new EcmaScriptException("Field mode (static) not correct for "+
        // propertyName);
        // }
        if (!Modifier.isPublic(modifiers)) {
            throw new EcmaScriptException("Field " + propertyName
                    + " not public");
        }

        try {
            fld.set(theObject, propertyValue.toJavaObject());
        } catch (IllegalArgumentException e) {
            throw new EcmaScriptException("Field " + propertyName + " of "
                    + this + " cannot be set with " + propertyValue
                    + ", error: " + e.toString());
        } catch (IllegalAccessException e) {
            throw new EcmaScriptException("Access error setting field "
                    + propertyName + " of " + this + ", error: " + e.toString());
        }
        return true;
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
        // Well, in fact it should use a hasProperty, but as
        // it is not well implemented...
        return false;
    }

    /**
     * indicates that the getProperties return an enumerator to the value itself
     * rather than to the index.
     * 
     * Return true
     */
    @Override
    public boolean isDirectEnumerator() {
        return true;
    }

    /**
     * If this is an enumerator, use it directly as an enumerator for "for in"
     * statements. It is a "bizare" enumerator, as it returns directly the
     * element to enumerate rather than its index in the object itself.
     * <P>
     * Otherwise returns an dummy enumerator.
     * 
     * @returns the enumerator or a dummy enumerator
     */
    @Override
    @SuppressWarnings("unchecked")
    public Enumeration<String> getProperties() {
        if (javaObject instanceof Enumeration<?>) {
            return (Enumeration<String>) javaObject;
        }
        // No visible properties supported - yet
        return new Enumeration<String>() {
            public boolean hasMoreElements() {
                return false;
            }

            public String nextElement() {
                throw new java.util.NoSuchElementException();
            }
        };

    }

    // No property list supported - yet
    @Override
    public Enumeration<String> getAllProperties() {
        return getProperties();
    }


    // overrides
    @Override
    public ESValue getDefaultValue(int hint) throws EcmaScriptException {
        if (hint == EStypeString) {
            return new ESString(javaObject.toString());
        }
        throw new EcmaScriptException("No default value for " + this
                + " and hint " + hint);

    }

    // overrides
    @Override
    public ESValue getDefaultValue() throws EcmaScriptException {
        return this.getDefaultValue(EStypeString);
    }

    /*
     * 2/5/2006 Graham Technology modification Made method protected to make
     * sub-classing easier
     */

    /**
     * Find a method
     * <P>
     * If static return null instead of exception in case of not found at all
     * 
     * @param functionName
     *            the name to find
     * @param params
     *            The list of parameters to filter by type
     * @staticMethod true if looking for a static method
     * @return the method (null if static and not found)
     * @exception NoSuchMethodException
     *                instance object and method not found
     * @exception EcmaScriptException
     *                various errors
     */
    protected Method lookupMethod(String functionName, Object[] params,
            boolean staticMethod) throws EcmaScriptException,
            NoSuchMethodException {

        int nArgs = params.length;
        if (ESLoader.debugJavaAccess)
            System.out.println("** " + (asBean ? "Bean" : "Class")
                    + " method lookup: " + (staticMethod ? "static " : "")
                    + functionName);
        Class<? extends Object> cls = null;
        if (staticMethod) {
            if (javaObject instanceof Class<?>) {
                cls = (Class<?>) javaObject;
            } else {
                throw new ProgrammingError(
                        "Cannot lookup for static method if not class");
            }
        } else {
            cls = javaObject.getClass();
        }
        Method[] methods = null;
        if (asBean) {
            methods = ClassInfo.lookupBeanMethod(functionName, cls);
        } else {
            methods = ClassInfo.lookupPublicMethod(functionName, cls);
        }
        if (methods == null || methods.length == 0) {
            if (staticMethod)
                return null; // A second try will be done
            throw new NoSuchMethodException("No method named '" + functionName
                    + "' found in " + this);
        }
        // To inform user if a method of proper name and attribte found but not
        // matching arguments
        boolean atLeastOneFoundWithAttributes = false;
        Method nearestMethodFound = null;
        CompatibilityDescriptor descriptorOfNearestMethodFound = null;
        int distanceOfNearestMethodFound = -1; // infinite
        boolean multipleAtSameDistance = false;
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (ESLoader.debugJavaAccess)
                System.out.println("** Method to validate: "
                        + method.toString());
            int modifiers = method.getModifiers();
            if (staticMethod && !Modifier.isStatic(modifiers))
                continue; // for static only
            atLeastOneFoundWithAttributes = true;
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != nArgs)
                continue;
            CompatibilityDescriptor cd = ESLoader.areParametersCompatible(
                    paramTypes, params);
            int distance = cd.getDistance();
            if (distance < 0)
                continue; // Method not applicable
            if (ESLoader.debugJavaAccess)
                System.out.println("** Method acceptable(" + distance + " : "
                        + Modifier.toString(modifiers) + " "
                        + methods[i].toString());
            // Optimization - if perfect match return immediately
            // Note that "perfect" match could be wrong if there is
            // a bug in the complex parameter matching algorithm,
            // so the optimization is not taken when debugging, to
            // allow to catch multiple "perfect match" which should not happen.
            if (distance == 0 && !ESLoader.debugJavaAccess) {
                cd.convert(params);
                return method; // success
            }
            // Imperfect match, keep the best one if multiple found
            if (nearestMethodFound == null) {
                // None so far
                nearestMethodFound = method;
                descriptorOfNearestMethodFound = cd;
                distanceOfNearestMethodFound = distance;
            } else {
                // Keep best - if identical we have a problem
                if (distance < distanceOfNearestMethodFound) {
                    nearestMethodFound = method;
                    descriptorOfNearestMethodFound = cd;
                    distanceOfNearestMethodFound = distance;
                    multipleAtSameDistance = false;
                } else if (distance == distanceOfNearestMethodFound) {
                    if (ESLoader.debugJavaAccess)
                        System.out
                                .println("** Same distance as previous method!");
                    if (distance != 0) {
                        // if 0 (due to debugging) accept any good one
                        multipleAtSameDistance = true;
                    }
                }
            }
        }
        if (nearestMethodFound != null  && descriptorOfNearestMethodFound != null) {
            if (multipleAtSameDistance) {
                throw new EcmaScriptException("Ambiguous method '"
                        + functionName + "' matching parameters in " + this);
            }
            descriptorOfNearestMethodFound.convert(params);
            return nearestMethodFound; // success
        }
        if (atLeastOneFoundWithAttributes) {
            throw new EcmaScriptException("No method '" + functionName
                    + "' matching parameters in " + this);
        }
        if (ESLoader.debugJavaAccess)
            System.out
                    .println("** Method rejected - did not match attribute or parameters");
        if (staticMethod)
            return null; // A second try will be done
        throw new EcmaScriptException("No method named '" + functionName
                + "' found in " + this);

    }

    // overrides
    @Override
    public ESValue doIndirectCall(Evaluator evaluator, ESObject target,
            String functionName, ESValue[] arguments)
            throws EcmaScriptException, NoSuchMethodException {
        int nArgs = arguments.length;
        if (ESLoader.debugJavaAccess)
            System.out.println("** Method searched: " + functionName
                    + " in object of class " + javaObject.getClass());
        Object[] params = new Object[nArgs];
        for (int k = 0; k < nArgs; k++) {
            if (arguments[k] == ESUndefined.theUndefined) {
                throw new EcmaScriptException(
                        "Cannot use undefined as parameter for java method "
                                + functionName + ", use 'null'");
            }
            params[k] = arguments[k].toJavaObject();
        }
        Method method = null;
        if (javaObject instanceof Class<?>) {
            // Will be null if no method of that name, error thrown if
            // method found but does not have compatible parameters. This
            // results in more helpful error message if a method is found
            // as static but does not have the expected parameter types.
            method = lookupMethod(functionName, params, true);
        }
        if (method == null) {
            method = lookupMethod(functionName, params, false);
        }
        Object obj = null;
        Class<?> retCls = method.getReturnType();
        try {
            obj = method.invoke(javaObject, params);
        } catch (InvocationTargetException e) {
            throw new EcmaScriptException("Error in java method "
                    + functionName, e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new EcmaScriptException("Access error invoking java method "
                    + functionName + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ProgrammingError(
                    "Inconsistent type of argument for method " + functionName
                            + ": " + e.getMessage());
        }
        ESValue eobj;
        if (retCls != Void.TYPE) {
            eobj = ESLoader.normalizeValue(obj, evaluator);
        } else {
            eobj = ESUndefined.theUndefined;
        }
        return eobj;
    }

    // overrides
    @Override
    public ESValue doIndirectCallInScope(Evaluator evaluator,
            ScopeChain previousScope, ESObject thisObject, String functionName,
            int hash, ESValue[] arguments) throws EcmaScriptException {
        if (ESLoader.debugJavaAccess)
            System.out
                    .println("** Method searched (indirect): " + functionName);
        try {
            return doIndirectCall(evaluator, thisObject, functionName,
                    arguments);
        } catch (NoSuchMethodException e) {
            if (previousScope == null) {
                throw new EcmaScriptException("no global function named '"
                        + functionName + "'");
            }
            return previousScope.doIndirectCall(evaluator, thisObject,
                    functionName, hash, arguments);

        }
    }

    // overrides
    @Override
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        return constructOrCall(arguments, true);
    }

    // overrides
    @Override
    public ESObject doConstruct(ESValue[] arguments)
            throws EcmaScriptException {
        return constructOrCall(arguments, false);
    }

    /**
     * Implements both new and function call on class objects (which create a
     * new object). The only difference is that elementary objects are not
     * normalized to EcmaScript objects if 'new' is used, so that the can be
     * operated upon as java object. Mostly useful for String
     * 
     * @param arguments
     *            the arguments
     * @param isCall
     *            true if call, false if new
     * 
     * @return the result of the new or call
     * @exception EcmaScriptException
     *                any exception during call
     */
    private ESObject constructOrCall(ESValue[] arguments, boolean isCall)
            throws EcmaScriptException {
        if (ESLoader.debugJavaAccess)
            System.out.println("** Constructor searched for: "
                    + javaObject.toString());
        if (javaObject instanceof Class<?>) {
            int nArgs = arguments.length;
            try {
                Class<?> cls = (Class<?>) javaObject;
                Constructor<?>[] constructors = cls.getConstructors();
                Object[] params = new Object[nArgs];
                for (int k = 0; k < nArgs; k++) {
                    if (arguments[k] == ESUndefined.theUndefined) {
                        throw new EcmaScriptException(
                                "Cannot use undefined as parameter for java constructor "
                                        + cls.toString());
                    }
                    params[k] = arguments[k].toJavaObject();
                }
                // To inform user if a constructor of proper name and attribute
                // found but not matching arguments
                boolean atLeastOneFoundWithAttributes = false;
                Constructor<?> nearestConstructorFound = null;
                CompatibilityDescriptor descriptorOfNearestConstructorFound = null;
                int distanceOfNearestConstructorFound = -1; // infinite
                boolean multipleAtSameDistance = false;
                for (int i = 0; i < constructors.length; i++) {
                    Constructor<?> constructor = constructors[i];
                    if (ESLoader.debugJavaAccess)
                        System.out.println("** Contructor examined: "
                                + constructor.toString());
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    int modifiers = constructor.getModifiers();
                    if (!Modifier.isPublic(modifiers))
                        continue;
                    atLeastOneFoundWithAttributes = true;
                    if (paramTypes.length != nArgs)
                        continue;
                    CompatibilityDescriptor cd = ESLoader
                            .areParametersCompatible(paramTypes, params);
                    int distance = cd.getDistance();
                    if (distance < 0)
                        continue; // Constructor not applicable
                    if (ESLoader.debugJavaAccess)
                        System.out.println("** Constructor acceptable("
                                + distance + " : "
                                + Modifier.toString(modifiers) + " "
                                + constructors[i].toString());
                    // Optimization - if perfect match return immediately
                    // Note that "perfect" match could be wrong if there is
                    // a bug in the complex parameter matching algorithm,
                    // so the optimization is not taken when debugging, to
                    // allow to catch multiple "perfect match" which should not
                    // happen.
                    if (distance == 0 && !ESLoader.debugJavaAccess) {
                        nearestConstructorFound = constructor;
                        descriptorOfNearestConstructorFound = cd;
                        distanceOfNearestConstructorFound = distance;
                        break; // success
                    }
                    // Imperfect match, keep the best one if multiple found
                    if (nearestConstructorFound == null) {
                        // None so far
                        nearestConstructorFound = constructor;
                        descriptorOfNearestConstructorFound = cd;
                        distanceOfNearestConstructorFound = distance;
                    } else {
                        // Keep best - if identical we have a problem
                        if (distance < distanceOfNearestConstructorFound) {
                            nearestConstructorFound = constructor;
                            descriptorOfNearestConstructorFound = cd;
                            distanceOfNearestConstructorFound = distance;
                            multipleAtSameDistance = false;
                        } else if (distance == distanceOfNearestConstructorFound) {
                            if (ESLoader.debugJavaAccess)
                                System.out
                                        .println("** Same distance as previous constructor!");
                            if (distance != 0) {
                                // if 0 (due to debugging) accept any good one
                                multipleAtSameDistance = true;
                            }
                        }
                    }
                }
                // We have found
                if (nearestConstructorFound != null && descriptorOfNearestConstructorFound != null) {
                    if (multipleAtSameDistance) {
                        throw new EcmaScriptException(
                                "Ambiguous constructor for "
                                        + javaObject.toString());
                    }
                    descriptorOfNearestConstructorFound.convert(params);
                    if (ESLoader.debugJavaAccess)
                        System.out.println("** Contructor called: "
                                + nearestConstructorFound.toString());
                    Object obj = null;
                    try {
                        obj = nearestConstructorFound.newInstance(params);
                    } catch (InvocationTargetException e) {
                        throw new EcmaScriptException("Error creating "
                                + javaObject + ": " + e.getTargetException());
                    } 
                    if (ESLoader.isBasicClass(cls) && !isCall) {
                        // Do not normalize if new basic class
                        return new ESWrapper(obj, getEvaluator()); 
                    }
                    return ESLoader.normalizeObject(obj, getEvaluator());

                }

                if (atLeastOneFoundWithAttributes) {
                    throw new EcmaScriptException(
                            "No constructor matching parameters in: " + this);
                }
                throw new EcmaScriptException("No public constructor in: "
                        + this);

            } catch (IllegalArgumentException e) {
                throwError(e);
            } catch (InstantiationException e) {
                throwError(e);
            } catch (IllegalAccessException e) {
                throwError(e);
            }
        }
        throw new EcmaScriptException("Not a java class: " + this);

    }

    private void throwError(Exception e)
            throws EcmaScriptException {
        throw new EcmaScriptException("Cannot build new " + this
                + ", error: " + e.toString());
    }

    // overrides
    @Override
    public double doubleValue() {
        double d = Double.NaN; // should check if doubleValue is present
        return d;
    }

    // overrides
    @Override
    public boolean booleanValue() {
        return true; // Should check if booleanValue is present
    }

    // overrides
    @Override
    public String toString() {
        return (javaObject == null) ? "<?Wrapper to null?>" : javaObject
                .toString();
    }

    // overrides
    @Override
    public Object toJavaObject() {
        return javaObject;
    }

    // public String getTypeofString() {
    // return "JavaObject";
    // }

    // Routines to describe this object

    // overrides
    @Override
    public String toDetailString() {
        if (asBean)
            return "ES:[BEAN:" + getESClassName() + ":" + javaObject.toString()
                    + "]";

        return "ES:[OBJ:" + getESClassName() + ":" + javaObject.toString()
                + "]";
    }

    static final int stepClass = 0;
    static final int stepConstructors = 1;
    static final int stepMethods = 2;
    static final int stepBeanMethods = 3;
    static final int stepFields = 4;
    static final int stepBeanProperties = 5;
    static final int stepEvents = 6;
    static final int stepNoMore = 7;

    EventSetDescriptor[] getEvents(Class<? extends Object> cls) {
        EventSetDescriptor[] eds = null;
        BeanInfo bi = null;
        try {
            bi = Introspector.getBeanInfo(cls);
        } catch (IntrospectionException e) {
            // ignore events
        }
        if (bi != null)
            eds = bi.getEventSetDescriptors();
        if (eds == null) {
            eds = new EventSetDescriptor[0];
        }
        return eds;
    }

    // overrides
    @Override
    public Enumeration<ValueDescription> getAllDescriptions() {
        return new Enumeration<ValueDescription>() {
            Class<? extends Object> clazz = javaObject.getClass();
            int step = stepClass;
            Constructor<?>[] constructors = clazz.getConstructors();
            Method[] methods = (asBean ? new Method[0] : clazz.getMethods());
            Field[] fields = (asBean ? new Field[0] : clazz.getFields());
            PropertyDescriptor[] beanProperties = getBeanPropertyDescriptors();
            MethodDescriptor[] beanMethods = getBeanMethodDescriptors();
            EventSetDescriptor[] events = ESWrapper.this.getEvents(clazz);
            int index = 0;

            private PropertyDescriptor[] getBeanPropertyDescriptors() {
                PropertyDescriptor[] bean_properties = new PropertyDescriptor[0];
                if (ESWrapper.this.asBean) {
                    try {
                        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
                        bean_properties = beanInfo.getPropertyDescriptors();
                    } catch (Exception e) {
                        // ignore
                    }
                } else {
                    // Only return them if different from fields...
                    PropertyDescriptor[] properties = null;
                    try {
                        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
                        properties = beanInfo.getPropertyDescriptors();
                    } catch (Exception e) {
                        // ignore
                    }
                    if (properties != null) {
                        Field[] fields = clazz.getFields();
                        // This is pretty uggly...
                        int remainingProps = 0;
                        for (int iProps = 0; iProps < properties.length; iProps++) {
                            String propName = properties[iProps].getName();
                            for (int iField = 0; iField < fields.length; iField++) {
                                String fieldName = fields[iField].getName();
                                if (propName.equals(fieldName)) {
                                    properties[iProps] = null;
                                    break;
                                }
                            }
                            if (properties[iProps] != null)
                                remainingProps++;
                        }
                        if (remainingProps > 0) {
                            bean_properties = new PropertyDescriptor[remainingProps];
                            int insert = 0;
                            for (int iProps = 0; iProps < properties.length; iProps++) {
                                if (properties[iProps] != null)
                                    bean_properties[insert++] = properties[iProps];
                            }
                        }
                    }
                }
                return bean_properties;
            }

            private MethodDescriptor[] getBeanMethodDescriptors() {
                MethodDescriptor[] bean_methods = new MethodDescriptor[0];
                if (ESWrapper.this.asBean) {
                    try {
                        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
                        bean_methods = beanInfo.getMethodDescriptors();
                    } catch (Exception e) {
                        // ignore
                    }
                }
                return bean_methods;
            }

            public boolean hasMoreElements() {
                if (step == stepClass)
                    return true;
                if (step == stepConstructors) {
                    if (constructors.length > index)
                        return true;
                    step++;
                    index = 0;
                }
                if (step == stepMethods) {
                    if (methods.length > index)
                        return true;
                    step++;
                    index = 0;
                }
                if (step == stepBeanMethods) {
                    if (beanMethods.length > index)
                        return true;
                    step++;
                    index = 0;
                }
                if (step == stepFields) {
                    if (fields.length > index)
                        return true;
                    step++;
                    index = 0;
                }
                if (step == stepBeanProperties) {
                    if (beanProperties.length > index)
                        return true;
                    step++;
                    index = 0;
                }
                if (step == stepEvents) {
                    if (events.length > index)
                        return true;
                    step++;
                    index = 0;
                }
                return false;
            }

            public ValueDescription nextElement() {
                if (hasMoreElements()) {
                    switch (step) {
                    case stepClass: {
                        step++;
                        if (asBean) {
                            return new ValueDescription("BEAN",
                                    describe_class_or_interface(clazz));
                        }
                        return new ValueDescription("CLASS",
                                describe_class_or_interface(clazz));

                    }
                    case stepConstructors: {
                        if (asBean) {
                            String info = "[[error]]";
                            index++;
                            try {
                                BeanInfo beanInfo = Introspector
                                        .getBeanInfo(clazz);
                                BeanDescriptor beanDescriptor = beanInfo
                                        .getBeanDescriptor();
                                info = beanDescriptor.getName() + " ("
                                        + beanDescriptor.getShortDescription()
                                        + ")";
                            } catch (Exception e) {
                                info += e.getMessage();
                            }
                            return new ValueDescription("BEANINFO", info);
                        }
                        return new ValueDescription(
                                "CONSTR",
                                describe_method_or_constructor(constructors[index++]));

                    }
                    case stepMethods: {
                        return new ValueDescription(
                                "FUNC",
                                describe_method_or_constructor(methods[index++]));
                    }
                    case stepBeanMethods: {
                        return new ValueDescription("METHOD",
                                describe_bean_method(beanMethods[index++]));
                    }
                    case stepFields: {
                        return new ValueDescription("FIELD", describe_field(
                                fields[index++], javaObject));
                    }
                    case stepBeanProperties: {
                        return new ValueDescription("PROPS",
                                describe_bean_property(beanProperties[index++],
                                        javaObject));
                    }
                    case stepEvents: {
                        return new ValueDescription("EVENT",
                                describe_event(events[index++]));
                    }
                    } // switch
                    throw new ProgrammingError("Inconsistent step");
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
        return new ValueDescription(name, "JAVAOBJ", this.toString());
    }

    /** Return the name of an interface or primitive type, handling arrays. */
    private static String typename(Class<?> t) {
        StringBuilder brackets = new StringBuilder("");
        while (t.isArray()) {
            brackets.append("[]");
            t = t.getComponentType();
        }
        return t.getName() + brackets;
    }

    /** Return a string version of modifiers, handling spaces nicely. */
    private static String modifiers(int m) {
        if (m == 0)
            return "";
        return Modifier.toString(m) + " ";
    }

    /** describe the modifiers, type, and name of a field */
    static String describe_field(Field f, Object obj) {
        String s = modifiers(f.getModifiers()) + typename(f.getType()) + " "
                + f.getName();
        s += " = " + obj.toString();

        return s + ";";
    }

    /** describe the modifiers, type, and name of a field */
    static String describe_bean_property(PropertyDescriptor d, Object obj) {
        String a = (d instanceof IndexedPropertyDescriptor) ? "[]" : "";
        String s = d.getName() + a + " (" + d.getShortDescription() + ")";
        Method readMethod = d.getReadMethod();
        if (readMethod != null) {
            try {
                Object v = readMethod.invoke(obj, (Object[]) null);
                s += " = " + v.toString();
            } catch (InvocationTargetException ignore) {
                // do nothing
            } catch (IllegalAccessException ignore) {
                // do nothing
            } catch (IllegalArgumentException ignore) {
                // do nothing
            }
        }
        return s + ";";
    }

    /**
     * Describe the modifiers, return type, name, parameter types and exception
     * type of a method or constructor. Note the use of the Member interface to
     * allow this method to work with both Method and Constructor objects
     */
    static String describe_method_or_constructor(Member member) {
        Class<?> returntype = null, parameters[], exceptions[];
        StringBuilder buffer = new StringBuilder();
        if (member instanceof Method) {
            Method m = (Method) member;
            returntype = m.getReturnType();
            parameters = m.getParameterTypes();
            exceptions = m.getExceptionTypes();
        } else {
            Constructor<?> c = (Constructor<?>) member;
            parameters = c.getParameterTypes();
            exceptions = c.getExceptionTypes();
        }

        buffer.append(modifiers(member.getModifiers()));
        if (returntype != null) {
            buffer.append(typename(returntype)).append(' ');
        }
        buffer.append(member.getName()).append('(');
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0)
                buffer.append(", ");
            buffer.append(typename(parameters[i]));
        }
        buffer.append(')');
        if (exceptions.length > 0)
            buffer.append(" throws ");
        for (int i = 0; i < exceptions.length; i++) {
            if (i > 0)
                buffer.append(", ");
            buffer.append(typename(exceptions[i]));
        }
        buffer.append(';');
        return buffer.toString();
    }

    /**
     * Describe the modifiers, return type, name, parameter types and exception
     * type of a bean method
     */
    static String describe_bean_method(MethodDescriptor descriptor) {
        Class<?> returntype = null, parameters[], exceptions[];
        StringBuilder buffer = new StringBuilder();
        Method method = descriptor.getMethod();
        returntype = method.getReturnType();
        parameters = method.getParameterTypes();
        exceptions = method.getExceptionTypes();

        buffer.append(descriptor.getName()).append(": ").append(
                modifiers(method.getModifiers()));
        if (returntype != null) {
            buffer.append(typename(returntype)).append(' ');
        }
        buffer.append(method.getName()).append('(');
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0)
                buffer.append(", ");
            buffer.append(typename(parameters[i]));
        }
        buffer.append(')');
        if (exceptions.length > 0)
            buffer.append(" throws ");
        for (int i = 0; i < exceptions.length; i++) {
            if (i > 0)
                buffer.append(", ");
            buffer.append(typename(exceptions[i]));
        }
        buffer.append(';');
        return buffer.toString();
    }

    static String describe_class_or_interface(Class<? extends Object> c) {
        StringBuilder buffer = new StringBuilder();
        // Print modifiers, type (class or interface), name and superclass.
        if (c.isInterface()) {
            // The modifiers will include the "interface" keyword here...
            buffer.append(Modifier.toString(c.getModifiers())).append(' ')
                    .append(c.getName());
        } else if (c.getSuperclass() != null) {
            buffer.append(Modifier.toString(c.getModifiers()))
                    .append(" class ").append(c.getName()).append(" extends ")
                    .append(c.getSuperclass().getName());
        } else {
            buffer.append(Modifier.toString(c.getModifiers()))
                    .append(" class ").append(c.getName());
        }

        // Print interfaces or super-interfaces of the class or interface.
        Class<?>[] interfaces = c.getInterfaces();
        if ((interfaces != null) && (interfaces.length > 0)) {
            if (c.isInterface())
                buffer.append(" extends ");
            else
                buffer.append(" implements ");
            for (int i = 0; i < interfaces.length; i++) {
                if (i > 0)
                    buffer.append(", ");
                buffer.append(interfaces[i].getName());
            }
        }
        return buffer.toString();
    }

    static String describe_event(EventSetDescriptor event) {
        Class<?> eventClass = event.getListenerType();
        return event.getName() + " " + eventClass.getName();
    }
}
