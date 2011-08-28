package org.yaji.json;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

import FESI.Data.ESObject;
import FESI.Data.ESValue;
import FESI.Interpreter.Evaluator;

public class JsonParserTest {

    private Evaluator evaluator;

    @Before
    public void setUp() {
        evaluator = new Evaluator();
    }
    @Test
    public void shouldParse() throws Exception {
        String source = "{\"command\":\"version\"}";

        Json json = newJson(source);
        ESObject result = (ESObject) json.Parse();
        assertEquals("version",result.getProperty("command","command".hashCode()).toString());
    }

    private Json newJson(String source) {
        Json json = new Json(new StringReader(source));
        json.setEvaluator(evaluator);
        return json;
    }
    
    @Test
    public void shouldParseStandardSample() throws Exception {
        String source = "{\n" + 
        		"      \"Image\": {\n" + 
        		"          \"Width\":  800,\n" + 
        		"          \"Height\": 600,\n" + 
        		"          \"Title\":  \"View from 15th Floor\",\n" + 
        		"          \"Thumbnail\": {\n" + 
        		"              \"Url\":    \"http://www.example.com/image/481989943\",\n" + 
        		"              \"Height\": 125,\n" + 
        		"              \"Width\":  \"100\"\n" + 
        		"          },\n" + 
        		"          \"IDs\": [116, 943, 234, 38793]\n" + 
        		"        }\n" + 
        		"   }";
        Json json = newJson(source);
        ESObject result = (ESObject) json.Parse();
        assertEquals(125,((ESObject)((ESObject)result.getProperty("Image","Image".hashCode())).getProperty("Thumbnail","Thumbnail".hashCode())).getProperty("Height","Height".hashCode()).toInteger(),0.0);
    }
    
    @Test
    public void shouldTranslateBooleanTrueToNumber() throws Exception {
        Json json = newJson("true");
        ESValue value = json.Parse();
        assertEquals(1,value.toInteger(),0.0);
        assertEquals("true",value.toString());
    }
    
    @Test
    public void shouldTranslateBooleanFalseToNumber() throws Exception {
        Json json = newJson("false");
        ESValue value = json.Parse();
        assertEquals(0,value.toInteger(),0.0);
        assertEquals("false",value.toString());
    }
    
    @Test
    public void shouldArrayToNumber() throws Exception {
        Json json = newJson("[1,2,3]");
        ESValue value = json.Parse();
        assertEquals(0,value.toInteger(),0.0);
        assertEquals("1,2,3",value.toString());
    }
    
    @Test
    public void shouldParseNull() throws Exception {
        Json json = newJson("null");
        ESValue value = json.Parse();
        assertEquals(0,value.toInteger(),0.0);
        assertEquals("null",value.toString());
    }
}
