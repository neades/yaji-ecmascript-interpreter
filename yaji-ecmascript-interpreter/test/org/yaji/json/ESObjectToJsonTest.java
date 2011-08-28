package org.yaji.json;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESUndefined;
import FESI.Data.ObjectObject;
import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

public class ESObjectToJsonTest {

    @Test
    public void shouldWriteObjectToString() throws Exception {
        ESObject object = newObject();
        object.putProperty("command", ESString.valueOf("version"));
        assertEquals("{\"command\":\"version\"}",serialise(object));
    }


    private ESObject newObject() {
        return ObjectObject.createObject(new Evaluator());
    }


    @Test
    public void shouldWriteObjectWithMultiplePropertiesToString() throws Exception {
        ESObject object = newObject();
        object.putProperty("command", ESString.valueOf("version"));
        object.putProperty("result", ESNumber.valueOf(1));
        assertEquals("{\"command\":\"version\",\"result\":1}",serialise(object));
    }

    private String serialise(ESObject object) throws IOException, EcmaScriptException {
        StringBuilder sb = new StringBuilder();
        object.toJson(sb, new JsonState(ESUndefined.theUndefined,ESUndefined.theUndefined), "");
        return sb.toString();
    }

}
