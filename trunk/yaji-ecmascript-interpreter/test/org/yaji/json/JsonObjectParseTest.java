package org.yaji.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Enumeration;

import org.junit.Before;
import org.junit.Test;

import FESI.Data.ArrayPrototype;
import FESI.Data.ESBoolean;
import FESI.Data.ESNull;
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESValue;
import FESI.Data.ObjectPrototype;
import FESI.Data.StandardProperty;
import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.SyntaxError;
import FESI.Interpreter.Evaluator;

public class JsonObjectParseTest {

    private Evaluator evaluator;

    @Before
    public void setUp() throws Exception {
        evaluator = new Evaluator();
    }

    @Test
    public void testClassname() throws Exception {
        ESObject jsonObject = (ESObject) evaluator.getGlobalObject().getProperty(StandardProperty.JSONstring,StandardProperty.JSONhash);
        assertEquals(StandardProperty.JSONstring,jsonObject.getESClassName());
    }

    @Test
    public void testParseNullLiteral() throws Exception {
        assertSame(ESNull.theNull, parse("null") );
    }
    
    @Test(expected=SyntaxError.class)
    public void shouldThrowException() throws EcmaScriptException, NoSuchMethodException {
        parse("function() {}");
    }
    
    @Test(expected=SyntaxError.class)
    public void shouldThrowParseException() throws EcmaScriptException, NoSuchMethodException {
        parse("}");
    }

    @Test(expected=SyntaxError.class)
    public void shouldThrowParseExceptionOnUndefined() throws EcmaScriptException, NoSuchMethodException {
        ESObject jsonObject = (ESObject) evaluator.getGlobalObject().getProperty(StandardProperty.JSONstring,StandardProperty.JSONhash);
        jsonObject.doIndirectCall(evaluator, jsonObject, "parse", ESValue.EMPTY_ARRAY);
    }

    @Test
    public void testParseBooleanTrueLiteral() throws Exception {
        assertSame(ESBoolean.valueOf(true), parse("true") );
    }

    @Test
    public void testParseBooleanFalseLiteral() throws Exception {
        assertSame(ESBoolean.valueOf(false), parse("false") );
    }

    @Test
    public void testParseStringLiteral() throws Exception {
        assertEquals(new ESString("false"), parse("\"false\""));
    }

    @Test
    public void testParseNumberLiteral() throws Exception {
        assertEquals(ESNumber.valueOf(-1.25e45), parse("-1.25e45"));
    }
    
    @Test
    public void testParseIntegerLiteral() throws Exception {
        assertEquals(ESNumber.valueOf(1), parse("1"));
    }

    private ESValue parse(String src) throws EcmaScriptException,
            NoSuchMethodException {
        ESObject jsonObject = (ESObject) evaluator.getGlobalObject().getProperty(StandardProperty.JSONstring,StandardProperty.JSONhash);
        return jsonObject.doIndirectCall(evaluator, jsonObject, "parse", new ESValue[] { new ESString(src) });
    }
    
    @Test
    public void testParseArray() throws Exception {
        ESObject value = (ESObject) parse("[1,2,3]");
        assertTrue(value instanceof ArrayPrototype);
        assertEquals(ESNumber.valueOf(3),value.getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash));
        assertEquals(ESNumber.valueOf(1),value.getProperty(0));
        assertEquals(ESNumber.valueOf(2),value.getProperty(1));
        assertEquals(ESNumber.valueOf(3),value.getProperty(2));
    }
    
    @Test
    public void testParseEmptyArray() throws Exception {
        ESObject value = (ESObject) parse("[]");
        assertTrue(value instanceof ArrayPrototype);
        assertEquals(ESNumber.valueOf(0),value.getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash));
    }
    
    @Test
    public void testParseEmptyObject() throws Exception {
        ESObject value = (ESObject) parse("{}");
        assertTrue(value instanceof ObjectPrototype);
        Enumeration<String> allProperties = value.getAllProperties();
        assertFalse("Should have no properties",allProperties.hasMoreElements());
    }
    
    @Test
    public void testParsePopulatedObject() throws Exception {
        ESObject value = (ESObject) parse("{\"p\":1}");
        assertTrue(value instanceof ObjectPrototype);
        Enumeration<String> allProperties = value.getAllProperties();
        assertEquals("p",allProperties.nextElement());
        assertFalse("Should have no more properties",allProperties.hasMoreElements());
        assertEquals(ESNumber.valueOf(1),value.getProperty("p","p".hashCode()));
    }
    
    @Test
    public void testCallsReviver() throws Exception {
        ESObject value = parseWithReviver("return (typeof(value)=='object')?value:key+value;", "{\"p\":1}");
        assertEquals(new ESString("p1"),value.getProperty("p", "p".hashCode()));
    }
    
    @Test
    public void testCallsArrayWithReviver() throws Exception {
        ESObject value = parseWithReviver("return (typeof(value)=='object')?value:key+value;", "[{\"p\":1},2]");
        assertEquals(new ESString("p1"),((ESObject)value.getProperty(0)).getProperty("p", "p".hashCode()));
        assertEquals(new ESString("12"),value.getProperty(1));
    }
    
    @Test
    public void testReviverCanDeleteProperties() throws Exception {
        ESObject value = parseWithReviver("return (typeof(value)=='object')?value:undefined;", "{\"p\":1}");
        Enumeration<String> allProperties = value.getAllProperties();
        assertFalse("Should have no properties",allProperties.hasMoreElements());
    }

    private ESObject parseWithReviver(String reviverBody, String jsonText)
            throws EcmaScriptException, NoSuchMethodException {
        ESObject functionObject = (ESObject) evaluator.getGlobalObject().getProperty("Function", "Function".hashCode());
        ESObject function = functionObject.doConstruct(new ESValue[] { new ESString("key"), new ESString("value"), new ESString(reviverBody) });
        ESObject jsonObject = (ESObject) evaluator.getGlobalObject().getProperty(StandardProperty.JSONstring,StandardProperty.JSONhash);
        return (ESObject) jsonObject.doIndirectCall(evaluator, jsonObject, "parse", new ESValue[] { new ESString(jsonText), function });
    }
}
