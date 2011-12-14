package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.RangeError;
import FESI.Exceptions.TypeError;


public class DatePrototypeTest extends EvaluatorTestCase {

    private boolean hasFullIndonesian;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        evaluator.setDefaultTimeZone(TimeZone.getTimeZone("Asia/Jakarta"));
        Locale locale = new Locale("fr");
        
        evaluator.setDefaultLocale(locale);
    }
    
    @Test
    public void shouldConvertDateToIsoFormat() throws  Exception {
        Date date = createDate();
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
        Date date = createDate();
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(ESValue.EMPTY_ARRAY);
        dp.setDate(date);
        ESValue value = dp.doIndirectCall(evaluator, dp, "toJSON", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("1965-10-30T14:22:36.123Z"),value);
    }
    
    @Test
    public void toJSONshouldCalltoISOString() throws  Exception {
        Date date = createDate();
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(ESValue.EMPTY_ARRAY);
        dp.setDate(date);
        dp.putProperty("toISOString", TestHelpers.createFunction(evaluator, "return 'overridden';"));
        ESValue value = dp.doIndirectCall(evaluator, dp, "toJSON", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("overridden"),value);
    }
    
    @Test(expected=TypeError.class)
    public void toJSONshouldThrowTypeErrorIftoISOStringNotFunction() throws  Exception {
        Date date = createDate();
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(ESValue.EMPTY_ARRAY);
        dp.setDate(date);
        dp.putProperty("toISOString", new ESString("return 'overridden';"));
        dp.doIndirectCall(evaluator, dp, "toJSON", ESValue.EMPTY_ARRAY);
    }

    private Date createDate() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.set(1965, 9, 30, 14, 22, 36);
        Date date = new Date(calendar.getTimeInMillis()+123L);
        return date;
    }
    
    @Test
    public void toJSONreturnsNullWithInvalidDate() throws  Exception {
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(Double.NaN)});
        ESValue value = dp.doIndirectCall(evaluator, dp, "toJSON", ESValue.EMPTY_ARRAY);
        assertEquals(ESNull.theNull,value);
    }
    
    
    @Test
    public void constructorShouldBeSet() throws Exception {
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(ESValue.EMPTY_ARRAY);
        assertSame(dateObject,dp.getProperty("constructor"));
    }
    
    @Test
    public void toDateString() throws Exception  {
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(Double.NaN)});
        dp.setDate(createDate());
        ESValue value = dp.doIndirectCall(evaluator, dp, "toDateString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("Sat Oct 30 1965"),value);
    }
    
    @Test
    public void toTimeString() throws Exception  {
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(Double.NaN)});
        dp.setDate(createDate());
        ESValue value = dp.doIndirectCall(evaluator, dp, "toTimeString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("21:22:36 UTC+0700"),value);
    }
    
    @Test
    public void toLocaleDateString() throws Exception  {
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(Double.NaN)});
        dp.setDate(createDate());
        ESValue value = dp.doIndirectCall(evaluator, dp, "toLocaleDateString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("30 oct. 1965"),value);
    }
    
    @Test
    public void toLocaleDateStringUK() throws Exception  {
        evaluator.setDefaultLocale(Locale.UK);
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(Double.NaN)});
        dp.setDate(createDate());
        ESValue value = dp.doIndirectCall(evaluator, dp, "toLocaleDateString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("30-Oct-1965"),value);
    }
    
    @Test
    public void toLocaleTimeString() throws Exception  {
        DatePrototype dp = (DatePrototype) dateObject.doConstruct(new ESValue[] { ESNumber.valueOf(Double.NaN)});
        dp.setDate(createDate());
        ESValue value = dp.doIndirectCall(evaluator, dp, "toLocaleTimeString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("21:22:36 WIT"),value);
    }
}
