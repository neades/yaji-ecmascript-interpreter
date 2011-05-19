// JSGlobalWrapper.java
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

import FESI.Exceptions.ProgrammingError;
import FESI.Interpreter.Evaluator;
import FESI.jslib.JSException;
import FESI.jslib.JSGlobalObject;
import FESI.jslib.JSObject;

/**
 * Package an EcmaScript object as a JSObject for use by the
 * Netscape like interface. Specialled for the global object.
 */
public class JSGlobalWrapper extends JSWrapper implements JSGlobalObject {
    
    /**
     * Create a JSGlobalWrapper object over the global object
     * @param go the EcmaScript global object
     * @param evaluator the Evaluator
     */
     
    public JSGlobalWrapper(GlobalObject go, Evaluator evaluator) {
       super(go, evaluator);
    }
   
      
   /**
     * Package any object as an EcmaScript object, allowing to use
     * it for example with an "eval" function, where it becomes the
     * 'this' object.
     *
     * @param object The object to wrap.
     * @return A JSObject that can be pass to FESI to represent the wrapped object.
     */
    public JSObject makeObjectWrapper(Object object) {
       synchronized (evaluator) {
           if (object instanceof JSWrapper) {
               return (JSWrapper) object; // Already a JSObject
           }
           if (object instanceof ESWrapper) { 
               // A java object wrapped as an ecmascript object
               ESWrapper eswrapper = (ESWrapper) object;
               // Just wrap it for the JS interface
               return new JSWrapper(eswrapper,evaluator);
           }
           // Any native java object - make it an internal ES object, then wrap it
           // for the public interface
           ESWrapper eswrapper =  new ESWrapper(object, evaluator);
           return new JSWrapper(eswrapper,evaluator);
       }
   }

   /**
      * Given an object that was created by makeObjectWrapper, returned
      * the wrapped bean.
      * 
      * @param object an object created by makeObjectWrapper
      * 
      * @return the wrapped object
      * @exception JSException If the object was not a wrapped bean
      */
     public Object getWrappedObject(JSObject object) throws JSException {
         synchronized (evaluator) {
             if (object == null) throw new NullPointerException("getWrappedObject(null)");
             if (object instanceof JSWrapper) {
                 JSWrapper jswrapper = (JSWrapper) object;
                 ESObject esobject = jswrapper.getESObject();
                 if (esobject != null && esobject instanceof ESWrapper) {
                     ESWrapper eswrapper = (ESWrapper) esobject;
                     return eswrapper.getJavaObject();
                 } else {
                     throw new JSException("getWrappedObject call on an object not created by makeWrappedObject");
                 }
             } else {
                 throw new JSException("getWrappedObject call on an object not created by makeWrappedObject");
             }
         }    
     }
    
   /**
     * Mark an object as a bean, restricting its access by FESI scripts
     * to the public bean methods and properties.
     *
     * @param object The object to wrap as a bean.
     * @return An internal object that can be passed to FESI to represent the wrapped bean.
     */
    public Object makeBeanWrapper(Object object) {
       synchronized (evaluator) {
           if (object instanceof ESWrapper) {
               ESWrapper eswrapper = (ESWrapper) object;
               if (eswrapper.isBean()) {
                   return eswrapper;
               } else {
                   return new ESWrapper(eswrapper.getJavaObject(), eswrapper.getEvaluator(), true);
               }
           } else {
               return new ESWrapper(object, evaluator, true);
           }
       }
   }

   /**
     * Given an object that was created by makeBeanWrapper, returned
     * the wrapped bean.
     * 
     * @param object an object created by makeBeanWrapper
     * 
     * @return the wrapped object
   * @exception JSException If the object was not a wrapped bean
     */
    public Object getWrappedBean(Object object) throws JSException {
        if (object == null) throw new NullPointerException("getWrappedBean(null)");
        synchronized (evaluator) {
            if (object instanceof ESWrapper) {
                ESWrapper eswrapper = (ESWrapper) object;
                if (eswrapper.isBean()) {
                    return eswrapper.getJavaObject();
                } else {
                    throw new JSException("getWrappedBean call on an object not created by makeWrappedBean");
                }
            } else {
                throw new JSException("getWrappedBean call on an object not created by makeWrappedBean");
            }
        }    
    }

   
    /** 
     * Make a new object based on a given prototype (which may be null).
     * The object is of class Object and has initially no property.
     *
     * @return A new object
     */
    public JSObject makeJSObject() {
        return makeJSObject(null);
    }
    
    /** 
     * Make a new object based the object prototype object.
     * The object is of class Object and has initially no property.
     *
     * @param prototype An object to use as prototype for this object
     * @return A new object
     */
    public JSObject makeJSObject(JSObject prototype) {
        synchronized (evaluator) {
            ESObject op = evaluator.getObjectPrototype();
            if (prototype != null) {
                Evaluator otherEvaluator = ((JSWrapper) prototype).evaluator;
                if (otherEvaluator != evaluator) throw new ProgrammingError("Evaluator mismatch");
                op = (ESObject) ((JSWrapper) prototype).getESObject();
            }
            ESObject object = new ObjectPrototype((ESObject) op, evaluator);
            return new JSWrapper(object, evaluator);
        }
    }
    
    /** 
     * Make a new array object.
     * The object is of class Array and is empty (length 0).
     *
     * @return A new object
     */
    public JSObject makeJSArrayObject() {
        synchronized (evaluator) {
            ESObject ap = evaluator.getArrayPrototype();
            ArrayPrototype theArray = new ArrayPrototype(ap, evaluator);
            return new JSWrapper(theArray, evaluator);
        }
    }
    


    /**
     * Display the string value of the contained object
     * @return The string value
     */
    public String toString() {
        return object.toString();
    }
}