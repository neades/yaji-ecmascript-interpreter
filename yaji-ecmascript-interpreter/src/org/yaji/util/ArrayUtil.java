package org.yaji.util;

import java.lang.reflect.Array;

import FESI.Data.ArrayPrototype;
import FESI.Data.ESObject;
import FESI.Data.ESValue;
import FESI.Data.StandardProperty;
import FESI.Exceptions.EcmaScriptException;

public abstract class ArrayUtil {

    public static long getArrayLength(ESObject esObject) throws EcmaScriptException {
        ESValue lengthProperty = esObject.getProperty(StandardProperty.LENGTHstring, StandardProperty.LENGTHhash);
        return lengthProperty.toUInt32();
    }

    public static Object arrayToJavaArray(ESObject esArray, Class<?> componentType) throws EcmaScriptException {
        int l = (int) getArrayLength(esArray);
        Object array = Array.newInstance(componentType, l);
        if (l == 0) {
            return array;
        }
        for (int i = 0; i < l; i++) {
            ESValue element = esArray.getProperty((long)i);
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

}
