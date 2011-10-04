package org.yaji.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import FESI.Data.ArrayPrototype;
import FESI.Data.ESBoolean;
import FESI.Data.ESNull;
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESUndefined;
import FESI.Data.ESValue;
import FESI.Data.ObjectObject;
import FESI.Data.ObjectPrototype;
import FESI.Data.StandardProperty;
import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;

public class JsonObjectStringifyTest {

    private Evaluator evaluator;

    @Before
    public void setUp() throws Exception {
        evaluator = new Evaluator();
    }

    @Test
    public void testStringifyEmptyObject() throws EcmaScriptException, NoSuchMethodException {
        ESValue result = stringify(createObject());
        assertEquals(new ESString("{}"),result);
    }

    private ObjectPrototype createObject() {
        return ObjectObject.createObject(evaluator);
    }

    @Test
    public void testStringifyEmptyArray() throws EcmaScriptException, NoSuchMethodException {
        ESValue result = stringify(createArray());
        assertEquals(new ESString("[]"),result);
    }

    private ArrayPrototype createArray() {
        return new ArrayPrototype(evaluator.getArrayPrototype(), evaluator);
    }

    @Test
    public void testStringifyNull() throws EcmaScriptException, NoSuchMethodException {
        assertEquals(new ESString("null"),stringify(ESNull.theNull));
    }

    @Test
    public void testStringifyTrue() throws EcmaScriptException, NoSuchMethodException {
        assertEquals(new ESString("true"),stringify(ESBoolean.valueOf(true)));
    }

    @Test
    public void testStringifyFalse() throws EcmaScriptException, NoSuchMethodException {
        assertEquals(new ESString("false"),stringify(ESBoolean.valueOf(false)));
    }

    @Test
    public void testStringifyFiniteNumber() throws EcmaScriptException, NoSuchMethodException {
        assertEquals(new ESString("123.125"),stringify(ESNumber.valueOf(123.125)));
    }
    
    @Test
    public void testStringifyNaN() throws EcmaScriptException, NoSuchMethodException {
        assertEquals(new ESString("null"),stringify(ESNumber.valueOf(Double.NaN)));
    }
    
    @Test
    public void testStringifyPositiveInfinity() throws EcmaScriptException, NoSuchMethodException {
        assertEquals(new ESString("null"),stringify(ESNumber.valueOf(Double.POSITIVE_INFINITY)));
    }
    
    @Test
    public void testStringifyNegativeInfinity() throws EcmaScriptException, NoSuchMethodException {
        assertEquals(new ESString("null"),stringify(ESNumber.valueOf(Double.NEGATIVE_INFINITY)));
    }
    
    @Test
    public void testStringifyString() throws EcmaScriptException, NoSuchMethodException {
        assertEquals(new ESString("\"abc\\b\\f\\n\\r\\t\\\\\\\"\\u0001\""),stringify(new ESString("abc\b\f\n\r\t\\\"\u0001")));
    }
    
    @Test
    public void testStringifyArray() throws EcmaScriptException, NoSuchMethodException {
        ArrayPrototype array = createArray();
        array.putProperty(0,new ESString("x"));
        assertEquals(new ESString("[\"x\"]"),stringify(array));
    }
    
    @Test
    public void testStringifyArrayWithReplacer() throws EcmaScriptException, NoSuchMethodException {
        ArrayPrototype array = createArray();
        array.putProperty(0,new ESString("x"));
        array.putProperty(1,new ESString("q"));
        assertEquals(new ESString("[\"y\",\"q\"]"),stringify(array,createFunction("return key==='0'?'y':value;")));
    }
    
    @Test
    public void testStringifyArrayWithUndefinedFromReplacer() throws EcmaScriptException, NoSuchMethodException {
        ArrayPrototype array = createArray();
        array.putProperty(0,new ESString("x"));
        array.putProperty(1,new ESString("q"));
        assertEquals(new ESString("[null,\"q\"]"),stringify(array,createFunction("return key==='0'?undefined:value;")));
    }
    
    @Test
    public void testStringifyObjectPropertyWithReplacer() throws EcmaScriptException, NoSuchMethodException {
        ObjectPrototype object = createObject();
        object.putProperty("propName", ESNull.theNull, "propName".hashCode());
        assertEquals(new ESString("{\"propName\":\"replaced\"}"),
                stringify(object,createFunction("return key==='propName'?'replaced':value;")));
    }

    @Test
    public void testStringifyObjectPropertyWithFunction() throws EcmaScriptException, NoSuchMethodException {
        ObjectPrototype object = createObject();
        object.putProperty("propName", ESNull.theNull, "propName".hashCode());
        object.putProperty("f",createFunction("return key==='propName'?'replaced':value;"),"f".hashCode());
        assertEquals(new ESString("{\"propName\":null}"),stringify(object));
    }

    @Test
    public void testStringifyObjectPropertyWithToJsonFunction() throws EcmaScriptException, NoSuchMethodException {
        ObjectPrototype object = createObject();
        object.putProperty("propName", ESNull.theNull, "propName".hashCode());
        object.putProperty("toJSON",createFunction("return {r:'replaced'+key};"),"toJSON".hashCode());
        assertEquals(new ESString("{\"r\":\"replaced\"}"),stringify(object));
    }

    @Test
    public void testStringifyObjectPropertyWithToJsonFunctionWithPropertyName() throws EcmaScriptException, NoSuchMethodException {
        ObjectPrototype object = createObject();
        ObjectPrototype innerObject = createObject();
        object.putProperty("propName", innerObject, "propName".hashCode());
        innerObject.putProperty("toJSON",createFunction("return {r:'replaced'+key};"),"toJSON".hashCode());
        assertEquals(new ESString("{\"propName\":{\"r\":\"replacedpropName\"}}"),stringify(object));
    }

    @Test
    public void testStringifyShouldFormatASingleProperty() throws EcmaScriptException, NoSuchMethodException {
        ObjectPrototype object = createObject();
        object.putProperty("propName", ESNumber.valueOf(123), "propName".hashCode());
        assertEquals(new ESString("{\n  \"propName\": 123\n}"),stringify(object,ESUndefined.theUndefined,ESNumber.valueOf(2)));
    }

    @Test
    public void testStringifyArrayWithFunction() throws EcmaScriptException, NoSuchMethodException {
        ArrayPrototype array = createArray();
        array.putProperty(0,new ESString("x"));
        array.putProperty(1,createFunction("return key==='propName'?'replaced':value;"));
        array.putProperty(2,new ESString("q"));
        assertEquals(new ESString("[\"x\",null,\"q\"]"),stringify(array));
    }

    @Test
    public void testStringifyArrayFormatted() throws EcmaScriptException, NoSuchMethodException {
        ArrayPrototype array = createArray();
        array.putProperty(0,new ESString("x"));
        array.putProperty(1,new ESString("q"));
        assertEquals(new ESString("[\n  \"x\",\n  \"q\"\n]"),stringify(array,ESUndefined.theUndefined,ESNumber.valueOf(2)));
    }

    @Test
    public void testStringifyTopLevelWithReplacer() throws EcmaScriptException, NoSuchMethodException {
        ObjectPrototype object = createObject();
        object.putProperty("propName", ESNull.theNull, "propName".hashCode());
        assertEquals(new ESString("\"replaced\""),
                stringify(object,createFunction("return 'replaced';")));
    }

    @Test
    public void shouldFilterProperties() throws EcmaScriptException, NoSuchMethodException { 
        ArrayPrototype array = createArray();
        array.putProperty(0,new ESString("propName"));
        array.putProperty(1,new ESString("lastName"));
        ObjectPrototype object = createObject();
        object.putProperty("lastName", ESNull.theNull, "lastName".hashCode());
        object.putProperty("propName", ESNull.theNull, "propName".hashCode());
        object.putProperty("filteredName", ESNull.theNull, "filteredName".hashCode());
        object.putProperty("anotherFilteredName", ESNull.theNull, "anotherFilteredName".hashCode());
        assertEquals(new ESString("{\"propName\":null,\"lastName\":null}"), stringify(object,array));
    }
    
    @Test
    public void shouldDetectCyclicObjects() throws Exception {
        ObjectPrototype objectA = createObject();
        ObjectPrototype objectB = createObject();
        objectA.putProperty("b",objectB,"b".hashCode());
        objectB.putProperty("a",objectA,"a".hashCode());
        try {
            stringify(objectA);
            fail("Should throw exception");
        } catch( TypeError e ) {
            // expected exceptions
        }
    }
    
    @Test
    public void shouldDetectCyclicArrays() throws Exception {
        ArrayPrototype arrayA = createArray();
        ArrayPrototype arrayB = createArray();
        arrayA.putProperty(0,arrayB);
        arrayB.putProperty(0,arrayA);
        try {
            stringify(arrayA);
            fail("Should throw exception");
        } catch( TypeError e ) {
            // expected exceptions
        }
    }
    
    @Test
    public void shouldReturnUndefinedIfCannotStringify() throws Exception {
        assertSame(ESUndefined.theUndefined,stringify());
        assertSame(ESUndefined.theUndefined,stringify(evaluator.getGlobalObject().getProperty("Function", "Function".hashCode())));
    }
    
    private ESObject createFunction(String replacerBody)
            throws EcmaScriptException {
        ESObject functionObject = (ESObject) evaluator.getGlobalObject().getProperty("Function", "Function".hashCode());
        ESObject function = functionObject.doConstruct(new ESValue[] { new ESString("key"), new ESString("value"), new ESString(replacerBody) });
        return function;
    }
    
    private ESValue stringify(ESValue... values) throws EcmaScriptException, NoSuchMethodException {
        ESObject jsonObject = (ESObject) evaluator.getGlobalObject().getProperty(StandardProperty.JSONstring,StandardProperty.JSONhash);
        return jsonObject.doIndirectCall(evaluator, jsonObject, "stringify", values);
    }


}
