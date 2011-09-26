package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;
import FESI.Util.EvaluatorAccess;
import FESI.Util.IEvaluatorAccess;

public class ObjectObjectTest {
    
    private static final ESBoolean ES_TRUE = ESBoolean.valueOf(true);
    private static final ESBoolean ES_FALSE = ESBoolean.valueOf(false);
    private Evaluator evaluator;
    private BuiltinFunctionObject objectObject;
    private BuiltinFunctionObject arrayObject;
    @Before
    public void setUp() throws Exception {
        evaluator = new Evaluator();
        
        EvaluatorAccess.setAccessor(new IEvaluatorAccess() {
            
            public Evaluator getEvaluator() {
                return evaluator;
            }
        });
        
        objectObject = (BuiltinFunctionObject) evaluator.getGlobalObject().getProperty("Object","Object".hashCode());
        arrayObject = (BuiltinFunctionObject) evaluator.getGlobalObject().getProperty("Array","Array".hashCode());
    }
    
    @Test public void testNewObjectNotFrozen() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        
        assertEquals("A new object is extensible, so it is not frozen.",ES_FALSE,
                objectObject.doIndirectCall(evaluator, evaluator.getGlobalObject(), "isFrozen", new ESValue[] { object }));
    }
    
/*
    // An empty object which is not extensible is vacuously frozen.
    var vacuouslyFrozen = Object.preventExtensions({});
    assert(Object.isFrozen(vacuouslyFrozen) === true);

    // A new object with one property is also extensible, ergo not frozen.
    var oneProp = { p: 42 };
    assert(Object.isFrozen(oneProp) === false);

    // Preventing extensions to the object still doesn't make it frozen,
    // because the property is still configurable (and writable).
    Object.preventExtensions(oneProp);
    assert(Object.isFrozen(oneProp) === false);

    // ...but then deleting that property makes the object vacuously frozen.
    delete oneProp.p;
    assert(Object.isFrozen(oneProp) === true);

    // A non-extensible object with a non-writable but still configurable property is not frozen.
    var nonWritable = { e: "plep" };
    Object.preventExtensions(nonWritable);
    Object.defineProperty(nonWritable, "e", { writable: false }); // make non-writable
    assert(Object.isFrozen(nonWritable) === false);

    // Changing that property to non-configurable then makes the object frozen.
    Object.defineProperty(nonWritable, "e", { configurable: false }); // make non-configurable
    assert(Object.isFrozen(nonWritable) === true);

    // A non-extensible object with a non-configurable but still writable property also isn't frozen.
    var nonConfigurable = { release: "the kraken!" };
    Object.preventExtensions(nonConfigurable);
    Object.defineProperty(nonConfigurable, "release", { configurable: false });
    assert(Object.isFrozen(nonConfigurable) === false);

    // Changing that property to non-writable then makes the object frozen.
    Object.defineProperty(nonConfigurable, "release", { writable: false });
    assert(Object.isFrozen(nonConfigurable) === true);

    // A non-extensible object with a configurable accessor property isn't frozen.
    var accessor = { get food() { return "yum"; } };
    Object.preventExtensions(accessor);
    assert(Object.isFrozen(accessor) === false);

    // ...but make that property non-configurable and it becomes frozen.
    Object.defineProperty(accessor, "food", { configurable: false });
    assert(Object.isFrozen(accessor) === true);
*/
    @Test public void testFrozenAfterFreeze() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.putProperty("1", ESString.valueOf("81"), "1".hashCode());
        object.freeze();
        objectObject.doIndirectCall(evaluator, evaluator.getGlobalObject(), "freeze", new ESValue[] { object });
        assertEquals("the easiest way for an object to be frozen is if Object.freeze has been called on it.",
                ES_TRUE,
                objectObject.doIndirectCall(evaluator, evaluator.getGlobalObject(), "isFrozen", new ESValue[] { object }));
    }
/*

    // By definition, a frozen object is non-extensible.
    assert(Object.isExtensible(frozen) === false);

    // Also by definition, a frozen object is sealed.
    assert(Object.isSealed(frozen) === true);
*/

    @Test public void shouldInitialisePrototypeToObjectPrototype() throws Exception {
        ESValue prototype = objectObject.getProperty("prototype", "prototype".hashCode());
        assertTrue( prototype instanceof ObjectPrototype);
    }
    
    @Test public void shouldImplementGetPrototypeOf() throws Exception {
        ESValue result = objectObject.doIndirectCall(evaluator, objectObject, "getPrototypeOf", new ESValue[] { objectObject });
        assertTrue(result instanceof FunctionPrototype);
    }
    @Test public void getPrototypeOfShouldThrowTypeError() throws Exception {
        try {
            objectObject.doIndirectCall(evaluator, objectObject, "getPrototypeOf", ESValue.EMPTY_ARRAY);
            fail("Should throw exception");
        } catch( EcmaScriptException e) {
            ESObject errorObject = (ESObject) e.getErrorObject(evaluator);
            assertEquals("TypeError",errorObject.getProperty("name","name".hashCode()).toString());
        }
    }
    
    @Test(expected=TypeError.class)
    public void getOwnPropertyShouldThrowTypeErrorIfNotPassedObject() throws Exception {
        objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { ESUndefined.theUndefined });
    }
    
    @Test
    public void getOwnPropertyShouldReturnDataDescriptorObject() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.putProperty("test", ESNull.theNull, "test".hashCode());
        ObjectPrototype result = (ObjectPrototype)objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { object, new ESString("test") });
        assertEquals(ES_TRUE,result.getProperty(StandardProperty.WRITABLEstring));
        assertEquals(ES_TRUE,result.getProperty(StandardProperty.ENUMERABLEstring));
        assertEquals(ES_TRUE,result.getProperty(StandardProperty.CONFIGURABLEstring));
        assertEquals(ESNull.theNull,result.getProperty(StandardProperty.VALUEstring));
    }
    
    @Test
    public void getOwnPropertyShouldReturnIndicateFieldWritable() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.putProperty("test", ESNull.theNull, "test".hashCode());
        object.freeze();
        ObjectPrototype result = (ObjectPrototype)objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { object, new ESString("test") });
        assertEquals(ES_FALSE,result.getProperty(StandardProperty.WRITABLEstring));
    }
    
    @Test
    public void getOwnPropertyShouldIndicateFieldEnumerable() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.putHiddenProperty("test", ESNull.theNull);
        ObjectPrototype result = (ObjectPrototype)objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { object, new ESString("test") });
        assertEquals(ES_FALSE,result.getProperty(StandardProperty.ENUMERABLEstring));
    }
    
    @Test
    public void getOwnPropertyReturnsUndefinedIfNotDefined() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.putProperty("test", ESNull.theNull, "test".hashCode());
        ESValue result = objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { object, new ESString("not_test") });
        assertSame(ESUndefined.theUndefined,result);
    }
    
    @Test
    public void getOwnPropertyReturnsSetterAndGetter() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        ESValue v = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        v.setGetAccessorDescriptor(createFunction("return null;"));
        v.setSetAccessorDescriptor(createFunction("return void 0;"));
        object.putProperty("test", v, "test".hashCode());
        
        ObjectPrototype result = (ObjectPrototype)objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { object, new ESString("test") });
        assertEquals(ESUndefined.theUndefined,result.getProperty(StandardProperty.WRITABLEstring));
        assertEquals(ES_TRUE,result.getProperty(StandardProperty.ENUMERABLEstring));
        assertEquals(ES_TRUE,result.getProperty(StandardProperty.CONFIGURABLEstring));
        assertEquals(ESUndefined.theUndefined,result.getProperty(StandardProperty.VALUEstring));
        assertTrue(result.getProperty(StandardProperty.GETstring) instanceof FunctionPrototype);
        assertTrue(result.getProperty(StandardProperty.SETstring) instanceof FunctionPrototype);
    }
    
    @Test
    public void definePropertyShouldAllowEnumerableToBeSetFalse() throws Exception {
        String descPropertyName = StandardProperty.ENUMERABLEstring;
        definePropertyOnExistingProperty(descPropertyName, false);
    }
    
    @Test
    public void definePropertyShouldAllowWritableToBeSetFalse() throws Exception {
        String descPropertyName = StandardProperty.WRITABLEstring;
        definePropertyOnExistingProperty(descPropertyName, false);
    }

    @Test
    public void definePropertyShouldAllowConfigurableToBeSetFalse() throws Exception {
        String descPropertyName = StandardProperty.CONFIGURABLEstring;
        definePropertyOnExistingProperty(descPropertyName, false);
    }

    
    @Test
    public void definePropertyShouldAllowEnumerableToBeSetTrue() throws Exception {
        String descPropertyName = StandardProperty.ENUMERABLEstring;
        definePropertyOnExistingProperty(descPropertyName, true);
    }
    
    @Test
    public void definePropertyShouldAllowWritableToBeSetTrue() throws Exception {
        String descPropertyName = StandardProperty.WRITABLEstring;
        definePropertyOnExistingProperty(descPropertyName, true);
    }
    
    @Test
    public void definePropertyShouldAllowValueToBeSet() throws Exception {
        String descPropertyName = StandardProperty.VALUEstring;
        definePropertyOnExistingProperty(descPropertyName, true);
    }

    @Test
    public void definePropertyShouldAllowConfigurableToBeSetTrue() throws Exception {
        String descPropertyName = StandardProperty.CONFIGURABLEstring;
        definePropertyOnExistingProperty(descPropertyName, true);
    }
    
    @Test(expected=TypeError.class)
    public void definePropertyCannotAddPropertyIfObjectNotExtensible() throws Exception {
        // 8.12.9.3
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ES_FALSE);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.freeze();
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }
    
    @Test
    public void shouldInitialiseNoneExistentPropertyToDefaultValue() throws Exception {
        // 8.12.9.4 & 8.6.1 Table 7
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        ObjectPrototype result = (ObjectPrototype)objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { object, propertyName });
        assertEquals(ES_FALSE,result.getProperty(StandardProperty.WRITABLEstring));
        assertEquals(ES_FALSE,result.getProperty(StandardProperty.ENUMERABLEstring));
        assertEquals(ES_FALSE,result.getProperty(StandardProperty.CONFIGURABLEstring));
        assertEquals(ESUndefined.theUndefined,result.getProperty(StandardProperty.VALUEstring));
    }
    
    @Test(expected=TypeError.class)
    public void shouldRejectIfNotConfigurable() throws Exception {
        // 8.12.9.7a
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ES_FALSE);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ES_TRUE);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }

    @Test(expected=TypeError.class)
    public void shouldRejectChangingEnumerablityOfNonConfigurableProperty() throws Exception {
        // 8.12.9.7b
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = createArgumentsObject(null, ES_FALSE, ES_TRUE, null, null, null);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.ENUMERABLEstring, ES_FALSE);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }

    @Test(expected=TypeError.class)
    public void shouldRejectChangingAccessorUsageOfNonConfigurableProperty() throws Exception {
        // 8.12.9.9a
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = createArgumentsObject(ES_TRUE, ES_FALSE, null, null, null, null);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        argumentsObject = createArgumentsObject(null, null, null, null, createFunction("return null;"), createFunction("return void 0;"));
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }
    
    @Test
    public void changingToAccessorPropertyShouldRetainConfigurableAndEnumerableAttributes() throws Exception {
        // 8.12.9.9b
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = createArgumentsObject(ES_TRUE, ES_TRUE, ES_TRUE, null, null, null);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        
        argumentsObject = createArgumentsObject(null, null, null, null, createFunction("return null;"), createFunction("return void 0;"));
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        
        ObjectPrototype result = (ObjectPrototype)objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { object, propertyName });
        assertEquals(ESUndefined.theUndefined,result.getProperty(StandardProperty.WRITABLEstring));
        assertEquals(ES_TRUE,result.getProperty(StandardProperty.ENUMERABLEstring));
        assertEquals(ES_TRUE,result.getProperty(StandardProperty.CONFIGURABLEstring));
        assertEquals(ESUndefined.theUndefined,result.getProperty(StandardProperty.VALUEstring));
    }

    @Test
    public void changingToDataPropertyShouldRetainConfigurableAndEnumerableAttributes() throws Exception {
        // 8.12.9.9b
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = createArgumentsObject(null, ES_TRUE, ES_TRUE, null, createFunction("return null;"), createFunction("return void 0;"));
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.VALUEstring,new ESString("changed"));
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        
        ObjectPrototype result = (ObjectPrototype)objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { object, propertyName });
        assertEquals(ES_FALSE,result.getProperty(StandardProperty.WRITABLEstring));
        assertEquals(ES_TRUE,result.getProperty(StandardProperty.ENUMERABLEstring));
        assertEquals(ES_TRUE,result.getProperty(StandardProperty.CONFIGURABLEstring));
        assertEquals(new ESString("changed"),result.getProperty(StandardProperty.VALUEstring));
    }
    
    @Test(expected=TypeError.class)
    public void shouldDisallowChangingWritableIfNonConfigurable() throws Exception {
        // 8.12.9.10.a.i
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = createArgumentsObject(ES_TRUE, ES_FALSE, ES_TRUE, ES_FALSE, null, null);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        
        argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.WRITABLEstring, ES_TRUE);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }

    @Test(expected=TypeError.class)
    public void cantSetBothValueAndAccessor() throws Exception {
        // 8.10.5.9
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = createArgumentsObject(ES_TRUE, ES_FALSE, ES_TRUE, ES_FALSE, null, createFunction("return null;"));
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }


    @Test(expected=TypeError.class)
    public void shouldDisallowChangingValueIfNonConfigurable() throws Exception {
        // 8.12.9.10.a.ii.TRUE
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = createArgumentsObject(ES_TRUE, ES_FALSE, ES_TRUE, ES_FALSE, null, null);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        
        argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.VALUEstring, ES_FALSE);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }

    @Test
    public void shouldAllowChangingValueIfNonConfigurableIfSameValue() throws Exception {
        // 8.12.9.10.a.ii.FALSE
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        ESString propertyName = defineValueProperty(object, "propertyName", false, true, false, ES_TRUE);
        
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.VALUEstring, ES_TRUE);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }

    private ESString defineValueProperty(ESObject object, String propName,
            boolean configurable, boolean enumerable, boolean writable,
            ESBoolean value) throws EcmaScriptException, NoSuchMethodException {
        ESString propertyName = new ESString(propName);
        ESObject argumentsObject = createArgumentsObject(value, ESBoolean.valueOf(configurable),
                ESBoolean.valueOf(enumerable), ESBoolean.valueOf(writable), null, null);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        return propertyName;
    }

    private ESObject createArgumentsObject(ESValue value,
            ESBoolean configurable, ESBoolean enumerable, ESBoolean writable, ESValue getter, ESValue setter)
            throws EcmaScriptException {
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        setPropertyIfValueNotNull(argumentsObject, StandardProperty.CONFIGURABLEstring, configurable);
        setPropertyIfValueNotNull(argumentsObject, StandardProperty.ENUMERABLEstring, enumerable);
        setPropertyIfValueNotNull(argumentsObject, StandardProperty.WRITABLEstring, writable);
        setPropertyIfValueNotNull(argumentsObject, StandardProperty.VALUEstring, value);
        setPropertyIfValueNotNull(argumentsObject, StandardProperty.GETstring, getter);
        setPropertyIfValueNotNull(argumentsObject, StandardProperty.SETstring, setter);
        return argumentsObject;
    }

    private void setPropertyIfValueNotNull(ESObject argumentsObject,
            String propertyName, ESValue value)
            throws EcmaScriptException {
        if (value != null) {
            argumentsObject.putProperty(propertyName, value);
        }
    }

    @Test(expected=TypeError.class)
    public void shouldRejectSetAccessorChangeOnNonConfigurableProperty() throws Exception {
        // 8.12.9.11.a.i
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = createArgumentsObject(null, ES_FALSE, ES_TRUE, null, createFunction("return null;"), createFunction("return void 0;"));
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        argumentsObject = createArgumentsObject(null, null, null, null, createFunction("return null;"), createFunction("return void 0;"));
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }
    

    @Test
    public void shouldAllowSetAccessorNoChangeOnNonConfigurableProperty() throws Exception {
        // 8.12.9.11.a.i.FALSE
        ESString propertyName = new ESString("propertyName");
        ESValue setFunction = createFunction("return void 0;");
        ESObject argumentsObject = createArgumentsObject(null, ES_FALSE, ES_TRUE, null, null, setFunction);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        argumentsObject = createArgumentsObject(null, null, null, null, null, setFunction);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }
    

    @Test(expected=TypeError.class)
    public void shouldRejectGetAccessorChangeOnNonConfigurableProperty() throws Exception {
        // 8.12.9.11.a.ii
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = createArgumentsObject(null, ES_FALSE, ES_TRUE, null, createFunction("return null;"), null);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });

        argumentsObject = createArgumentsObject(null, null, null, null, createFunction("return null;"), null);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }
    

    @Test
    public void shouldAllowGetAccessorNoChangeOnNonConfigurableProperty() throws Exception {
        // 8.12.9.11.a.ii.FALSE
        ESString propertyName = new ESString("propertyName");
        ESValue getFunction = createFunction("return void 0;");
        ESObject argumentsObject = createArgumentsObject(null, ES_FALSE, ES_TRUE, null, getFunction, null);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        argumentsObject = createArgumentsObject(null, null, null, null, getFunction, null);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }
    
    @Test
    public void getOwnPropertyNamesShouldReturnArray() throws Exception {
        // 15.2.3.4.2
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        ESValue result = objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyNames", new ESValue[] { object });
        ArrayPrototype array = (ArrayPrototype) result;
        assertEquals(0,array.size());
    }

    @Test
    public void getOwnPropertyNamesShouldReturnArrayWithProperty() throws Exception {
        // 15.2.3.4.2
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        defineValueProperty(object, "propertyName", false, false, false, ES_TRUE);
        
        ESValue result = objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyNames", new ESValue[] { object });
        ArrayPrototype array = (ArrayPrototype) result;
        assertEquals(1,array.size());
        assertEquals("propertyName",array.getProperty(0).toString());
    }


    @Test
    public void getOwnPropertyNamesShouldReturnIndicesForArray() throws Exception {
        // 15.2.3.4.2
        ESObject object = arrayObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.putProperty(StandardProperty.LENGTHstring, ESNumber.valueOf(3));
        object.putProperty("another", ESNumber.valueOf(3));
        
        ESValue result = objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyNames", new ESValue[] { object });
        ArrayPrototype array = (ArrayPrototype) result;
        assertEquals(5,array.size());
        assertEquals("0",array.getProperty(0).toString());
        assertEquals("1",array.getProperty(1).toString());
        assertEquals("2",array.getProperty(2).toString());
        assertEquals("length",array.getProperty(3).toString());
        assertEquals("another",array.getProperty(4).toString());
    }

    @Test
    public void getOwnPropertyNamesShouldReturnIndicesForString() throws Exception {
        // 15.2.3.4.NOTE
        ESObject object = (new ESString("abc")).toESObject(evaluator);
        object.putProperty(StandardProperty.LENGTHstring, ESNumber.valueOf(3));
        
        ESValue result = objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyNames", new ESValue[] { object });
        ArrayPrototype array = (ArrayPrototype) result;
        assertEquals(4,array.size());
        assertEquals("0",array.getProperty(0).toString());
        assertEquals("1",array.getProperty(1).toString());
        assertEquals("2",array.getProperty(2).toString());
        assertEquals("length",array.getProperty(3).toString());
    }

    @Test
    public void shouldDefineProperties() throws Exception {
        // 15.1.3.7
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty("propertyName", createArgumentsObject(new ESString("value"), ES_FALSE, ES_FALSE, ES_FALSE, null, null));
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperties", new ESValue[] { object, argumentsObject });
        ObjectPrototype result = (ObjectPrototype)objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { object, propertyName });
        assertEquals(ES_FALSE,result.getProperty(StandardProperty.WRITABLEstring));
        assertEquals(ES_FALSE,result.getProperty(StandardProperty.ENUMERABLEstring));
        assertEquals(ES_FALSE,result.getProperty(StandardProperty.CONFIGURABLEstring));
        assertEquals(new ESString("value"),result.getProperty(StandardProperty.VALUEstring));
    }
    
    @Test
    public void objectObjectCreateShouldReturnObject() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        
        ESValue objectCreated = objectObject.doIndirectCall(evaluator, objectObject, "create", new ESValue[] { object });
        
        assertSame(object,((ESObject)objectCreated).getPrototype());
    }
    
    @Test
    public void objectObjectCreateShouldReturnObjectWithNullPrototype() throws Exception {
        
        ESValue objectCreated = objectObject.doIndirectCall(evaluator, objectObject, "create", new ESValue[] { ESNull.theNull });
        
        assertNull(((ESObject)objectCreated).getPrototype());
    }
    
    @Test(expected=TypeError.class)
    public void objectObjectCreateShouldFail() throws Exception {
        objectObject.doIndirectCall(evaluator, objectObject, "create", new ESValue[] { ESNumber.valueOf(1.0) });
    }
    
    @Test(expected=TypeError.class)
    public void sealShouldFailForNonObjectNull() throws Exception {
        objectObject.doIndirectCall(evaluator, objectObject, "seal", new ESValue[] { ESNull.theNull });
    }
    
    @Test(expected=TypeError.class)
    public void sealShouldFailForNonObjectUndefined() throws Exception {
        objectObject.doIndirectCall(evaluator, objectObject, "seal", new ESValue[] { ESUndefined.theUndefined });
    }
    
    @Test(expected=TypeError.class)
    public void isSealedShouldFailForNonObjectUndefined() throws Exception {
        objectObject.doIndirectCall(evaluator, objectObject, "isSealed", new ESValue[] { ESUndefined.theUndefined });
    }
    
    @Test(expected=TypeError.class)
    public void isExtensibleShouldFailForNonObjectUndefined() throws Exception {
        objectObject.doIndirectCall(evaluator, objectObject, "isExtensible", new ESValue[] { ESUndefined.theUndefined });
    }
    
    @Test
    public void sealShouldReturnSuppliedObject() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        ESValue result = objectObject.doIndirectCall(evaluator, objectObject, "seal", new ESValue[] { object });
        assertSame(result,object);
    }
    
    @Test
    public void sealShouldSetNonExtensible() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        assertEquals(ESBoolean.valueOf(true),objectObject.doIndirectCall(evaluator, objectObject, "isExtensible", new ESValue[] { object }));
        ESValue result = objectObject.doIndirectCall(evaluator, objectObject, "seal", new ESValue[] { object });
        assertEquals(ESBoolean.valueOf(false),objectObject.doIndirectCall(evaluator, objectObject, "isExtensible", new ESValue[] { result }));
    }
    
    @Test
    public void sealShouldMakePropertiesNonConfigurable() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.putProperty("propertyName", ESNumber.valueOf(1));
        objectObject.doIndirectCall(evaluator, objectObject, "seal", new ESValue[] { object });
        ObjectPrototype result = (ObjectPrototype)objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { object, new ESString("propertyName") });
        assertEquals(ES_FALSE,result.getProperty(StandardProperty.CONFIGURABLEstring));
    }
    
    @Test
    public void onceSealedIsSealedShouldBeTrue() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.putProperty("propertyName", ESNumber.valueOf(1));
        assertEquals(ESBoolean.valueOf(false),objectObject.doIndirectCall(evaluator, objectObject, "isSealed", new ESValue[] { object }));
        ESValue result = objectObject.doIndirectCall(evaluator, objectObject, "seal", new ESValue[] { object });
        assertEquals(ESBoolean.valueOf(true),objectObject.doIndirectCall(evaluator, objectObject, "isSealed", new ESValue[] { result }));
    }
    
    @Test
    public void changingPropertyOfSealedObjectToNonwritableMakesObjectFrozen() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.putProperty("propertyName", ESNumber.valueOf(1));
        ESValue result = objectObject.doIndirectCall(evaluator, objectObject, "seal", new ESValue[] { object });
        assertEquals(ESBoolean.valueOf(false),objectObject.doIndirectCall(evaluator, objectObject, "isFrozen", new ESValue[] { result }));
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, new ESString("propertyName"), createArgumentsObject(null, null, null, ES_FALSE, null, null) });
        assertEquals(ESBoolean.valueOf(true),objectObject.doIndirectCall(evaluator, objectObject, "isFrozen", new ESValue[] { result }));
    }
    
    @Test(expected=TypeError.class)
    public void preventExtensionsShouldFailIfNotPassedObject() throws Exception {
        objectObject.doIndirectCall(evaluator, objectObject, "preventExtensions", new ESValue[] { ESNumber.valueOf(1.0) });
    }
    
    @Test
    public void preventExtensionsShouldSetNonExtensible() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        assertEquals(ESBoolean.valueOf(true),objectObject.doIndirectCall(evaluator, objectObject, "isExtensible", new ESValue[] { object }));
        ESValue result = objectObject.doIndirectCall(evaluator, objectObject, "preventExtensions", new ESValue[] { object });
        assertEquals(ESBoolean.valueOf(false),objectObject.doIndirectCall(evaluator, objectObject, "isExtensible", new ESValue[] { result }));
    }
    
    @Test
    public void preventExtensionsShouldReturnSameObject() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        ESValue result = objectObject.doIndirectCall(evaluator, objectObject, "preventExtensions", new ESValue[] { object });
        assertSame(result,object);
    }
    
    @Test
    public void changingPropertyOfNonExtensibleObjectToNonConfigurableMakesObjectSealed() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.putProperty("propertyName", ESNumber.valueOf(1));
        ESValue result = objectObject.doIndirectCall(evaluator, objectObject, "preventExtensions", new ESValue[] { object });
        assertEquals(ESBoolean.valueOf(false),objectObject.doIndirectCall(evaluator, objectObject, "isSealed", new ESValue[] { result }));
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, new ESString("propertyName"), createArgumentsObject(null, ES_FALSE, null, null, null, null) });
        assertEquals(ESBoolean.valueOf(true),objectObject.doIndirectCall(evaluator, objectObject, "isSealed", new ESValue[] { result }));
    }
    
    @Test
    public void objectObjectCreateShouldDefineProperties() throws Exception {
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty("propertyName", createArgumentsObject(new ESString("value"), ES_FALSE, ES_FALSE, ES_FALSE, null, null));
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        ESValue objectCreated = objectObject.doIndirectCall(evaluator, objectObject, "create", new ESValue[] { object, argumentsObject });
        
        ObjectPrototype result = (ObjectPrototype)objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { objectCreated, propertyName });
        assertEquals(ES_FALSE,result.getProperty(StandardProperty.WRITABLEstring));
        assertEquals(ES_FALSE,result.getProperty(StandardProperty.ENUMERABLEstring));
        assertEquals(ES_FALSE,result.getProperty(StandardProperty.CONFIGURABLEstring));
        assertEquals(new ESString("value"),result.getProperty(StandardProperty.VALUEstring));
        
        assertSame(object,((ESObject)objectCreated).getPrototype());
    }
    
    @Test
    public void keysShouldReturnNamesOfEnumerableProperties() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.putProperty("name",ES_TRUE);
        ArrayPrototype result = (ArrayPrototype) objectObject.doIndirectCall(evaluator, objectObject, "keys", new ESValue[] { object });
        assertEquals(1,result.size());
        assertEquals("name",result.getProperty(0).toString());
    }
    
    @Test
    public void keysShouldntIncludeHiddenProperties() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.putProperty("name",ES_TRUE);
        object.putHiddenProperty("hidden",ES_TRUE);
        ArrayPrototype result = (ArrayPrototype) objectObject.doIndirectCall(evaluator, objectObject, "keys", new ESValue[] { object });
        assertEquals(1,result.size());
        assertEquals("name",result.getProperty(0).toString());
    }
    
    private void definePropertyOnExistingProperty(String descPropertyName, boolean stateToTest)
            throws EcmaScriptException, NoSuchMethodException {
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(descPropertyName, ESBoolean.valueOf(stateToTest));
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.putProperty("propertyName", ESUndefined.theUndefined);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        ObjectPrototype result = (ObjectPrototype)objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { object, propertyName });
        assertEquals(ESBoolean.valueOf(stateToTest),result.getProperty(descPropertyName));
    }
    
    private ESValue createFunction(String... params) throws EcmaScriptException {
        ESObject functionObject = (ESObject) evaluator.getGlobalObject().getProperty("Function", "Function".hashCode());
        ArrayList<ESValue> paramArray = new ArrayList<ESValue>();
        for (String string : params) {
            paramArray.add(new ESString(string));
        }
        ESObject function = functionObject.doConstruct(functionObject, paramArray.toArray(new ESValue[paramArray.size()]));
        return function;
    }

    
}
