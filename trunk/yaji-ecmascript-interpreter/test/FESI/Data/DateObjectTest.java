package FESI.Data;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

public class DateObjectTest extends EvaluatorTestCase {


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void parseShouldHandleToStringFormat() throws Exception {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(1965, 9, 30, 14, 22, 36);
        Date date = new Date(calendar.getTimeInMillis());
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(ESValue.EMPTY_ARRAY);
        dp.setDate(date);
        String toStringDate = dp.doIndirectCall(evaluator, dp, "toString", ESValue.EMPTY_ARRAY ).toString();
        
        ESValue value = dateObject.doIndirectCall(evaluator, dateObject, "parse", new ESValue[] { new ESString(toStringDate) });
        assertEquals(ESNumber.valueOf(date.getTime()), value);
    }
    
    @Test
    public void parseShouldHandleToISOStringFormat() throws Exception {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(1965, 9, 30, 14, 22, 36);
        Date date = new Date(calendar.getTimeInMillis());
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(ESValue.EMPTY_ARRAY);
        dp.setDate(date);
        String toStringDate = dp.doIndirectCall(evaluator, dp, "toISOString", ESValue.EMPTY_ARRAY ).toString();
        
        ESValue value = dateObject.doIndirectCall(evaluator, dateObject, "parse", new ESValue[] { new ESString(toStringDate) });
        assertEquals(ESNumber.valueOf(date.getTime()), value);
    }
    
    @Test
    public void parseUndefinedShouldReturnNaN() throws Exception {
        
        ESValue value = dateObject.doIndirectCall(evaluator, dateObject, "parse", ESValue.EMPTY_ARRAY);
        assertTrue(Double.isNaN(value.doubleValue()));
    }
}
