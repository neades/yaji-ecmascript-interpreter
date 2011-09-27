package FESI.Data;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;

public class ObjectPrototypeTest {

    private Evaluator evaluator;
    private BuiltinFunctionObject objectObject;

    @Before
    public void setUp() throws Exception {
        evaluator = new Evaluator();
        
        objectObject = (BuiltinFunctionObject) evaluator.getGlobalObject().getProperty("Object","Object".hashCode());
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
        object.putProperty("toString", createFunction("return '[myobject Object]';"));
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
    
    private ESValue createFunction(String... params) throws EcmaScriptException {
        ESObject functionObject = (ESObject) evaluator.getGlobalObject().getProperty("Function", "Function".hashCode());
        ArrayList<ESValue> paramArray = new ArrayList<ESValue>();
        for (String string : params) {
            paramArray.add(new ESString(string));
        }
        ESObject function = functionObject.doConstruct(paramArray.toArray(new ESValue[paramArray.size()]));
        return function;
    }
}