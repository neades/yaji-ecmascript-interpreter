package FESI.Data;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.RangeError;
import FESI.Exceptions.TypeError;


public class DatePrototypeTest extends EvaluatorTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }
    
    @Test
    public void shouldConvertDateToIsoFormat() throws  Exception {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(1965, 9, 30, 14, 22, 36);
        Date date = new Date(calendar.getTimeInMillis()+123L);
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(ESValue.EMPTY_ARRAY);
        dp.setDate(date);
        ESValue value = dp.doIndirectCall(evaluator, dp, "toISOString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("1965-10-30T14:22:36.123Z"),value);
    }
    
    @Test(expected=RangeError.class)
    public void toISOStringThrowsRangeErrorWithInvalidDate() throws  Exception {
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(Double.NaN)});
        dp.doIndirectCall(evaluator, dp, "toISOString", ESValue.EMPTY_ARRAY);
    }
    
    @Test
    public void shouldConvertDateToJSONFormat() throws  Exception {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(1965, 9, 30, 14, 22, 36);
        Date date = new Date(calendar.getTimeInMillis()+123L);
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(ESValue.EMPTY_ARRAY);
        dp.setDate(date);
        ESValue value = dp.doIndirectCall(evaluator, dp, "toJSON", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("1965-10-30T14:22:36.123Z"),value);
    }
    
    @Test
    public void toJSONshouldCalltoISOString() throws  Exception {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(1965, 9, 30, 14, 22, 36);
        Date date = new Date(calendar.getTimeInMillis()+123L);
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(ESValue.EMPTY_ARRAY);
        dp.setDate(date);
        dp.putProperty("toISOString", TestHelpers.createFunction(evaluator, "return 'overridden';"));
        ESValue value = dp.doIndirectCall(evaluator, dp, "toJSON", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("overridden"),value);
    }
    
    @Test(expected=TypeError.class)
    public void toJSONshouldThrowTypeErrorIftoISOStringNotFunction() throws  Exception {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(1965, 9, 30, 14, 22, 36);
        Date date = new Date(calendar.getTimeInMillis()+123L);
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(ESValue.EMPTY_ARRAY);
        dp.setDate(date);
        dp.putProperty("toISOString", new ESString("return 'overridden';"));
        dp.doIndirectCall(evaluator, dp, "toJSON", ESValue.EMPTY_ARRAY);
    }
    
    @Test
    public void toJSONreturnsNullWithInvalidDate() throws  Exception {
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(Double.NaN)});
        ESValue value = dp.doIndirectCall(evaluator, dp, "toJSON", ESValue.EMPTY_ARRAY);
        assertEquals(ESNull.theNull,value);
    }
    
}
