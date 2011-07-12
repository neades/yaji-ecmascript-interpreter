package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;
import FESI.Util.EvaluatorAccess;


public class GlobalObjectTest  {

    private Evaluator evaluator;
    private GlobalObject globalObject;
    @Before
    public void setUp() throws Exception {
        evaluator = new Evaluator();
        globalObject = (GlobalObject) evaluator.getGlobalObject();
    }

    @After 
    public void tearDown() throws Exception {
        EvaluatorAccess.setAccessor(null);
    }
    
    @Test
    public void testIsNaNOnUndefined(){
        try {
            ESValue result = globalObject.doIndirectCall(evaluator, globalObject, "isNaN", new ESValue[] {ESUndefined.theUndefined});
            assertEquals(true, result.booleanValue());
        } catch (EcmaScriptException e) {
            fail("EcmaScriptException: "+e);
        } catch (NoSuchMethodException e) {
            fail("NoSuchMethodException: "+e);
        }
    }
    
    @Test
    public void testParseFloatOnUndefined(){
        try {
            ESValue result = globalObject.doIndirectCall(evaluator, globalObject, "parseFloat", new ESValue[] {ESUndefined.theUndefined});
            assertEquals("NaN", result.toString());
        } catch (EcmaScriptException e) {
            fail("EcmaScriptException: "+e);
        } catch (NoSuchMethodException e) {
            fail("NoSuchMethodException: "+e);
        }
    }

    
    @Test
    public void testParseFloatNoArgs(){
        try {
            ESValue result = globalObject.doIndirectCall(evaluator, globalObject, "parseFloat", new ESValue[] {});
            assertEquals("NaN", result.toString());
        } catch (EcmaScriptException e) {
            fail("EcmaScriptException: "+e);
        } catch (NoSuchMethodException e) {
            fail("NoSuchMethodException: "+e);
        }
    }
    
    @Test
    public void testParseIntNoArgs(){
        try {
            ESValue result = globalObject.doIndirectCall(evaluator, globalObject, "parseInt", new ESValue[] {});
            assertEquals("NaN", result.toString());
        } catch (EcmaScriptException e) {
            fail("EcmaScriptException: "+e);
        } catch (NoSuchMethodException e) {
            fail("NoSuchMethodException: "+e);
        }
    }

}
