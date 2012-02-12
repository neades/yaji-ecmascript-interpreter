// ArrayPrototype.java
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

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yaji.json.JsonState;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.ProgrammingError;
import FESI.Exceptions.RangeError;
import FESI.Exceptions.ReferenceError;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.ScopeChain;

/**
 * Implements the prototype and is the class of all Array objects
 */
public class ArrayPrototype extends ESObject {
    private static final long serialVersionUID = 2559830243680989945L;
    
    private enum IterationType {
        EVERY, SOME, FOREACH, MAP, FILTER;
    }

    protected ArrayList<ESValue> theArray = new ArrayList<ESValue>();

    /**
     * Create a new empty array
     * 
     * @param prototype
     *            the ArrayPrototype
     * @param evaluator
     *            The evaluator
     * @throws EcmaScriptException 
     */
    public ArrayPrototype(ESObject prototype, Evaluator evaluator) throws EcmaScriptException {
        super(prototype, evaluator);
        putProperty(StandardProperty.LENGTHstring, WRITEABLE, ESNumber.valueOf(0));
    }

    // overrides
    @Override
    public String getESClassName() {
        return StandardProperty.ARRAYstring;
    }
    
    @Override
    public boolean isArray() {
        return true;
    }
    
    public void add(ESValue v) throws EcmaScriptException {
        theArray.add(v);
        updateLength(theArray.size());
    }

    /**
     * Return a Java array object which is the object to pass to Java routines
     * called by FESI.
     * 
     * @param componentType
     *            the type of the component of the array
     * @return a java array object
     */
    public Object toJavaArray(Class<?> componentType)
            throws EcmaScriptException {
        int l = size();
        Object array = Array.newInstance(componentType, l);
        if (l == 0) {
            return array;
        }
        for (int i = 0; i < l; i++) {
            ESValue element = theArray.get(i);
            if (componentType == Integer.TYPE) {
                if (element.isNumberValue()) {
                    double d = element.doubleValue();
                    int value = (int) d;
                    if (value != d) {
                        throw new EcmaScriptException("An element (" + element
                                + ") of array is too large for class "
                                + componentType);
                    }
                    Array.setInt(array, i, value);
                } else {
                    throw new EcmaScriptException("An element (" + element
                            + ") of array cannot be converted to class "
                            + componentType);
                }
            } else if (componentType == Short.TYPE) {
                if (element.isNumberValue()) {
                    double d = element.doubleValue();
                    short value = (short) d;
                    if (value != d) {
                        throw new EcmaScriptException("An element (" + element
                                + ") of array is too large for class "
                                + componentType);
                    }
                    Array.setShort(array, i, value);
                } else {
                    throw new EcmaScriptException("An element (" + element
                            + ") of array cannot be converted to class "
                            + componentType);
                }
            } else if (componentType == Byte.TYPE) {
                if (element.isNumberValue()) {
                    double d = element.doubleValue();
                    byte value = (byte) d;
                    if (value != d) {
                        throw new EcmaScriptException("An element (" + element
                                + ") of array is too large for class "
                                + componentType);
                    }
                    Array.setByte(array, i, value);
                } else {
                    throw new EcmaScriptException("An element (" + element
                            + ") of array cannot be converted to class "
                            + componentType);
                }
            } else if (componentType == Long.TYPE) {
                if (element.isNumberValue()) {
                    double d = element.doubleValue();
                    long value = (long) d;
                    if (value != d) {
                        throw new EcmaScriptException("An element (" + element
                                + ") of array is too large for class "
                                + componentType);
                    }
                    Array.setLong(array, i, value);
                } else {
                    throw new EcmaScriptException("An element (" + element
                            + ") of array cannot be converted to class "
                            + componentType);
                }
            } else if (componentType == Float.TYPE) {
                if (element.isNumberValue()) {
                    double d = element.doubleValue();
                    float value = (float) d;
                    if (value != d) {
                        throw new EcmaScriptException("An element (" + element
                                + ") of array is too large for class "
                                + componentType);
                    }
                    Array.setFloat(array, i, value);
                } else {
                    throw new EcmaScriptException("An element (" + element
                            + ") of array cannot be converted to class "
                            + componentType);
                }
            } else if (componentType == Double.TYPE) {
                if (element.isNumberValue()) {
                    double d = element.doubleValue();
                    Array.setDouble(array, i, d);
                } else {
                    throw new EcmaScriptException("An element (" + element
                            + ") of array cannot be converted to class "
                            + componentType);
                }
            } else if (componentType == Boolean.TYPE) {
                if (element.isBooleanValue()) {
                    boolean b = element.booleanValue();
                    Array.setBoolean(array, i, b);
                } else {
                    throw new EcmaScriptException("An element (" + element
                            + ") of array cannot be converted to class "
                            + componentType);
                }
            } else if (componentType == Character.TYPE) {
                if (element.isStringValue()) {
                    String s = element.toString();
                    if (s.length() != 1) {
                        throw new EcmaScriptException(
                                "A string ("
                                        + element
                                        + ") of array is not of size 1 for conversion to Character");
                    }
                    Array.setChar(array, i, s.charAt(0));
                } else {
                    throw new EcmaScriptException("An element (" + element
                            + ") of array cannot be converted to class "
                            + componentType);
                }
            } else {
                Object o;
                if (element instanceof ArrayPrototype) {
                    // HACK FUR NOW, USE Object arrays, could get away with it!
                    o = ((ArrayPrototype) element).toJavaArray(Object.class);
                } else {
                    o = element.toJavaObject();
                }
                if (o == null) {
                    Array.set(array, i, null);
                } else {
                    Class<? extends Object> sourceClass = o.getClass();
                    if (componentType.isAssignableFrom(sourceClass)) {
                        Array.set(array, i, o);
                    } else {
                        throw new EcmaScriptException("An element (" + element
                                + ") of array cannot be converted to class "
                                + componentType);
                    }
                }
            }
        }
        return array;
    }

    // overrides
    @Override
    public String toDetailString() {
        return "ES:[" + getESClassName() + ":" + this.getClass().getName()
                + "]";
    }

    /**
     * Return the size of the array
     * 
     * @return the size as an int
     */
    public int size() {
        try {
            return getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash).toInt32();
        } catch (EcmaScriptException e) {
            return 0;
        }
    }

    /**
     * Set the size of the array, truncating if needed
     * 
     * @param size
     *            the new size 90 or positive)
     * @throws EcmaScriptException 
     */
    public void setSize(int size) throws EcmaScriptException {
        int currentSize = theArray.size();
        if (size > currentSize) {
            theArray.ensureCapacity(size);
            theArray.addAll(currentSize,
                    Arrays.asList(new ESValue[size - currentSize]));
        } else {
            // Truncate
            theArray.subList(size, theArray.size()).clear();
            theArray.trimToSize();
        }
        updateLength(size);
    }

    private void updateLength(int size) throws EcmaScriptException {
        super.putProperty(StandardProperty.LENGTHstring, ESNumber.valueOf(size), StandardProperty.LENGTHhash);
    }

    /**
     * Set the value of a specific element
     * 
     * @param theElement
     *            the new element value
     * @param index
     *            the index of the element
     */
    public void setElementAt(ESValue theElement, int index) {
        theArray.set(index, theElement);
    }

    /**
     * ES5 - 15.4.4.8 
     * Array.prototype.reverse ( )
     * 
     * The elements of the array are rearranged so as to reverse their order.
     * The object is returned as the result of the call.
     * 
     * @return the reversed array (which is the same as this one)
     * @throws EcmaScriptException
     */
    public ESValue reverse() {
        if (theArray.size() > 1) {
            Collections.reverse(theArray);
        }
        return this;
    }

    /**
     * ES5 - 15.4.4.7 
     * Array.prototype.push ( [ item1 [ , item2 [ , � ] ] ] )
     * 
     * The arguments are appended to the end of the array, in the order in which
     * they appear. The new length of the array is returned as the result of the
     * call.
     * 
     * @param args
     * @return
     * @throws EcmaScriptException 
     */
    public ESValue push(ESValue[] args) throws EcmaScriptException {
        int size = size();
        setSize(size+args.length);
        for (ESValue v : args) {
            theArray.set(size++,v);
        }
        return ESNumber.valueOf(size());
    }
    
    /**
     * ES5 - 15.4.4.6
     * Array.prototype.pop ( )
     * 
     * The last element of the array is removed from the array and returned.
     * 
     * @return
     * @throws EcmaScriptException 
     */
    public ESValue pop() throws EcmaScriptException {
        int len = theArray.size();
        if (len == 0) {
            return ESUndefined.theUndefined;
        }
        updateLength(len-1);
        return theArray.remove(len-1);
    }
    
    /**
     * ES5 - 15.4.4.9
     * Array.prototype.shift ( )
     * 
     * The first element of the array is removed from the array and returned.
     * 
     * @return
     * @throws EcmaScriptException 
     */
    public ESValue shift() throws EcmaScriptException {
        if (theArray.size() == 0) {
            return ESUndefined.theUndefined;
        }
        updateLength(theArray.size()-1);
        return theArray.remove(0);
    }

    /**
     * ES5 - 15.4.4.13 
     * Array.prototype.unshift ( [ item1 [ , item2 [ , � ] ] ] )
     * 
     * The arguments are prepended to the start of the array, such that their
     * order within the array is the same as the order in which they appear in
     * the argument list.
     * 
     * @param args
     * @return
     * @throws EcmaScriptException 
     */
    public ESValue unshift(ESValue[] args) throws EcmaScriptException {
       int i = 0;
       for (ESValue v : args) {
           theArray.add(i++, v);
       }
       updateLength(theArray.size());
       return ESNumber.valueOf(theArray.size());
    }
    
    /**
     * ES5 - 15.4.4.10
     * Array.prototype.slice (start, end)
     * 
     * Returns a one-level deep copy of a portion of an array.
     * 
     * @param args
     * @return
     * @throws EcmaScriptException
     */
    public ESValue slice(ESValue[] args) throws EcmaScriptException {
        int begin, end;
        if (args.length == 0) {
            begin = 0;
            end = theArray.size();
        } else {
            begin = ((ESNumber) args[0]).toInt32();
            end = args.length == 1 ? args.length : ((ESNumber) args[1]).toInt32();
        }
        ArrayPrototype newArray = newEmptyArray();
        newArray.theArray.addAll(theArray.subList(begin, end));
        newArray.updateLength(newArray.theArray.size());
        return newArray;
    }

    /**
     * ES5 - 15.4.4.12 
     * Array.prototype.splice (start, deleteCount [, item1 [,item2 [ ... ] ] )
     * 
     * Changes the content of an array, adding new elements while removing old
     * elements.
     * 
     * TODO: Optimise
     * 
     * @param args
     * @return
     * @throws EcmaScriptException
     */
    public ESValue splice(ESValue[] args) throws EcmaScriptException {
        ArrayPrototype newArray = newEmptyArray();

        if (args.length >= 2) {
            int len = theArray.size();
            
            int relStart = ((ESNumber) args[0]).toInt32();
            int start = relStart < 0 ? Math.max(len + relStart, 0) : Math.min(
                    relStart, len);
            
            int delCount = Math.min(
                    Math.max(((ESNumber) args[1]).toInt32(), 0), len - start);

            int i;
            for (i = 0; i < delCount; i++) {
                ESValue v = theArray.get(start + i);
                if (v != null) {
                    newArray.theArray.add(v);
                }
            }
           
            int itemCount = args.length - 2;
            if (itemCount < delCount) {
                for (i = start; i < (len - delCount); i++) {
                    int to = i + itemCount;
                    ESValue v = theArray.get(i + delCount);
                    if (v != null) {
                        if (to >= theArray.size()) {
                            theArray.add(v);
                        } else {
                            theArray.set(to, v);
                        }
                    } else {
                        theArray.remove(to);
                    }
                }
                
                for (i = len; i > (len - delCount + itemCount); i--) {
                    theArray.remove(i-1);
                }
            } else if (itemCount > delCount) {
                for (i = len - delCount; i > start; i--) {
                    int to = i + itemCount - 1;
                    ESValue v = theArray.get(i + delCount - 1);
                    if (v != null) {
                        if (to >= theArray.size()) {
                            theArray.add(v);
                        } else {
                            theArray.set(to, v);
                        }
                    } else {
                        theArray.remove(to);
                    }
                }
            }
            
            for (i = 2; i < args.length; i++) {
                theArray.set(start++, args[i]);
            }
            
            updateLength(theArray.size());
            newArray.updateLength(newArray.theArray.size());
        }
        return newArray;
    }
    
    /**
     * ES5 - 15.4.4.14 
     * Array.prototype.indexOf ( searchElement [ , fromIndex ] )
     * 
     * Returns the first index at which a given element can be found in the
     * array, or -1 if it is not present.
     * 
     * @param args
     * @return
     * @throws EcmaScriptException
     */
    public ESValue indexOf(ESValue[] args) throws EcmaScriptException {
        int len = theArray.size();
        if (len != 0) {
            int offset = args.length >= 2 ? ((ESNumber) args[1]).toInt32() : 0;
            if (offset < len) {
                int index;
                if (offset >= 0) {
                    index = offset;
                } else {
                    index = len - Math.abs(offset);
                    if (index < 0) {
                        index = 0;
                    }
                }
                
                while (index < len) {
                    if (theArray.get(index).equals(args[0])) {
                        return ESNumber.valueOf(index);
                    }
                    index++;
                }
            }
        }
        return ESNumber.valueOf(-1);
    }

    /**
     * 15.4.4.15 
     * Array.prototype.lastIndexOf ( searchElement [ , fromIndex ] )
     * 
     * Returns the last index at which a given element can be found in the
     * array, or -1 if it is not present. The array is searched backwards,
     * starting at fromIndex.
     * 
     * @param args
     * @return
     * @throws EcmaScriptException
     */
    public ESValue lastIndexOf(ESValue[] args) throws EcmaScriptException {
        int len = theArray.size();
        if (len != 0) {
            int offset = args.length >= 2 ? ((ESNumber) args[1]).toInt32()
                    : len;
            int index = offset >= 0 ? Math.min(offset, len - 1) : len
                    - Math.abs(offset);

            while (index >= 0) {
                if (theArray.get(index).equals(args[0])) {
                    return ESNumber.valueOf(index);
                }
                index--;
            }
        }
        return ESNumber.valueOf(-1);
    }
    
    /**
     * ES5 - 15.4.4.4
     * Array.prototype.concat ( [ item1 [ , item2 [ , � ] ] ] )
     * 
     * When the concat method is called with zero or more arguments item1,
     * item2, etc., it returns an array containing the array elements of the
     * object followed by the array elements of each argument in order.
     * 
     * @param args
     * @return
     * @throws EcmaScriptException
     */
    public ESValue concat(ESValue[] args) throws EcmaScriptException {
        ArrayPrototype newArray = newEmptyArray();
        
        ArrayList<ESValue> items = new ArrayList<ESValue>();
        items.addAll(theArray);
        Collections.addAll(items, args);
        for (ESValue value : items) {
            if (value instanceof ArrayPrototype) {
                for (ESValue subValue : ((ArrayPrototype) value).theArray) {
                    newArray.theArray.add(subValue);
                }
            } else {
                newArray.theArray.add(value);
            }
            newArray.updateLength(newArray.theArray.size());
        }
        return newArray;
    }
    
    /**
     * ES5 - 15.4.4.21
     * Array.prototype.reduce ( callbackfn [ , initialValue ] )
     * 
     * Apply a function against an accumulator and each value of
     * the array (from left-to-right) as to reduce it to a single value.
     * 
     * @param args
     * @return
     * @throws EcmaScriptException
     */
    public ESValue reduce(ESValue[] args) throws EcmaScriptException {
        return reduce(args, 0);
    }

    /**
     * ES5 - 15.4.4.22 
     * Array.prototype.reduceRight ( callbackfn [ , initialValue ] )
     * 
     * Apply a function simultaneously against two values of the array (from
     * right-to-left) as to reduce it to a single value.
     * 
     * @param args
     * @return
     * @throws EcmaScriptException
     */
    public ESValue reduceRight(ESValue[] args) throws EcmaScriptException {
        return reduce(args, theArray.size()-1);
    }
    
    /**
     * ES5 - 15.4.4.16 
     * Array.prototype.every ( callbackfn [ , thisArg ] )
     * 
     * Tests whether all elements in the array pass the test implemented by the
     * provided function.
     * 
     * @param args
     * @return
     * @throws EcmaScriptException
     */
    public ESValue every(ESValue[] args) throws EcmaScriptException {
        return iterate(args, IterationType.EVERY);
    }
    
    /**
     * ES5 - 15.4.4.17 
     * Array.prototype.some ( callbackfn [ , thisArg ] )
     * 
     * Tests whether some element in the array passes the test implemented by
     * the provided function.
     * 
     * @param args
     * @return
     * @throws EcmaScriptException
     */
    public ESValue some(ESValue[] args) throws EcmaScriptException {
        return iterate(args, IterationType.SOME);
    }
    
    /**
     * ES5 - 15.4.4.18 
     * Array.prototype.forEach ( callbackfn [ , thisArg ] )
     * 
     * Executes a provided function once per array element.
     * 
     * @param args
     * @return
     * @throws EcmaScriptException
     */
    public ESValue forEach(ESValue[] args) throws EcmaScriptException {
        return iterate(args, IterationType.FOREACH);
    }
    
    /**
     * ES5 - 15.4.4.19 
     * Array.prototype.map ( callbackfn [ , thisArg ] )
     * 
     * Creates a new array with the results of calling a provided function on
     * every element in this array.
     * 
     * @param args
     * @return
     * @throws EcmaScriptException
     */
    public ESValue map(ESValue[] args) throws EcmaScriptException {
        return iterate(args, IterationType.MAP);
    }

    /**
     * ES5 - 15.4.4.20 
     * Array.prototype.filter ( callbackfn [ , thisArg ] )
     * 
     * Creates a new array with all elements that pass the test implemented by
     * the provided function.
     * 
     * @param args
     * @return
     * @throws EcmaScriptException
     */
    public ESValue filter(ESValue[] args) throws EcmaScriptException {
        return iterate(args, IterationType.FILTER);
    }
    
    private ESValue reduce(ESValue[] args, int offset)
            throws EcmaScriptException {
        ESValue callbackfn = args.length > 0 ? args[0]
                : ESUndefined.theUndefined;
        if (callbackfn == null || !(callbackfn instanceof FunctionPrototype)) {
            throw new TypeError(callbackfn + " is not a function");
        }

        ESValue initialValue = args.length > 1 ? args[1] : null;
        for (int i = 0; i < theArray.size(); i++) {
            ESValue currentValue = theArray.get(Math.abs(i - offset));
            if (currentValue == null) {
                continue;
            }

            if (initialValue == null) {
                initialValue = currentValue;
            } else {
                ESValue[] callArgs = { initialValue, currentValue,
                        ESNumber.valueOf(i), this };
                initialValue = callbackfn.callFunction(this, callArgs);
            }
        }

        if (initialValue == null) {
            throw new TypeError("Reduce of empty array with no initial value");
        }

        return initialValue;
    }
    
    private ESValue iterate(ESValue[] args, IterationType type)
            throws EcmaScriptException {
        ESValue callbackfn = args.length > 0 ? args[0]
                : ESUndefined.theUndefined;
        if (callbackfn == null || !(callbackfn instanceof FunctionPrototype)) {
            throw new TypeError(callbackfn + " is not a function");
        }

        ArrayPrototype newArray = newEmptyArray();
        for (int i = 0; i < theArray.size(); i++) {
            ESValue elem = theArray.get(i);
            if (elem == null) {
                continue;
            }

            ESValue thisArg;
            if (args.length < 2 || args[1] == null
                    || args[1] == ESUndefined.theUndefined) {
                thisArg = this;
            } else {
                thisArg = args[1];
            }

            ESValue[] callArgs = { elem, ESNumber.valueOf(i), this };
            ESValue ret = callbackfn.callFunction(thisArg, callArgs);
            switch (type) {
            case EVERY:
                if (!ret.booleanValue()) {
                    return ret.toESBoolean();
                }
                break;
            case SOME:
                if (ret.booleanValue()) {
                    return ret.toESBoolean();
                }
                break;
            case FOREACH:
                break;
            case MAP:
                newArray.theArray.add(i, ret);
                break;
            case FILTER:
                if (ret.booleanValue()) {
                    newArray.theArray.add(elem);
                }
                break;
            default:
                throw new ProgrammingError("Invalid iteration type");
            }
        }
        switch (type) {
        case EVERY:
            return ESBoolean.valueOf(true);
        case SOME:
            return ESBoolean.valueOf(false);
        case FOREACH:
        default:
            return ESUndefined.theUndefined;
        case MAP:
        case FILTER:
            newArray.updateLength(newArray.theArray.size());
            return newArray;
        }
    }
    
    // This routines are taken from Java examples in a nutshell
    static interface Comparer {
        /**
         * Compare objects and return a value that indicates their relative
         * order: if (a > b) return a number > 0; if (a == b) return 0; if (a <
         * b) return a number < 0.
         **/
        int compare(ESValue a, ESValue b) throws EcmaScriptException;
    }

    static class DefaultComparer implements Comparer {
        public int compare(ESValue v1, ESValue v2) throws EcmaScriptException {
            ESValue v1p = v1.toESPrimitive(ESValue.EStypeNumber);
            ESValue v2p = v2.toESPrimitive(ESValue.EStypeNumber);
            if (v1p == ESUndefined.theUndefined
                    && v2p == ESUndefined.theUndefined) {
                return 0;
            }
            if (v1p == ESUndefined.theUndefined) {
                return 1;
            }
            if (v2p == ESUndefined.theUndefined) {
                return -1;
            }
            String s1 = v1.toString();
            String s2 = v2.toString();
            return s1.compareTo(s2);
        }
    }
    
    private ArrayPrototype newEmptyArray() throws EcmaScriptException {
        Evaluator eval = getEvaluator();
        return (ArrayPrototype) eval.getValue(StandardProperty.ARRAYstring).doConstruct(
                ESValue.EMPTY_ARRAY);
    }

    /**
     * This is the main sort() routine. It performs a quicksort on the elements
     * of array a between the element from and the element to. The Comparer
     * argument c is used to perform comparisons between elements of the array.
     **/
    private void sort(int from, int to, Comparer c) throws EcmaScriptException {
        // If there is nothing to sort, return
        if (theArray.size() < 2) {
            return;
        }

        // This is the basic quicksort algorithm, stripped of frills that can
        // make
        // it faster but even more confusing than it already is. You should
        // understand what the code does, but don't have to understand just
        // why it is guaranteed to sort the array...
        // Note the use of the compare() method of the Comparer object.
        int i = from, j = to;
        ESValue center = theArray.get((from + to) / 2);
        do {
            ESValue ai = theArray.get(i);
            ESValue aj = theArray.get(j);
            while ((i < to) && (c.compare(center, ai) > 0)) {
                i++;
                ai = theArray.get(i);
            }
            while ((j > from) && (c.compare(center, aj) < 0)) {
                j--;
                aj = theArray.get(j);
            }
            if (i < j) {
                ESValue tmp = ai;
                theArray.set(i, aj);
                theArray.set(j, tmp);
            }
            if (i <= j) {
                i++;
                j--;
            }
        } while (i <= j);
        if (from < j) {
            sort(from, j, c); // recursively sort the rest
        }
        if (i < to) {
            sort(i, to, c);
        }
    }

    /**
     * ES5 - 15.4.4.11 
     * Array.prototype.sort (comparefn)
     * 
     * Sorts the elements of an array in place and returns the array.
     * 
     * @param compareFn
     *            A function returning a comparer
     * @return the sorted array (in place)
     */
    public ESValue sort(ESValue compareFn) throws EcmaScriptException {
        if ((compareFn != null) && (!(compareFn instanceof FunctionPrototype))) {
            throw new EcmaScriptException("Compare function not a function: "
                    + compareFn);
        }
        Comparer c = null;
        if (compareFn != null) {
            c = new FunctionComparer((FunctionPrototype) compareFn);
        } else {
            c = new DefaultComparer();
        }

        sort(0, theArray.size() - 1, c);
        return this;
    }

    // overrides
    @Override
    public void putProperty(String propertyName, ESValue propertyValue, int hash)
            throws EcmaScriptException {
        if (hash == StandardProperty.LENGTHhash && propertyName.equals(StandardProperty.LENGTHstring)) {
            long newLength = ((ESPrimitive) propertyValue).longValue();
            int newLen = checkUInt32(newLength);
            if (newLen < theArray.size()) {
                int len = theArray.size();
                int i;
                for (i = len; i >= newLen; i--) {
                    ESValue value = theArray.get(i-1);
                    if (value != null && value != ESUndefined.theUndefined) {
                        // assuming null/undefined are the only "deletable"
                        // elements in our implementation (for the moment)
                        break;
                    }
                }
                if (i != len) {
                    setSize(i);
                }
            }
            super.putProperty(propertyName, propertyValue, hash);
        } else {
            long index = getIndex(propertyName);
            if (index < 0) {
                super.putProperty(propertyName, propertyValue, hash);
            } else {
                putProperty(index, propertyValue);
            }
        }
    }

    private int checkUInt32(long newLength) throws RangeError {
        if (newLength != (newLength & 0xffffffffL)) {
            throw new RangeError("Array length exceeds maximum supported");
        }
        return (int)newLength;
    }

    private boolean isUint32(long newLength) {
        return newLength != (newLength & 0x7fffffffL);
    }

    // overrides
    // overrides
    @Override
    public void putProperty(long index, ESValue propertyValue)
            throws EcmaScriptException {
        if (index >= theArray.size()) {
            setSize((int)index+1);
        }
        theArray.set((int)index, propertyValue);
    }

    // overrides
    @Override
    public ESValue getPropertyInScope(String propertyName,
            ScopeChain previousScope, int hash) throws EcmaScriptException {
        if (hash == StandardProperty.LENGTHhash && propertyName.equals(StandardProperty.LENGTHstring)) {
            return ESNumber.valueOf(theArray.size());
        }

        ESValue value = getPropertyIfAvailable(propertyName, hash);

        if (value != null) {
            return value;
        }

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
        long index = getIndex(propertyName);
        if (index < 0 || index > theArray.size()) {
            return super.getPropertyIfAvailable(propertyName, hash);
        }
        return getPropertyIfAvailable(index);
    }

    private long getIndex(String propertyName) {
        long index = -1; // indicates not a valid index value
        if (isAllDigits(propertyName)) {
            try {
                index = Long.parseLong(propertyName); // should be uint
                if (isUint32(index)) {
                    index = -1;
                }
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
        return index;
    }

    @Override
    public ESValue getPropertyIfAvailable(long index) throws EcmaScriptException {
        Object theElement = null;
        if (index < theArray.size()) {
            theElement = theArray.get((int)index);
        }
        return (ESValue) theElement;

    }

//    // overrides
//    @Override
//    public boolean hasProperty(String propertyName, int hash)
//            throws EcmaScriptException {
//        if (hash == LENGTHhash && propertyName.equals(LENGTHstring)) {
//            return true;
//        }
//        int index = -1; // indicates not a valid index value
//        try {
//            index = Integer.parseInt(propertyName); // should be uint
//        } catch (NumberFormatException e) {
//            // do nothing
//        }
//        if (index < 0) {
//            return super.hasProperty(propertyName, hash);
//        }
//        return index < theArray.size();
//
//    }

    // overrides
    // Skip elements which were never set (are null), as Netscape
    /*
     * OLD - DID IGNORE THE NORMAL PROPERTIES OF AN ARRAY public Enumeration
     * getProperties() { return new Enumeration() { int nextIndex = 0; public
     * boolean hasMoreElements() { while ( (nextIndex<theArray.size()) &&
     * (theArray.elementAt(nextIndex) == null)) nextIndex++; return
     * nextIndex<theArray.size(); } public Object nextElement() { if
     * (hasMoreElements()) { return ESNumber.valueOf(nextIndex++); } else {
     * throw new java.util.NoSuchElementException(); } } }; }
     */
    /**
     * Returns an enumerator for the key elements of this object, that is all is
     * enumerable properties and the (non hidden) ones of its prototype, etc...
     * As used for the for in statement.
     *<P>
     * Skip elements which were never set (are null), as Netscape SHOULD USE
     * SUPER INSTEAD !
     * 
     * @return the enumerator
     */
    @Override
    public Enumeration<String> getProperties() {
        return new Enumeration<String>() {
            Enumeration<String> props = getPropertyMap().keys();
            String currentKey = null;
            int currentHash = 0;
            int nextIndex = 0;
            boolean inside = false;
            ESObject arrayPrototype = ArrayPrototype.this.getPrototype();

            public boolean hasMoreElements() {
                // Check if hasMoreElements was already called
                if (currentKey != null) {
                    return true;
                }

                // Check if a numeric key is appropriate
                while ((nextIndex < theArray.size())
                        && (theArray.get(nextIndex) == null)) {
                    nextIndex++;
                }
                if (nextIndex < theArray.size()) {
                    // Should it be an ESNumber?
                    currentKey = Integer.toString(nextIndex++);
                    return true;
                }

                while (props.hasMoreElements()) {
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
                if (!inside && arrayPrototype != null) {
                    inside = true;
                    props = arrayPrototype.getProperties();
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
     *          <P>
     *          Hidde elements which are null (as netscape)
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
            int currentHash = 0;
            boolean inside = false; // true when examing prototypes properties
            int nextIndex = 0;

            public boolean hasMoreElements() {
                // OK if we already checked for a property and one exists
                if (currentKey != null) {
                    return true;
                }
                // loop on idex properties
                if (nextIndex < theArray.size()) {
                    while ((nextIndex < theArray.size())
                            && (theArray.get(nextIndex) == null)) {
                        nextIndex++;
                    }
                    if (nextIndex < theArray.size()) {
                        currentKey = Integer.toString(nextIndex);
                        currentHash = currentKey.hashCode();
                        nextIndex++;
                        return true;
                    }
                }
                // Loop on special properties first
                if (specialEnumerator < specialProperties.length) {
                    currentKey = specialProperties[specialEnumerator];
                    currentHash = currentKey.hashCode();
                    specialEnumerator++;
                    return true;
                }
                // loop on standard or prototype properties
                while (props.hasMoreElements()) {
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
                if (!inside && getPrototype() != null) {
                    inside = true;
                    props = getPrototype().getProperties();
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

    // overrides
    @Override
    public String[] getSpecialPropertyNames() {
        String[] ns = { StandardProperty.LENGTHstring };
        return ns;
    }

    // Support of custom compare function for sort
    class FunctionComparer implements Comparer {
        FunctionPrototype compareFn;
        ESValue arguments[];
        ESObject thisObject;

        public FunctionComparer(FunctionPrototype fn) {
            this.compareFn = fn;
            this.arguments = new ESValue[2];
            this.thisObject = getEvaluator().getGlobalObject();
        }

        public int compare(ESValue v1, ESValue v2) throws EcmaScriptException {
            ESValue v1p = v1.toESPrimitive(ESValue.EStypeNumber);
            ESValue v2p = v2.toESPrimitive(ESValue.EStypeNumber);
            if (v1p == ESUndefined.theUndefined
                    && v2p == ESUndefined.theUndefined) {
                return 0;
            }
            if (v1p == ESUndefined.theUndefined) {
                return 1;
            }
            if (v2p == ESUndefined.theUndefined) {
                return -1;
            }
            arguments[0] = v1;
            arguments[1] = v2;
            ESValue compValue = compareFn.callFunction(thisObject, arguments);
            return compValue.toInt32();
        }
    }
    
    @Override
    public void toJson(Appendable appendable, JsonState state, String parentPropertyName) throws IOException, EcmaScriptException {
        state.pushCyclicCheck(this);
        state.indent.push();
        appendable.append('[');
        String separator = state.indent.start();
        int i = 0;
        for (ESValue value : theArray) {
            appendable.append(separator);
            value = state.callReplacerFunction(this, ESString.valueOf(i), value );
            if (!value.canJson()) {
                value = ESNull.theNull;
            }
            value.toJson(appendable, state, "");
            separator = state.indent.separator();
            i++;
        }
        if (i>0) {
            appendable.append(state.indent.end());
        }
        appendable.append(']');
        state.indent.pop();
        state.popCyclicCheck();
    }
    
    @Override
    public boolean canJson() {
        return true;
    }
    
    @Override
    public Enumeration<String> getOwnPropertyNames() {
        return new ArrayPropertyNamesEnumeration(super.getOwnPropertyNames(), size());
    }
    
    @Override
    public Enumeration<String> keys() {
        return new ArrayPropertyNamesEnumeration(super.keys(), size());
    }
    
    @Override
    public boolean hasEnumerableProperty(String propertyName, int hashCode) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher matcher = pattern.matcher(propertyName);
        if (matcher.matches()) {
            int idx = Integer.parseInt(propertyName,10);
            if (idx < theArray.size()) {
                return true;
            }
        }
        return super.hasEnumerableProperty(propertyName, hashCode);
    }
}
