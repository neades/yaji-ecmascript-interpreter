package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;


public class ArrayPrototypeTest extends EvaluatorTestCase{

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
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

    @Test
    public void toStringReturnsArrayContents() throws Exception {
        ESObject object = arrayObject.doConstruct(new ESValue[] { new ESString("a"), new ESString("b"), new ESString("c") });
        ESValue value = object.doIndirectCall(evaluator, object, "toString", null);

        assertEquals("a,b,c", value.toESString().toString());
    }
}