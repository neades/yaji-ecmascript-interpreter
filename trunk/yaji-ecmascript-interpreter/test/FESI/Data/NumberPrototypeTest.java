package FESI.Data;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.RangeError;

public class NumberPrototypeTest extends EvaluatorTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testToPrecisionNaNreturnsNan() throws Exception {
        ESObject number = ESNumber.valueOf(Double.NaN).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toPrecision", new ESValue[] { ESNumber.valueOf(5) });
        assertEquals(new ESString("NaN"), value);
    }

    @Test
    public void testToPrecisionInfinity() throws Exception {
        ESObject number = ESNumber.valueOf(Double.POSITIVE_INFINITY).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toPrecision", new ESValue[] { ESNumber.valueOf(5) });
        assertEquals(new ESString("Infinity"), value);
    }

    @Test
    public void testToPrecisionNegativeInfinity() throws Exception {
        ESObject number = ESNumber.valueOf(Double.NEGATIVE_INFINITY).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toPrecision", new ESValue[] { ESNumber.valueOf(5) });
        assertEquals(new ESString("-Infinity"), value);
    }

    @Test
    public void testToPrecisionInteger() throws Exception {
        ESObject number = ESNumber.valueOf(1).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toPrecision", new ESValue[] { ESNumber.valueOf(5) });
        assertEquals(new ESString("1.0000"), value);
    }

    @Test
    public void testToPrecisionZero() throws Exception {
        ESObject number = ESNumber.valueOf(0).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toPrecision", new ESValue[] { ESNumber.valueOf(4) });
        assertEquals(new ESString("0000"), value);
    }

    @Test
    public void testToPrecisionDouble() throws Exception {
        ESObject number = ESNumber.valueOf(123.456).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toPrecision", new ESValue[] { ESNumber.valueOf(2) });
        assertEquals(new ESString("1.2e+2"), value);
    }

//    @Test
//    public void testToPrecisionOneAsPerSpec() throws Exception {
//        ESObject number = ESNumber.valueOf(123.456).toESObject(evaluator);
//        ESValue value = number.doIndirectCall(evaluator, number, "toPrecision", new ESValue[] { ESNumber.valueOf(1) });
//        assertEquals(new ESString("1.e+2"), value);
//    }
//
    @Test
    public void testToPrecisionOneAsPerWeb() throws Exception {
        ESObject number = ESNumber.valueOf(123.456).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toPrecision", new ESValue[] { ESNumber.valueOf(1) });
        assertEquals(new ESString("1e+2"), value);
    }

    @Test
    public void toPrecisionShouldRoundHalfUp() throws Exception {
        ESObject number = ESNumber.valueOf(123.450).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toPrecision", new ESValue[] { ESNumber.valueOf(4) });
        assertEquals(new ESString("123.5"), value);
    }
    
    @Test
    public void toPrecisonShouldReturnExactValue() throws Exception {
        ESObject number = ESNumber.valueOf(123).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toPrecision", new ESValue[] { ESNumber.valueOf(3) });
        assertEquals(new ESString("123"), value);
    }
    
    @Test
    public void toPrecisonShouldHandleLeadingZeroPadded() throws Exception {
        ESObject number = ESNumber.valueOf(123.3456e-4).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toPrecision", new ESValue[] { ESNumber.valueOf(4) });
        assertEquals(new ESString("0.01233"), value);
    }
    
    @Test
    public void toPrecisonShouldHandleVerySmall() throws Exception {
        ESObject number = ESNumber.valueOf(-123.3456e-21).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toPrecision", new ESValue[] { ESNumber.valueOf(4) });
        assertEquals(new ESString("-1.233e-19"), value);
    }
    
    @Test
    public void toPrecisonWithUndefinedShouldReturnSameAsToString() throws Exception {
        ESObject number = ESNumber.valueOf(-123.3456e-21).toESObject(evaluator);
        ESValue toPrecisionValue = number.doIndirectCall(evaluator, number, "toPrecision", new ESValue[] { ESUndefined.theUndefined });
        ESValue toStringValue = number.doIndirectCall(evaluator, number, "toString", new ESValue[] { } );
        assertEquals(toStringValue,toPrecisionValue);
    }
    
    @Test(expected=RangeError.class)
    public void toPrecisonThrowsRangeErrorForPrecisionForGreaterThan21() throws Exception {
        ESObject number = ESNumber.valueOf(123.450).toESObject(evaluator);
        number.doIndirectCall(evaluator, number, "toPrecision", new ESValue[] { ESNumber.valueOf(22) });
    }
    
    @Test(expected=RangeError.class)
    public void toPrecisonThrowsRangeErrorForPrecisionForLessThan1() throws Exception {
        ESObject number = ESNumber.valueOf(123.450).toESObject(evaluator);
        number.doIndirectCall(evaluator, number, "toPrecision", new ESValue[] { ESNumber.valueOf(0) });
    }

    @Test(expected=RangeError.class)
    public void toFixedThrowsRangeErrorForFractionDigitsForGreaterThan20() throws Exception {
        ESObject number = ESNumber.valueOf(123.456).toESObject(evaluator);
        number.doIndirectCall(evaluator, number, "toFixed", new ESValue[] { ESNumber.valueOf(21) });
    }
    
    @Test(expected=RangeError.class)
    public void toFixedThrowsRangeErrorForFractionDigitsForLessThan0() throws Exception {
        ESObject number = ESNumber.valueOf(123.456).toESObject(evaluator);
        number.doIndirectCall(evaluator, number, "toFixed", new ESValue[] { ESNumber.valueOf(-1) });
    }
    
    @Test
    public void toFixedHandlesSimpleTrimming() throws Exception {
        ESObject number = ESNumber.valueOf(123.453).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toFixed", new ESValue[] { ESNumber.valueOf(2) });
        assertEquals(new ESString("123.45"),value);
    }

    @Test
    public void toFixedRoundUp() throws Exception {
        ESObject number = ESNumber.valueOf(123.456).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toFixed", new ESValue[] { ESNumber.valueOf(2) });
        assertEquals(new ESString("123.46"),value);
    }

    @Test
    public void toFixedAddsTrailingZeros() throws Exception {
        ESObject number = ESNumber.valueOf(123.456).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toFixed", new ESValue[] { ESNumber.valueOf(5) });
        assertEquals(new ESString("123.45600"),value);
    }

    @Test
    public void toFixedWithFraction() throws Exception {
        ESObject number = ESNumber.valueOf(0.01).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toFixed", new ESValue[] { ESNumber.valueOf(5) });
        assertEquals(new ESString("0.01000"),value);
    }

    @Test
    public void toFixedWithFraction1() throws Exception {
        ESObject number = ESNumber.valueOf(0.001).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toFixed", new ESValue[] { ESNumber.valueOf(5) });
        assertEquals(new ESString("0.00100"),value);
    }

    @Test
    public void toFixedForNegative() throws Exception {
        ESObject number = ESNumber.valueOf(-0.001).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toFixed", new ESValue[] { ESNumber.valueOf(5) });
        assertEquals(new ESString("-0.00100"),value);
    }

    @Test
    public void toFixedForTooLarge() throws Exception {
        ESObject number = ESNumber.valueOf(1e21).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toFixed", new ESValue[] { ESNumber.valueOf(5) });
        assertEquals(new ESString("1.0E21"),value);
    }
    
    @Test
    public void toFixedForUpperLimit() throws Exception {
        ESObject number = ESNumber.valueOf(999999999999999L).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toFixed", new ESValue[] { ESNumber.valueOf(20) });
        assertEquals(new ESString("999999999999999.00000000000000000000"),value);
    }
    
    @Test
    public void toFixedForUpperLimit2() throws Exception {
        ESObject number = ESNumber.valueOf(0.00000999999999999999).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toFixed", new ESValue[] { ESNumber.valueOf(20) });
        assertEquals(new ESString("0.00000999999999999999"),value);
    }
    
    @Test
    public void toFixedZeroPlaces() throws Exception {
        ESObject number = ESNumber.valueOf(0.00000999999999999999).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toFixed", new ESValue[] { ESNumber.valueOf(0) });
        assertEquals(new ESString("0"),value);
    }
    
    @Test
    public void toFixedNaN() throws Exception {
        ESObject number = ESNumber.valueOf(Double.NaN).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toFixed", new ESValue[] { ESNumber.valueOf(0) });
        assertEquals(new ESString("NaN"),value);
    }

    @Test
    public void toFixedRoundingHalf() throws Exception {
        ESObject number = ESNumber.valueOf(930.9805).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toFixed", new ESValue[] { ESNumber.valueOf(3) });
        assertEquals(new ESString("930.981"),value);
    }
}
