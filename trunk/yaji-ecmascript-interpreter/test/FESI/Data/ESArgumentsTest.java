package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.TypeError;

public class ESArgumentsTest extends EvaluatorTestCase {

    private ESArguments environment;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        String[] argumentNames = new String[] { "arg1", "arg2" };
        ESValue[] argumentValues = new ESValue[] { ESNumber.valueOf(100), new ESString("value") };
        ESObject callee = createFunction("return false;");
        environment = ESArguments.makeNewESArguments(evaluator, callee, argumentNames, argumentValues);
    }

    @Test
    public void argumentsObjectShouldHaveClassArguments() throws Exception {
        ESObject arguments = (ESObject) environment.getPropertyInScope("arguments",null,"arguments".hashCode());
        assertEquals("Arguments",arguments.getESClassName());
    }

    @Test
    public void argumentsObjectShouldHaveLength() throws Exception {
        ESObject arguments = (ESObject) environment.getPropertyInScope("arguments",null,"arguments".hashCode());
        assertEquals(ESNumber.valueOf(2),arguments.getProperty(StandardProperty.LENGTHstring));
    }
    
    @Test
    public void environmentShouldHaveProperty() throws Exception {
        assertEquals(ESNumber.valueOf(100),environment.getProperty("arg1"));
    }
    
    @Test
    public void argumentsShouldHaveIndexedArguments() throws Exception {
        ESObject arguments = (ESObject) environment.getPropertyInScope("arguments",null,"arguments".hashCode());
        assertEquals(ESNumber.valueOf(100),arguments.getProperty("0"));
    }
    
    @Test
    public void argumentsShouldHaveDirectlyIndexedArguments() throws Exception {
        ESObject arguments = (ESObject) environment.getPropertyInScope("arguments",null,"arguments".hashCode());
        assertEquals(ESNumber.valueOf(100),arguments.getProperty(0L));
    }
    
    @Test
    public void argumentsShouldAllowAccessByName() throws Exception {
        ESObject arguments = (ESObject) environment.getPropertyInScope("arguments",null,"arguments".hashCode());
        assertEquals(ESNumber.valueOf(100),arguments.getProperty("arg1"));
    }
    
    @Test
    public void argumentsShouldAllowArgumentsToBeOverridden() throws Exception {
        environment.putProperty("arguments",ESNumber.valueOf(42),"arguments".hashCode());
        ESValue arguments = environment.getPropertyInScope("arguments",null,"arguments".hashCode());
        assertEquals(ESNumber.valueOf(42),arguments);
    }
    
    @Test
    public void getArgumentValueAfterNamedArguments() throws Exception {
        String[] argumentNames = new String[] { "arg1" };
        ESValue[] argumentValues = new ESValue[] { ESNumber.valueOf(100), new ESString("value") };
        ESObject callee = createFunction("return false;");
        environment = ESArguments.makeNewESArguments(evaluator, callee, argumentNames, argumentValues);

        ESObject arguments = (ESObject) environment.getPropertyInScope("arguments",null,"arguments".hashCode());
        assertEquals(ESNumber.valueOf(2),arguments.getProperty("length"));
        assertEquals(new ESString("value"), arguments.getProperty(1L));
    }

    @Test
    public void argumentsShouldAllowValuesToBeDeleted() throws Exception {
        ESObject arguments = (ESObject) environment.getPropertyInScope("arguments",null,"arguments".hashCode());
        arguments.deleteProperty("0", "0".hashCode());
        assertEquals(ESUndefined.theUndefined,arguments.getProperty(0L));
    }
    
    @Test
    public void argumentsShouldntBeDeletedIfNotConfigurable() throws Exception {
        ESObject arguments = (ESObject) environment.getPropertyInScope("arguments",null,"arguments".hashCode());
        arguments.freeze();
        assertFalse( arguments.deleteProperty("0", "0".hashCode()) );
        assertEquals(ESNumber.valueOf(100),arguments.getProperty(0L));
    }
    
    @Test
    public void argumentsShouldBeLinkedToParameters() throws Exception {
        ESObject arguments = (ESObject) environment.getPropertyInScope("arguments",null,"arguments".hashCode());
        environment.putProperty("arg1",ESNumber.valueOf(123));
        assertEquals(ESNumber.valueOf(123), arguments.getProperty(0L));
    }
    
    @Test
    public void argumentsShouldNotBeLinkedToParametersInStrictMode() throws Exception {
        evaluator.setStrictMode(true);
        ESObject arguments = (ESObject) environment.getPropertyInScope("arguments",null,"arguments".hashCode());
        environment.putProperty("arg1",ESNumber.valueOf(123));
        assertEquals(ESNumber.valueOf(100), arguments.getProperty(0L));
    }
    
    @Test(expected=TypeError.class)
    public void shouldThrowExceptonIfReadingCalleeInStrictMode() throws Exception {
        evaluator.setStrictMode(true);
        ESObject arguments = (ESObject) environment.getPropertyInScope("arguments",null,"arguments".hashCode());
        arguments.getProperty(StandardProperty.CALLEEstring);
    }
}
