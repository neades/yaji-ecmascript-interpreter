package FESI.Data;

import static org.junit.Assert.assertEquals;
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
    
    private Evaluator evaluator;
    private BuiltinFunctionObject objectObject;
    @Before
    public void setUp() throws Exception {
        evaluator = new Evaluator();
        
        EvaluatorAccess.setAccessor(new IEvaluatorAccess() {
            
            public Evaluator getEvaluator() {
                return evaluator;
            }
        });
        
        objectObject = (BuiltinFunctionObject) evaluator.getGlobalObject().getProperty("Object","Object".hashCode());
    }
    
    @Test public void testNewObjectNotFrozen() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        
        assertEquals("A new object is extensible, so it is not frozen.",ESBoolean.valueOf(false),
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
                ESBoolean.valueOf(true),
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
        assertEquals(ESBoolean.valueOf(true),result.getProperty(StandardProperty.WRITABLEstring));
        assertEquals(ESBoolean.valueOf(true),result.getProperty(StandardProperty.ENUMERABLEstring));
        assertEquals(ESBoolean.valueOf(true),result.getProperty(StandardProperty.CONFIGURABLEstring));
        assertEquals(ESNull.theNull,result.getProperty(StandardProperty.VALUEstring));
    }
    
    @Test
    public void getOwnPropertyShouldReturnIndicateFieldWritable() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.putProperty("test", ESNull.theNull, "test".hashCode());
        object.freeze();
        ObjectPrototype result = (ObjectPrototype)objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { object, new ESString("test") });
        assertEquals(ESBoolean.valueOf(false),result.getProperty(StandardProperty.WRITABLEstring));
    }
    
    @Test
    public void getOwnPropertyShouldIndicateFieldEnumerable() throws Exception {
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        object.putHiddenProperty("test", ESNull.theNull);
        ObjectPrototype result = (ObjectPrototype)objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { object, new ESString("test") });
        assertEquals(ESBoolean.valueOf(false),result.getProperty(StandardProperty.ENUMERABLEstring));
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
        assertEquals(ESBoolean.valueOf(true),result.getProperty(StandardProperty.ENUMERABLEstring));
        assertEquals(ESBoolean.valueOf(true),result.getProperty(StandardProperty.CONFIGURABLEstring));
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
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ESBoolean.valueOf(false));
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
        assertEquals(ESBoolean.valueOf(false),result.getProperty(StandardProperty.WRITABLEstring));
        assertEquals(ESBoolean.valueOf(false),result.getProperty(StandardProperty.ENUMERABLEstring));
        assertEquals(ESBoolean.valueOf(false),result.getProperty(StandardProperty.CONFIGURABLEstring));
        assertEquals(ESUndefined.theUndefined,result.getProperty(StandardProperty.VALUEstring));
    }
    
    @Test(expected=TypeError.class)
    public void shouldRejectIfNotConfigurable() throws Exception {
        // 8.12.9.7a
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ESBoolean.valueOf(false));
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ESBoolean.valueOf(true));
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }

    @Test(expected=TypeError.class)
    public void shouldRejectChangingEnumerablityOfNonConfigurableProperty() throws Exception {
        // 8.12.9.7b
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ESBoolean.valueOf(false));
        argumentsObject.putProperty(StandardProperty.ENUMERABLEstring, ESBoolean.valueOf(true));
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        argumentsObject.putProperty(StandardProperty.ENUMERABLEstring, ESBoolean.valueOf(false));
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }

    @Test(expected=TypeError.class)
    public void shouldRejectChangingAccessorUsageOfNonConfigurableProperty() throws Exception {
        // 8.12.9.9a
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ESBoolean.valueOf(false));
        argumentsObject.putProperty(StandardProperty.VALUEstring, ESBoolean.valueOf(true));
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        ESValue v = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        v.setGetAccessorDescriptor(createFunction("return null;"));
        v.setSetAccessorDescriptor(createFunction("return void 0;"));
        argumentsObject.putProperty(StandardProperty.VALUEstring,v);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }
    
    @Test
    public void changingToAccessorPropertyShouldRetainConfigurableAndEnumerableAttributes() throws Exception {
        // 8.12.9.9b
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ESBoolean.valueOf(true));
        argumentsObject.putProperty(StandardProperty.ENUMERABLEstring, ESBoolean.valueOf(true));
        argumentsObject.putProperty(StandardProperty.VALUEstring, ESBoolean.valueOf(true));
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        
        argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        ESValue v = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        v.setGetAccessorDescriptor(createFunction("return null;"));
        v.setSetAccessorDescriptor(createFunction("return void 0;"));
        argumentsObject.putProperty(StandardProperty.VALUEstring,v);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        
        ObjectPrototype result = (ObjectPrototype)objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { object, propertyName });
        assertEquals(ESUndefined.theUndefined,result.getProperty(StandardProperty.WRITABLEstring));
        assertEquals(ESBoolean.valueOf(true),result.getProperty(StandardProperty.ENUMERABLEstring));
        assertEquals(ESBoolean.valueOf(true),result.getProperty(StandardProperty.CONFIGURABLEstring));
        assertEquals(ESUndefined.theUndefined,result.getProperty(StandardProperty.VALUEstring));
    }

    @Test
    public void changingToDataPropertyShouldRetainConfigurableAndEnumerableAttributes() throws Exception {
        // 8.12.9.9b
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ESBoolean.valueOf(true));
        argumentsObject.putProperty(StandardProperty.ENUMERABLEstring, ESBoolean.valueOf(true));
        ESValue v = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        v.setGetAccessorDescriptor(createFunction("return null;"));
        v.setSetAccessorDescriptor(createFunction("return void 0;"));
        argumentsObject.putProperty(StandardProperty.VALUEstring,v);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.VALUEstring,new ESString("changed"));
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        
        ObjectPrototype result = (ObjectPrototype)objectObject.doIndirectCall(evaluator, objectObject, "getOwnPropertyDescriptor", new ESValue[] { object, propertyName });
        assertEquals(ESBoolean.valueOf(false),result.getProperty(StandardProperty.WRITABLEstring));
        assertEquals(ESBoolean.valueOf(true),result.getProperty(StandardProperty.ENUMERABLEstring));
        assertEquals(ESBoolean.valueOf(true),result.getProperty(StandardProperty.CONFIGURABLEstring));
        assertEquals(new ESString("changed"),result.getProperty(StandardProperty.VALUEstring));
    }
    
    @Test(expected=TypeError.class)
    public void shouldDisallowChangingWritableIfNonConfigurable() throws Exception {
        // 8.12.9.10.a.i
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ESBoolean.valueOf(false));
        argumentsObject.putProperty(StandardProperty.ENUMERABLEstring, ESBoolean.valueOf(true));
        argumentsObject.putProperty(StandardProperty.WRITABLEstring, ESBoolean.valueOf(false));
        argumentsObject.putProperty(StandardProperty.VALUEstring, ESBoolean.valueOf(true));
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        
        argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.WRITABLEstring, ESBoolean.valueOf(true));
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }

    @Test(expected=TypeError.class)
    public void shouldDisallowChangingValueIfNonConfigurable() throws Exception {
        // 8.12.9.10.a.ii.TRUE
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ESBoolean.valueOf(false));
        argumentsObject.putProperty(StandardProperty.ENUMERABLEstring, ESBoolean.valueOf(true));
        argumentsObject.putProperty(StandardProperty.WRITABLEstring, ESBoolean.valueOf(false));
        argumentsObject.putProperty(StandardProperty.VALUEstring, ESBoolean.valueOf(true));
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        
        argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.VALUEstring, ESBoolean.valueOf(false));
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }

    @Test
    public void shouldAllowChangingValueIfNonConfigurableIfSameValue() throws Exception {
        // 8.12.9.10.a.ii.FALSE
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ESBoolean.valueOf(false));
        argumentsObject.putProperty(StandardProperty.ENUMERABLEstring, ESBoolean.valueOf(true));
        argumentsObject.putProperty(StandardProperty.WRITABLEstring, ESBoolean.valueOf(false));
        argumentsObject.putProperty(StandardProperty.VALUEstring, ESBoolean.valueOf(true));
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        
        argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.VALUEstring, ESBoolean.valueOf(true));
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }

    @Test(expected=TypeError.class)
    public void shouldRejectSetAccessorChangeOnNonConfigurableProperty() throws Exception {
        // 8.12.9.11.a.i
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ESBoolean.valueOf(false));
        argumentsObject.putProperty(StandardProperty.ENUMERABLEstring, ESBoolean.valueOf(true));
        ESValue v = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        v.setGetAccessorDescriptor(createFunction("return null;"));
        v.setSetAccessorDescriptor(createFunction("return void 0;"));
        argumentsObject.putProperty(StandardProperty.VALUEstring,v);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        v = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        v.setGetAccessorDescriptor(createFunction("return null;"));
        v.setSetAccessorDescriptor(createFunction("return void 0;"));
        argumentsObject.putProperty(StandardProperty.VALUEstring,v);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }
    

    @Test
    public void shouldAllowSetAccessorNoChangeOnNonConfigurableProperty() throws Exception {
        // 8.12.9.11.a.i.FALSE
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ESBoolean.valueOf(false));
        argumentsObject.putProperty(StandardProperty.ENUMERABLEstring, ESBoolean.valueOf(true));
        ESValue v = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        ESValue setFunction = createFunction("return void 0;");
        v.setSetAccessorDescriptor(setFunction);
        argumentsObject.putProperty(StandardProperty.VALUEstring,v);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        v = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        v.setSetAccessorDescriptor(setFunction);
        argumentsObject.putProperty(StandardProperty.VALUEstring,v);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }
    

    @Test(expected=TypeError.class)
    public void shouldRejectGetAccessorChangeOnNonConfigurableProperty() throws Exception {
        // 8.12.9.11.a.ii
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ESBoolean.valueOf(false));
        argumentsObject.putProperty(StandardProperty.ENUMERABLEstring, ESBoolean.valueOf(true));
        ESValue v = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        v.setGetAccessorDescriptor(createFunction("return null;"));
        argumentsObject.putProperty(StandardProperty.VALUEstring,v);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        v = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        v.setGetAccessorDescriptor(createFunction("return null;"));
        argumentsObject.putProperty(StandardProperty.VALUEstring,v);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
    }
    

    @Test
    public void shouldAllowGetAccessorNoChangeOnNonConfigurableProperty() throws Exception {
        // 8.12.9.11.a.ii.FALSE
        ESString propertyName = new ESString("propertyName");
        ESObject argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        argumentsObject.putProperty(StandardProperty.CONFIGURABLEstring, ESBoolean.valueOf(false));
        argumentsObject.putProperty(StandardProperty.ENUMERABLEstring, ESBoolean.valueOf(true));
        ESValue v = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        ESValue getFunction = createFunction("return void 0;");
        v.setGetAccessorDescriptor(getFunction);
        argumentsObject.putProperty(StandardProperty.VALUEstring,v);
        ESObject object = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
        argumentsObject = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        v = objectObject.doConstruct(null, ESValue.EMPTY_ARRAY);
        v.setGetAccessorDescriptor(getFunction);
        argumentsObject.putProperty(StandardProperty.VALUEstring,v);
        
        objectObject.doIndirectCall(evaluator, objectObject, "defineProperty", new ESValue[] { object, propertyName, argumentsObject });
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
