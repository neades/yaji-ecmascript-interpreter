package FESI.Data;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.TypeError;


public class GlobalObjectTest extends EvaluatorTestCase {
    

    private static final ESValue[] UNDEFINED_ARGS = new ESValue[] {ESUndefined.theUndefined};
    private static final ESValue[] NO_ARGS = new ESValue[] {};

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After public void tearDown() throws Exception {
        super.tearDown();
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

    @Test
    public void NaNshouldNotBeWritable() throws Exception {
        globalObject.putProperty("NaN", ESNumber.valueOf(0));
        assertTrue(Double.isNaN(globalObject.getProperty("NaN").doubleValue()));
    }
    
    @Test(expected=TypeError.class)
    public void assignmentToNaNshouldThrowTypeErrorInStrictMode() throws Exception {
        evaluator.setStrictMode(true);
        globalObject.putProperty("NaN", ESNumber.valueOf(0));
        assertTrue(Double.isNaN(globalObject.getProperty("NaN").doubleValue()));
    }
}
