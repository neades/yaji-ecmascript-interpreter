package FESI.Data;

import static org.junit.Assert.assertEquals;
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
    public void testParseFloatInfinity() throws EcmaScriptException, NoSuchMethodException{
        ESValue result = globalObject.doIndirectCall(evaluator, globalObject, "parseFloat", new ESValue[] { new ESString("  Infinity and beyond") });
        assertTrue(Double.isInfinite(result.doubleValue()));
    }

    @Test
    public void testParseFloat1e() throws EcmaScriptException, NoSuchMethodException{
        ESValue result = globalObject.doIndirectCall(evaluator, globalObject, "parseFloat", new ESValue[] { new ESString("1.0eX") });
        assertEquals(1.0,result.doubleValue(),0.0);
    }

    @Test
    public void testParseFloatPlusPoint1() throws EcmaScriptException, NoSuchMethodException{
        ESValue result = globalObject.doIndirectCall(evaluator, globalObject, "parseFloat", new ESValue[] { new ESString("+.25") });
        assertEquals(0.25,result.doubleValue(),0.0);
    }

    @Test
    public void testParseFloatMinus11eDotEMinus1() throws EcmaScriptException, NoSuchMethodException{
        ESValue result = globalObject.doIndirectCall(evaluator, globalObject, "parseFloat", new ESValue[] { new ESString("-11.e-1string") });
        assertEquals(-1.1,result.doubleValue(),0.0);
    }

    @Test
    public void testParseIntNoArgs() throws EcmaScriptException, NoSuchMethodException{
        ESValue result = globalObject.doIndirectCall(evaluator, globalObject, "parseInt", NO_ARGS);
        assertTrue(Double.isNaN(result.doubleValue()));
    }

    @Test
    public void testParseInt1() throws EcmaScriptException, NoSuchMethodException{
        ESValue result = globalObject.doIndirectCall(evaluator, globalObject, "parseInt", new ESValue[] { new ESString("1") });
        assertEquals(1.0,result.doubleValue(),0.0);
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
    
    @Test
    public void isFiniteFalseForNaN() throws Exception {
        ESValue nan = globalObject.getProperty("NaN");
        assertEquals(ESBoolean.FALSE, globalObject.doIndirectCall(evaluator, globalObject, "isFinite", new ESValue[] { nan }));
    }
    
    @Test
    public void isFiniteFalseForInfinity() throws Exception {
        ESValue infinity = globalObject.getProperty("Infinity");
        assertEquals(ESBoolean.FALSE, globalObject.doIndirectCall(evaluator, globalObject, "isFinite", new ESValue[] { infinity }));
    }

    @Test
    public void isFiniteTrueForFiniteNumber() throws Exception {
        ESValue value = ESNumber.valueOf(Long.MAX_VALUE);
        assertEquals(ESBoolean.TRUE, globalObject.doIndirectCall(evaluator, globalObject, "isFinite", new ESValue[] { value }));
    }
    
    @Test
    public void isFiniteFalseForUndefined() throws Exception {
        assertEquals(ESBoolean.FALSE, globalObject.doIndirectCall(evaluator, globalObject, "isFinite", new ESValue[] { ESUndefined.theUndefined }));
    }
    
    @Test
    public void isFiniteTrueForNull() throws Exception {
        assertEquals(ESBoolean.TRUE, globalObject.doIndirectCall(evaluator, globalObject, "isFinite", new ESValue[] { ESNull.theNull }));
    }
}
