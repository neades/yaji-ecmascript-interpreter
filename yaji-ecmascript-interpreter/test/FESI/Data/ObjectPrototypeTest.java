package FESI.Data;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

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
    public void mathShouldReturnIndicator() throws Exception {
        ESObject object = (ESObject) evaluator.getGlobalObject().getProperty("Math");
        ESValue result = object.doIndirectCall(evaluator, object, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("[object Math]"), result);
    }
}
