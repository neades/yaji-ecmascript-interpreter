package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

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

    @Test
    public void keysShouldReturnIndices() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { new ESString("a"), new ESString("b"), new ESString("c") });
        ESValue value = objectObject.doIndirectCall(evaluator, array, "keys", new ESValue[] { array } );
        assertEquals("0,1,2",value.toESString().toString());
        assertEquals(new ESString("0"), ((ESObject)value).getProperty(0));
    }

    @Test
    public void propertyIsEnumerableReturnsTrueForArrayEleemnt() throws Exception {
        ESObject object = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.putProperty(0, ESNull.theNull);
        ESValue result = object.doIndirectCall(evaluator, object, "propertyIsEnumerable", new ESValue[] { ESNumber.valueOf(0) });

        assertSame(ESBoolean.valueOf(true), result);
    }

    @Test
    public void propertyIsEnumerableReturnsTrueForPropertyOnArrayEleemnt() throws Exception {
        ESObject object = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.putProperty("wobble", ESNull.theNull);
        ESValue result = object.doIndirectCall(evaluator, object, "propertyIsEnumerable", new ESValue[] { new ESString("wobble") });

        assertSame(ESBoolean.valueOf(true), result);
    }

    @Test
    public void propertyIsEnumerableReturnsFalseForIndexOutOfRangeArrayEleemnt() throws Exception {
        ESObject object = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.putProperty("wobble", ESNull.theNull);
        ESValue result = object.doIndirectCall(evaluator, object, "propertyIsEnumerable", new ESValue[] { ESNumber.valueOf(1) });

        assertSame(ESBoolean.valueOf(false), result);
    }

}
