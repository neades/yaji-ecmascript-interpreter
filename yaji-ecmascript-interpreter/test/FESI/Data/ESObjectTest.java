package FESI.Data;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;
import FESI.Util.EvaluatorAccess;

public class ESObjectTest extends EvaluatorTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }
    
    @After public void tearDown() throws Exception {
        EvaluatorAccess.setAccessor(null);
    }
    
    @Test public void testNewObjectNotFrozen() throws Exception {
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        
        assertFalse("A new object is extensible, so it is not frozen.",object.isFrozen());
    }

    @Test public void testFrozenObjectShouldFailOnPut() throws Exception {
        evaluator.setStrictMode(true);
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.putProperty("x", ESString.valueOf("Y"), "x".hashCode());
        try {
            object.freeze();
            object.putProperty("x", ESString.valueOf("Z"),  "x".hashCode());
            fail("Should have thrown a TypeError ");
        } catch (TypeError typeError) {
            // OK
        }
    }
    
    @Test public void testShouldReturnNullEvaluatorAfterFreezingIfNotOtherwiseSpecified() throws Exception {
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.freeze();
        assertNull(object.getEvaluator());
    }
    
    @Test public void testShouldReturnEvaluatorSpecifiedThroughUtilAfterFreezing() throws Exception {
        final Evaluator localEvaluator = new Evaluator();
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.freeze();
        setUtilEvaluatorAccessor(localEvaluator);
        assertSame(localEvaluator,object.getEvaluator());
    }

    private void setUtilEvaluatorAccessor(final Evaluator localEvaluator) {
        FESI.Util.EvaluatorAccess.setAccessor(new FESI.Util.IEvaluatorAccess() {

            public Evaluator getEvaluator() {
                return localEvaluator;
            }
            
        });
    }
    
    @Test public void testFrozenObjectShouldIgnorePut() throws Exception {
        evaluator.setStrictMode(false);
        setUtilEvaluatorAccessor(evaluator);
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.putProperty("x", ESString.valueOf("Y"), "x".hashCode());
        object.freeze();
        object.putProperty("x", ESString.valueOf("Z"),  "x".hashCode());
        assertEquals(ESString.valueOf("Y"),object.getProperty("x", "x".hashCode()));
    }
    
    @Test public void testDeletingAFrozenPropertyShouldFail() throws Exception {
        evaluator.setStrictMode(true);
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.putProperty("x", ESString.valueOf("Y"), "x".hashCode());
        try {
            object.freeze();
            object.deleteProperty("x", "x".hashCode());
            fail("Should have thrown a TypeError ");
        } catch (TypeError typeError) {
            // OK
        }
    }
    
    @Test public void testDeletingAFrozenPropertyShouldBeIgnored() throws Exception {
        evaluator.setStrictMode(false);
        setUtilEvaluatorAccessor(evaluator);
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.putProperty("x", ESString.valueOf("Y"), "x".hashCode());
        object.freeze();
        object.deleteProperty("x", "x".hashCode());
        assertEquals(ESString.valueOf("Y"),object.getProperty("x", "x".hashCode()));
    }
    
    @Test public void testFrozenObjectShouldFailOnAdd() throws Exception {
        evaluator.setStrictMode(true);
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        try {
            object.freeze();
            object.putProperty("x", ESString.valueOf("Z"),  "x".hashCode());
            fail("Should have thrown a TypeError ");
        } catch (TypeError typeError) {
            // OK
        }
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
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.putProperty("1", ESString.valueOf("81"), "1".hashCode());
        object.freeze();
        
        assertTrue("the easiest way for an object to be frozen is if Object.freeze has been called on it.",object.isFrozen());
    }
/*
    // By definition, a frozen object is non-extensible.
    assert(Object.isExtensible(frozen) === false);

    // Also by definition, a frozen object is sealed.
    assert(Object.isSealed(frozen) === true);
*/
}
