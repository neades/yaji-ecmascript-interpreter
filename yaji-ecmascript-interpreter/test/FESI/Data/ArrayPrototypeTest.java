package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.RangeError;


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
        ((ESObject)value).doIndirectCall(evaluator, (ESObject)value, "sort", ESValue.EMPTY_ARRAY);
        assertEquals("0,1,2",value.toESString().toString());
        assertEquals(new ESString("0"), ((ESObject)value).getProperty(0L));
    }

    @Test
    public void propertyIsEnumerableReturnsTrueForArrayEleemnt() throws Exception {
        ESObject object = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.putProperty(0L, ESNull.theNull);
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
    
    @Test(expected=RangeError.class)
    public void settingLengthTooLong() throws Exception {
        ESObject object = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.putProperty(StandardProperty.LENGTHstring, ESNumber.valueOf(0x100000000L), StandardProperty.LENGTHhash);
    }
    
    @Test
    public void settingAttributeTooLong() throws Exception {
        ESObject object = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        String string = Long.toString(0x100000000L);
        object.putProperty(string, ESNull.theNull);
        assertEquals(ESNull.theNull, object.getProperty(string));
    }
    
    @Test
    public void settingAttributeTooLongJustAddsProperty() throws Exception {
        ESObject object = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        String string = Long.toString(0x100000000L);
        object.putProperty(string, ESNull.theNull);
        assertEquals(ESNumber.valueOf(0), object.getProperty(StandardProperty.LENGTHstring));
    }
    
    @Test
    public void settingAttributeTooLongJustAddsProperty2() throws Exception {
        ESObject object = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        String string = "4294967294";
        object.putProperty(string, ESNull.theNull);
        assertEquals(ESNumber.valueOf(4294967295L), object.getProperty(StandardProperty.LENGTHstring));
    }
    
    @Test
    public void toLocaleStringUsesLocale() throws Exception {
        evaluator.setDefaultLocale(Locale.FRENCH);
        ESObject object = arrayObject.doConstruct(new ESValue[] { ESNumber.valueOf(123.456), ESNumber.valueOf(2) });
        ESValue result = object.doIndirectCall(evaluator, object, "toLocaleString", ESValue.EMPTY_ARRAY);
        String string = result.toString();
        assertEquals("123,456\u00a02",string);
    }
}
