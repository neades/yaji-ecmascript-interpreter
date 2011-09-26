package FESI.Data;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FESI.Interpreter.Evaluator;

public class ArrayPrototypeTest {

    private Evaluator evaluator;
    private BuiltinFunctionObject arrayObject;
    private BuiltinFunctionObject objectObject;

    @Before
    public void setUp() throws Exception {
        evaluator = new Evaluator();
        
        objectObject = (BuiltinFunctionObject) evaluator.getGlobalObject().getProperty("Object","Object".hashCode());
        arrayObject = (BuiltinFunctionObject) evaluator.getGlobalObject().getProperty("Array","Array".hashCode());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void keysShouldReturnIndices() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { new ESString("a"), new ESString("b"), new ESString("c") });
        ESValue value = objectObject.doIndirectCall(evaluator, array, "keys", new ESValue[] { array } );
        assertEquals("0,1,2",value.toESString().toString());
        assertEquals(new ESString("0"), ((ESObject)value).getProperty(0));
    }

}
