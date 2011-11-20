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
        evaluator.setDefaultTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
    }

    @Test
    public void parseShouldHandleToStringFormat() throws Exception {
        Date date = createDate(1965, 9, 30, 14, 22, 36);
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(ESValue.EMPTY_ARRAY);
        dp.setDate(date);
        String toStringDate = dp.doIndirectCall(evaluator, dp, "toString", ESValue.EMPTY_ARRAY ).toString();
        
        ESValue value = dateObject.doIndirectCall(evaluator, dateObject, "parse", new ESValue[] { new ESString(toStringDate) });
        assertEquals(ESNumber.valueOf(date.getTime()), value);
    }
    
    @Test
    public void parseShouldHandleToISOStringFormat() throws Exception {
        Date date = createDate(1965, 9, 30, 14, 22, 36);
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
    
    @Test
    public void invokingConstructorAsFunctionShouldReturnDateAsString() throws Exception {
        Calendar now = Calendar.getInstance();
        ESValue value = dateObject.callFunction(dateObject, ESValue.EMPTY_ARRAY);
        assertEquals(ESValue.EStypeString, value.getTypeOf());
        assertTrue(value.toString().contains(Integer.toString(now.get(Calendar.YEAR))));
    }
    
    @Test
    public void constructorShouldParseSingleArgument() throws Exception {
        Date date = createDate(1965, 9, 30, 14, 22, 36);
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(ESValue.EMPTY_ARRAY);
        dp.setDate(date);
        ESValue toStringDate = dp.doIndirectCall(evaluator, dp, "toISOString", ESValue.EMPTY_ARRAY );

        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { toStringDate });
        assertEquals(toStringDate,object.doIndirectCall(evaluator, object, "toISOString", ESValue.EMPTY_ARRAY ));
    }
    
    @Test
    public void constructorShouldHandleTwoArgs() throws Exception {
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(1995), ESNumber.valueOf(9) });
        assertEquals("Sun Oct 01 00:00:00 EST 1995",object.doIndirectCall(evaluator, object, "toString", ESValue.EMPTY_ARRAY ).toString());
    }
    
    @Test
    public void constructorShouldHandleThreeArgs() throws Exception {
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(1995), ESNumber.valueOf(9), ESNumber.valueOf(22) });
        assertEquals("Sun Oct 22 00:00:00 EST 1995",object.doIndirectCall(evaluator, object, "toString", ESValue.EMPTY_ARRAY ).toString());
    }
    
    @Test
    public void constructorShouldHandleFourArgs() throws Exception {
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(2011), ESNumber.valueOf(9), ESNumber.valueOf(22), ESNumber.valueOf(13) });
        assertEquals("Sat Oct 22 13:00:00 EST 2011",object.doIndirectCall(evaluator, object, "toString", ESValue.EMPTY_ARRAY ).toString());
    }
    
    @Test
    public void constructorShouldHandleFiveArgs() throws Exception {
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(2011), ESNumber.valueOf(9), ESNumber.valueOf(22), ESNumber.valueOf(13), ESNumber.valueOf(46) });
        assertEquals("Sat Oct 22 13:46:00 EST 2011",object.doIndirectCall(evaluator, object, "toString", ESValue.EMPTY_ARRAY ).toString());
    }
    
    @Test
    public void constructorShouldHandleSixArgs() throws Exception {
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(2011), ESNumber.valueOf(9), ESNumber.valueOf(22), ESNumber.valueOf(13), ESNumber.valueOf(46), ESNumber.valueOf(9) });
        assertEquals("Sat Oct 22 13:46:09 EST 2011",object.doIndirectCall(evaluator, object, "toString", ESValue.EMPTY_ARRAY ).toString());
    }
    
    @Test
    public void constructorShouldHandleSevenArgs() throws Exception {
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(2011), ESNumber.valueOf(9), ESNumber.valueOf(22), ESNumber.valueOf(13), ESNumber.valueOf(46), ESNumber.valueOf(9), ESNumber.valueOf(456) });
        assertEquals("2011-10-22T02:46:09.456Z",object.doIndirectCall(evaluator, object, "toISOString", ESValue.EMPTY_ARRAY ).toString());
    }
    
    @Test
    public void constructorShouldHandleYearBeingPoor() throws Exception {
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { new ESString("blah"), ESNumber.valueOf(9) });
        assertTrue(Double.isNaN(object.doubleValue()));
    }
    
    @Test
    public void constructorShouldHandleYearBeingInfinite() throws Exception {
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(Double.POSITIVE_INFINITY), ESNumber.valueOf(9) });
        assertTrue(Double.isNaN(object.doubleValue()));
    }
    
    @Test
    public void constructorShouldHandleDateBeingGreaterThanMax() throws Exception {
        evaluator.setDefaultTimeZone(TimeZone.getTimeZone("UTC"));
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(1970), ESNumber.valueOf(0), ESNumber.valueOf(100000001), ESNumber.valueOf(0), ESNumber.valueOf(0), ESNumber.valueOf(0), ESNumber.valueOf(1) });
        assertTrue(Double.isNaN(object.doubleValue()));
    }
    
    @Test
    public void constructorShouldHandleDateBeingEqualMax() throws Exception {
        evaluator.setDefaultTimeZone(TimeZone.getTimeZone("UTC"));
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(1970), ESNumber.valueOf(0), ESNumber.valueOf(100000001), ESNumber.valueOf(0), ESNumber.valueOf(0), ESNumber.valueOf(0), ESNumber.valueOf(0) });
        assertTrue(!Double.isNaN(object.doubleValue()));
    }
    
    
    @Test
    public void constructorShouldHandleMonthBeingPoor() throws Exception {
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(2011),new ESString("blah"), ESNumber.valueOf(9) });
        assertTrue(Double.isNaN(object.doubleValue()));
    }
    
    @Test
    public void constructorShouldHandleDayBeingPoor() throws Exception {
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(2011), ESNumber.valueOf(9),new ESString("blah") });
        assertTrue(Double.isNaN(object.doubleValue()));
    }
    
    @Test
    public void constructorShouldHandleHourBeingPoor() throws Exception {
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(2011), ESNumber.valueOf(9), ESNumber.valueOf(22), new ESString("blah") });
        assertTrue(Double.isNaN(object.doubleValue()));
    }
    
    @Test
    public void constructorShouldHandleMinuteBeingPoor() throws Exception {
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(2011), ESNumber.valueOf(9), ESNumber.valueOf(22), ESNumber.valueOf(13), new ESString("blah") });
        assertTrue(Double.isNaN(object.doubleValue()));
    }
    
    @Test
    public void constructorShouldHandleSecondBeingPoor() throws Exception {
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(2011), ESNumber.valueOf(9), ESNumber.valueOf(22), ESNumber.valueOf(13), ESNumber.valueOf(46), new ESString("blah") });
        assertTrue(Double.isNaN(object.doubleValue()));
    }
    
    @Test
    public void constructorShouldHandleMillisecondBeingPoor() throws Exception {
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(2011), ESNumber.valueOf(9), ESNumber.valueOf(22), ESNumber.valueOf(13), ESNumber.valueOf(46), ESNumber.valueOf(9), new ESString("blah") });
        assertTrue(Double.isNaN(object.doubleValue()));
    }
    
    private Date createDate(int year, int monthZeroIndexed, int dayOfMonth, int hourOfDay24, int minute, int second) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(year, monthZeroIndexed, dayOfMonth, hourOfDay24, minute, second);
        return new Date(calendar.getTimeInMillis());
    }
    
    
    @Test
    public void lengthOfToJSON() throws Exception {
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(2011), ESNumber.valueOf(9), ESNumber.valueOf(22), ESNumber.valueOf(13), ESNumber.valueOf(46), ESNumber.valueOf(9), new ESString("blah") });
        ESObject function = (ESObject) object.getProperty(StandardProperty.TOJSONstring);
        assertEquals(ESNumber.valueOf(1), function.getProperty(StandardProperty.LENGTHstring));
    }
    
    @Test
    public void constructorWithStringArgParsesString() throws Exception {
        Date date = createDate(1965, 9, 30, 14, 22, 36);
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(ESValue.EMPTY_ARRAY);
        dp.setDate(date);
        ESValue toStringDate = dp.doIndirectCall(evaluator, dp, "toString", ESValue.EMPTY_ARRAY );
        
        DatePrototype object = (DatePrototype) dateObject.doConstruct(new ESValue[] { toStringDate });
        assertEquals("1965-10-30T14:22:36.000Z",object.doIndirectCall(evaluator, object, "toISOString", ESValue.EMPTY_ARRAY ).toString());
    }

    @Test
    public void constructorWithPartialYearOnly() throws Exception {
        Date date = createDate(2007, 0, 1, 0, 0, 0);
        ESValue value = dateObject.doIndirectCall(evaluator, dateObject, "parse", new ESValue[] { new ESString("2007") });
        assertEquals(ESNumber.valueOf(date.getTime()), value);
    }

    @Test
    public void constructorWithPartialYearMonthOnly() throws Exception {
        Date date = createDate(2007, 5, 1, 0, 0, 0);
        ESValue value = dateObject.doIndirectCall(evaluator, dateObject, "parse", new ESValue[] { new ESString("2007-06") });
        assertEquals(ESNumber.valueOf(date.getTime()), value);
    }

    @Test
    public void nowReturnsApproximatleyCorrectTime() throws Exception  {
        long now = System.currentTimeMillis();
        ESNumber value = (ESNumber) dateObject.doIndirectCall(evaluator, dateObject, "now", ESValue.EMPTY_ARRAY);
        long diff = value.longValue() - now;
        assertTrue(diff < 10000 && diff >= 0); // very generously allow ten seconds
    }

}
