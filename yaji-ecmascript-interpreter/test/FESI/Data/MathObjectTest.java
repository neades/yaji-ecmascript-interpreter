package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.EcmaScriptException;
import FESI.Util.EvaluatorAccess;

public class MathObjectTest extends EvaluatorTestCase {
    
        private static final ESValue[] NAN_ARGS = new ESValue[] {ESNumber.valueOf(2), new ESString("string"), ESNumber.valueOf(1.34)};
        private static final ESValue[] MULTI_ARGS = new ESValue[] {ESNumber.valueOf(-3.2), ESNumber.valueOf(3.2), ESNumber.valueOf(0) , ESNumber.valueOf(-1)};
        private static final ESValue[] NO_ARGS = new ESValue[] {};
        private MathObject mathObject;
        
        @Override
        @Before
        public void setUp() throws Exception {
            super.setUp();
            mathObject = (MathObject)globalObject.getProperty("Math", "Math".hashCode());
            
        }

        @After 
        public void tearDown() throws Exception {
            EvaluatorAccess.setAccessor(null);
        }
        
        @Test
        public void testMinNoArgs() throws EcmaScriptException, NoSuchMethodException{
            ESValue result = mathObject.doIndirectCall(evaluator, mathObject, "min", NO_ARGS);
            assertEquals(Double.POSITIVE_INFINITY, result.doubleValue(), 0);
        }
        
        @Test
        public void testMinNaN() throws EcmaScriptException, NoSuchMethodException{
            ESValue result = mathObject.doIndirectCall(evaluator, mathObject, "min", NAN_ARGS);
            assertTrue(Double.isNaN(result.doubleValue()));
        }
        
        @Test
        public void testMinMultiArgs() throws EcmaScriptException, NoSuchMethodException{
            ESValue result = mathObject.doIndirectCall(evaluator, mathObject, "min", MULTI_ARGS);
            assertEquals(-3.2, result.doubleValue(), 0);
        }
        
        @Test
        public void testMaxNoArgs() throws EcmaScriptException, NoSuchMethodException{
            ESValue result = mathObject.doIndirectCall(evaluator, mathObject, "max", NO_ARGS);
            assertEquals(Double.NEGATIVE_INFINITY, result.doubleValue(), 0);
        }
        
        @Test
        public void testMaxNaN() throws EcmaScriptException, NoSuchMethodException{
            ESValue result = mathObject.doIndirectCall(evaluator, mathObject, "max", NAN_ARGS);
            assertTrue(Double.isNaN(result.doubleValue()));
        }
        
        @Test
        public void testMaxMultiArgs() throws EcmaScriptException, NoSuchMethodException{
            ESValue result = mathObject.doIndirectCall(evaluator, mathObject, "max", MULTI_ARGS);
            assertEquals(3.2, result.doubleValue(), 0);
        }

}
