package org.yaji.json;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import FESI.Data.ESBoolean;
import FESI.Data.ESNull;
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESUndefined;
import FESI.Data.ESValue;
import FESI.Data.ObjectObject;
import FESI.Data.ObjectPrototype;
import FESI.Interpreter.Evaluator;

public class JsonUtilTest {

    private Evaluator evaluator;

    @Before
    public void setUp() {
        evaluator = new Evaluator();
    }
    @Test
    public void testStringifyNull() throws Exception {
        assertEquals("null",JsonUtil.stringify(evaluator,ESNull.theNull,ESUndefined.theUndefined, ESUndefined.theUndefined));
    }

    @Test
    public void testStringifyTrue() throws Exception {
        assertEquals("true",JsonUtil.stringify(evaluator,ESBoolean.valueOf(true),ESUndefined.theUndefined, ESUndefined.theUndefined));
    }
    
    @Test
    public void testStringifyFalse() throws Exception {
        assertEquals("false",JsonUtil.stringify(evaluator,ESBoolean.valueOf(false),ESUndefined.theUndefined, ESUndefined.theUndefined));
    }
    
    
    @Test
    public void testIntegerNumber() throws Exception {
        assertEquals("123",JsonUtil.stringify(evaluator,ESNumber.valueOf(123),ESUndefined.theUndefined, ESUndefined.theUndefined));
    }
    
    @Test
    public void testString() throws Exception {
        assertEquals("\"123\"",JsonUtil.stringify(evaluator,new ESString("123"),ESUndefined.theUndefined, ESUndefined.theUndefined));
    }

    @Test
    public void testStringShouldEscapeQuotationMark() throws Exception {
        assertEquals("\"\\\"\\\\\\/\\b\\f\\n\\r\\t\\u0000\"",JsonUtil.stringify(evaluator,new ESString("\"\\/\b\f\n\r\t\0"),ESUndefined.theUndefined, ESUndefined.theUndefined));
    }
    
    @Test
    public void testObject() throws Exception {
        assertEquals("{}",JsonUtil.stringify(evaluator,ObjectObject.createObject(evaluator),ESUndefined.theUndefined, ESUndefined.theUndefined));
    }

    @Test
    public void testObjectWithProperties() throws Exception {
        ObjectPrototype object = ObjectObject.createObject(evaluator);
        object.putProperty("xxx", new ESString("vvv"), "xxx".hashCode());
        assertEquals("{\"xxx\":\"vvv\"}",JsonUtil.stringify(evaluator,object,ESUndefined.theUndefined, ESUndefined.theUndefined));
    }

    @Test
    public void testEmptyArray() throws Exception {
        ESObject array = evaluator.createArray();
        assertEquals("[]",JsonUtil.stringify(evaluator,array,ESUndefined.theUndefined, ESUndefined.theUndefined));
    }

    @Test
    public void testPopulatedArray() throws Exception {
        ESObject array = evaluator.createArray();
        array.doIndirectCall(evaluator, array, "push", new ESValue[] { ESNumber.valueOf(123L), new ESString("test") });
        assertEquals("[123,\"test\"]",JsonUtil.stringify(evaluator,array,ESUndefined.theUndefined, ESUndefined.theUndefined));
    }
    
    @Test
    public void testUnescapeSlashes() throws Exception {
        assertEquals("\\",JsonUtil.unescape("\"\\\\\""));
    }
    
    @Test
    public void testUnescape() throws Exception {
        assertEquals("\b\t\n\r\f\"",JsonUtil.unescape("\"\\b\\t\\n\\r\\f\\\"\""));
    }
    
    @Test
    public void testUnescapeUnicode() throws Exception {
        assertEquals("X",JsonUtil.unescape("\"\\u0058\""));
    }
}
