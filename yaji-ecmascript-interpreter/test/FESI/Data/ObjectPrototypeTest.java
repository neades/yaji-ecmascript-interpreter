package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.TypeError;

public class ObjectPrototypeTest extends EvaluatorTestCase {


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void toStringShouldIncludeClass() throws Exception {
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        ESValue result = object.doIndirectCall(evaluator, object, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("[object Object]"), result);
    }

    @Test
    public void toLocaleStringShouldBeImplemented() throws Exception {
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        ESValue result = object.doIndirectCall(evaluator, object, "toLocaleString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("[object Object]"), result);
    }
    
    @Test
    public void toLocaleStringShouldInvokeToString() throws Exception {
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.putProperty("toString", TestHelpers.createFunction(evaluator, "return '[myobject Object]';"));
        ESValue result = object.doIndirectCall(evaluator, object, "toLocaleString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("[myobject Object]"), result);
    }

    @Test(expected=TypeError.class)
    public void toLocaleStringShouldThrowTypeErrorIfToStringNotAFunction() throws Exception {
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.putProperty("toString",ESNull.theNull);
        object.doIndirectCall(evaluator, object, "toLocaleString", ESValue.EMPTY_ARRAY);
    }

    @Test
    public void mathShouldReturnIndicator() throws Exception {
        ESObject object = (ESObject) evaluator.getGlobalObject().getProperty("Math");
        ESValue result = object.doIndirectCall(evaluator, object, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("[object Math]"), result);
    }
    
    @Test
    public void valueOfObjectReturnsSelf() throws Exception {
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        ESValue result = object.doIndirectCall(evaluator, object, "valueOf", ESValue.EMPTY_ARRAY);
        assertSame(object, result);
    }

    @Test
    public void hasOwnPropertyReturnsTrueForProperty() throws Exception {
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.putProperty("aProperty", ESNull.theNull);
        ESValue result = object.doIndirectCall(evaluator, object, "hasOwnProperty", new ESValue[] { new ESString("aProperty") });
        assertSame(ESBoolean.valueOf(true), result);
    }

    @Test
    public void hasOwnPropertyIgnoresInherited() throws Exception {
        ESObject parentObject = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        parentObject.putProperty("aProperty", ESNull.theNull);
        ESObject object = (ESObject) objectObject.doIndirectCall(evaluator, objectObject, "create", new ESValue[] { parentObject });
        ESValue result = object.doIndirectCall(evaluator, object, "hasOwnProperty", new ESValue[] { new ESString("aProperty") });
        
        assertSame(ESBoolean.valueOf(false), result);
    }

    @Test
    public void isPrototypeOfNonObjectReturnsFalse() throws Exception {
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        ESValue result = object.doIndirectCall(evaluator, object, "isPrototypeOf", new ESValue[] { ESNull.theNull });
        
        assertSame(ESBoolean.valueOf(false), result);
    }

    @Test
    public void isPrototypeOfCanReturnTrueForPrototype() throws Exception {
        ESObject protoObject = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        ESObject object = (ESObject) objectObject.doIndirectCall(evaluator, objectObject, "create", new ESValue[] { protoObject });
        ESValue result = protoObject.doIndirectCall(evaluator, protoObject, "isPrototypeOf", new ESValue[] { object });
        
        assertSame(ESBoolean.valueOf(true), result);
    }

    @Test
    public void isPrototypeOfCanReturnTrueForChainedPrototype() throws Exception {
        ESObject protoObject = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        ESObject proto1Object = (ESObject) objectObject.doIndirectCall(evaluator, objectObject, "create", new ESValue[] { protoObject });
        ESObject object = (ESObject) objectObject.doIndirectCall(evaluator, objectObject, "create", new ESValue[] { proto1Object });
        ESValue result = protoObject.doIndirectCall(evaluator, protoObject, "isPrototypeOf", new ESValue[] { object });
        
        assertSame(ESBoolean.valueOf(true), result);
    }

    @Test
    public void isPrototypeOfCanReturnFalseIfNotPrototype() throws Exception {
        ESObject protoObject = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        ESObject object = (ESObject) objectObject.doIndirectCall(evaluator, objectObject, "create", new ESValue[] { ESNull.theNull });
        ESValue result = protoObject.doIndirectCall(evaluator, protoObject, "isPrototypeOf", new ESValue[] { object });
        
        assertSame(ESBoolean.valueOf(false), result);
    }

    @Test
    public void propertyIsEnumerableReturnsTrueForStandardProperty() throws Exception {
        ESObject protoObject = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        ESObject object = (ESObject) objectObject.doIndirectCall(evaluator, objectObject, "create", new ESValue[] { protoObject });
        object.putProperty("aProperty", ESNull.theNull);
        ESValue result = object.doIndirectCall(evaluator, object, "propertyIsEnumerable", new ESValue[] { new ESString("aProperty") });
        
        assertSame(ESBoolean.valueOf(true), result);
    }

    @Test
    public void propertyIsEnumerableReturnsFalseForPrototype() throws Exception {
        ESObject protoObject = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        protoObject.putProperty("aProperty", ESNull.theNull);
        ESObject object = (ESObject) objectObject.doIndirectCall(evaluator, objectObject, "create", new ESValue[] { protoObject });
        ESValue result = object.doIndirectCall(evaluator, object, "propertyIsEnumerable", new ESValue[] { new ESString("aProperty") });
        
        assertSame(ESBoolean.valueOf(false), result);
    }

    @Test
    public void propertyIsEnumerableReturnsFalseForNonExistentProperty() throws Exception {
        ESObject protoObject = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        ESObject object = (ESObject) objectObject.doIndirectCall(evaluator, objectObject, "create", new ESValue[] { protoObject });
        ESValue result = object.doIndirectCall(evaluator, object, "propertyIsEnumerable", new ESValue[] { new ESString("aProperty") });
        
        assertSame(ESBoolean.valueOf(false), result);
    }
}
