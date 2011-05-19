// JSGlobalObject.java
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

package FESI.jslib;

/**
 * Interface used to represent the GlobalObject wrapper of the interpreter. The
 * global object is used for functions which require the evaluator. It is
 * possible to get it from any JSObject.
 * <P>
 * This interface is exported by FESI objects, it is not intended or useful for
 * user objects to extend this interface.
 */

public interface JSGlobalObject extends JSObject {

    /**
     * Mark an object as a bean, restricting its access by FESI scripts to the
     * public bean methods and properties.
     * 
     * @param object
     *            The object to wrap as a bean.
     * 
     * @return an opaque object that can be passed to FESI and behave as a Bean.
     */
    public Object makeBeanWrapper(Object object);

    /**
     * Given an object that was created by makeBeanWrapper, returned the wrapped
     * bean. Note that most JSxxx routine already return an unwrap value if
     * possible.
     * 
     * @param object
     *            an object created by makeBeanWrapper
     * 
     * @return the wrapped object
     * @exception JSException
     *                If the object was not a wrapped bean
     */
    public Object getWrappedBean(Object object) throws JSException;

    /**
     * Make a new EcmaScript object based the object prototype object. The
     * object is of class Object and has initially no property.
     * 
     * @return A new EcmaScript object
     * 
     */
    public JSObject makeJSObject();

    /**
     * Make a new EcmaScript object based on a given prototype (which may be
     * null). The object is of class Object and has initially no property.
     * 
     * @param prototype
     *            An object to use as prototype for this object
     * @return A new EcmaScript object
     */
    public JSObject makeJSObject(JSObject prototype);

    /**
     * Package any object as an EcmaScript object, allowing to use it for
     * example with an "eval" function, where it becomes the 'this' object.
     * 
     * @param object
     *            The object to wrap.
     * @return The EcmaScript object that wrap the original object.
     */
    public JSObject makeObjectWrapper(Object object);

    /**
     * Given an object that was created by makeObjectWrapper, returned the
     * wrapped bean. Note that most JSxxx routine already return an unwrap value
     * if possible
     * 
     * @param object
     *            an object created by makeJSObject
     * 
     * @returnt the original object.
     * @exception JSException
     *                If the object was not a wrapped object
     */
    public Object getWrappedObject(JSObject object) throws JSException;

    /**
     * Make a new array object. The object is of class Array and is empty
     * (length 0).
     * 
     * @return A new EcmaScript object thatis an Array
     */
    public JSObject makeJSArrayObject();

}
