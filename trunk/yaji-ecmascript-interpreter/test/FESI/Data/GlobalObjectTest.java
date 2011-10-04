package FESI.Data;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;
import FESI.Util.EvaluatorAccess;


public class GlobalObjectTest  {
    

    private static final ESValue[] UNDEFINED_ARGS = new ESValue[] {ESUndefined.theUndefined};
    private static final ESValue[] NO_ARGS = new ESValue[] {};
    private Evaluator evaluator;
    private GlobalObject globalObject;
    @Before
    public void setUp() throws Exception {
        evaluator = new Evaluator();
        globalObject = evaluator.getGlobalObject();
    }

    @After 
    public void tearDown() throws Exception {
        EvaluatorAccess.setAccessor(null);
    }

    @Test
    public void testIsNaNOnUndefined() throws EcmaScriptException, NoSuchMethodException{
        ESValue result = globalObject.doIndirectCall(evaluator, globalObject, "isNaN", UNDEFINED_ARGS);
        assertTrue(result.booleanValue());
    }

    @Test
    public void testParseFloatOnUndefined() throws EcmaScriptException, NoSuchMethodException{
        ESValue result = globalObject.doIndirectCall(evaluator, globalObject, "parseFloat", UNDEFINED_ARGS);
        assertTrue(Double.isNaN(result.doubleValue()));
    }


    @Test
    public void testParseFloatNoArgs() throws EcmaScriptException, NoSuchMethodException{
        ESValue result = globalObject.doIndirectCall(evaluator, globalObject, "parseFloat", NO_ARGS);
        assertTrue(Double.isNaN(result.doubleValue()));
    }

    @Test
    public void testParseIntNoArgs() throws EcmaScriptException, NoSuchMethodException{
        ESValue result = globalObject.doIndirectCall(evaluator, globalObject, "parseInt", NO_ARGS);
        assertTrue(Double.isNaN(result.doubleValue()));
    }

}
