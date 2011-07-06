package FESI.Data;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import FESI.Interpreter.Evaluator;

public class ObjectObjectTest {
    
    private Evaluator evaluator;
    private BuiltinFunctionObject objectObject;
    @Before
    public void setUp() throws Exception {
        evaluator = new Evaluator();
        
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

}
