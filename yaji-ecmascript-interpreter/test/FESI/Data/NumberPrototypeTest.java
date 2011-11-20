package FESI.Data;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.RangeError;

public class NumberPrototypeTest extends EvaluatorTestCase {

    private ESObject [] toTest;
    private boolean performance = false;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        if (performance) {
            Random r = new Random(1);
            toTest = new ESObject[100000];
            for (int i=0; i<toTest.length; i++) {
                toTest[i] = ESNumber.valueOf(Double.longBitsToDouble(r.nextLong())).toESObject(evaluator);
            }
        }
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
        ESObject number = ESNumber.valueOf(1e20).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toFixed", new ESValue[] { ESNumber.valueOf(5) });
        assertEquals(new ESString("100000000000000000000"),value);
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
    
    @Test
    public void toStringForNaN() throws Exception {
        ESObject number = ESNumber.valueOf(Double.NaN).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("NaN"),value);
    }
    
    @Test
    public void toStringForZero() throws Exception {
        ESObject number = ESNumber.valueOf(0).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("0"),value);
    }
    
    @Test
    public void toStringForNegativeZero() throws Exception {
        ESObject number = ESNumber.valueOf(-0).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("0"),value);
    }
    
    @Test
    public void toStringForNegativeInfinity() throws Exception {
        ESObject number = ESNumber.valueOf(Double.NEGATIVE_INFINITY).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("-Infinity"),value);
    }
    
    @Test
    public void toStringForPositiveInfinity() throws Exception {
        ESObject number = ESNumber.valueOf(Double.POSITIVE_INFINITY).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("Infinity"),value);
    }
    
    @Test
    public void toStringFor9_8_1_6() throws Exception {
        ESObject number = ESNumber.valueOf(123456000).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("123456000"),value);
    }
    
    @Test
    public void toStringFor9_8_1_7() throws Exception {
        ESObject number = ESNumber.valueOf(123.456).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("123.456"),value);
    }
    
    @Test
    public void toStringFor9_8_1_8() throws Exception {
        ESObject number = ESNumber.valueOf(0.0123456).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("0.0123456"),value);
    }
    
    @Test
    public void toStringFor9_8_1_8_lowerlimit() throws Exception {
        ESObject number = ESNumber.valueOf(1.23456e-5).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("0.0000123456"),value);
    }
    
    @Test
    public void toStringFor9_8_1_8_lowerlimit_exceeded() throws Exception {
        ESObject number = ESNumber.valueOf(1.23456e-7).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("1.23456e-7"),value);
    }
    
    @Test
    public void toStringFor9_8_1_8_upperlimit() throws Exception {
        ESObject number = ESNumber.valueOf(0.123456).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("0.123456"),value);
    }
    
    @Test
    public void parseFloatGeneratesCorrectValue() throws Exception {
        ESValue value = globalObject.doIndirectCall(evaluator, globalObject, "parseFloat", new ESValue[] { ESNumber.valueOf(3.14) });
        assertEquals(3.14,value.doubleValue(),1e-18);
    }
    
    @Test
    public void toStringPadsWithZeros() throws Exception {
        ESObject number = ESNumber.valueOf(3.668E19).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("36680000000000000000"),value);
    }
    
    @Test
    public void toStringPositiveZero() throws Exception {
        ESObject number = ESNumber.valueOf(0).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("0"),value);
    }
    
    @Test
    public void toString1e21toString() throws Exception {
        ESObject number = ESNumber.valueOf(1e21).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("1e+21"),value);
    }
    
    @Test
    public void toStringNegativeZero() throws Exception {
        ESObject number = ESNumber.valueOf(-0).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("0"),value);
    }
    
    @Test
    public void performanceOfToString() throws Exception {
        if (performance) {
            for (ESObject d : toTest) {
                double parsedValue = Double.parseDouble(d.doIndirectCall(evaluator, d, "toString", ESValue.EMPTY_ARRAY).toString());
                double delta = Math.pow(10, (int)Math.log10(Math.abs(d.doubleValue()))-6);
                assertEquals("Precision:"+delta,parsedValue,d.doubleValue(),delta);
            }
        }
    }
    
    @Test
    public void toExponentialReturnsEFormatForIntegers() throws Exception {
        ESObject number = ESNumber.valueOf(3668).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toExponential", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("3.668e+3"),value);
    }
    
    @Test
    public void toExponentialReturnsClips() throws Exception {
        ESObject number = ESNumber.valueOf(3668).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toExponential", new ESValue[] { ESNumber.valueOf(1) });
        assertEquals(new ESString("3.7e+3"),value);
    }

    @Test(expected=RangeError.class)
    public void toExponentialThrowsRangeErrorIfFractionDigitsNegative() throws Exception {
        ESObject number = ESNumber.valueOf(3668).toESObject(evaluator);
        number.doIndirectCall(evaluator, number, "toExponential", new ESValue[] { ESNumber.valueOf(-1) });
    }

    @Test(expected=RangeError.class)
    public void toExponentialThrowsRangeErrorIfFractionDigitsTooGreat() throws Exception {
        ESObject number = ESNumber.valueOf(3668).toESObject(evaluator);
        number.doIndirectCall(evaluator, number, "toExponential", new ESValue[] { ESNumber.valueOf(21) });
    }

    @Test
    public void toExponentialpads() throws Exception {
        ESObject number = ESNumber.valueOf(3668).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toExponential", new ESValue[] { ESNumber.valueOf(7) });
        assertEquals(new ESString("3.6680000e+3"),value);
    }

    @Test
    public void toExponentialOmitsPeriodForZeroFractionDigits() throws Exception {
        ESObject number = ESNumber.valueOf(3668).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toExponential", new ESValue[] { ESNumber.valueOf(0) });
        assertEquals(new ESString("4e+3"),value);
    }

    @Test
    public void toExponentialForNegative() throws Exception {
        ESObject number = ESNumber.valueOf(-36.68).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toExponential", new ESValue[] { ESNumber.valueOf(4) });
        assertEquals(new ESString("-3.6680e+1"),value);
    }

    @Test
    public void toExponentialForNegativeExponent() throws Exception {
        ESObject number = ESNumber.valueOf(-0.03668).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toExponential", new ESValue[] { ESNumber.valueOf(4) });
        assertEquals(new ESString("-3.6680e-2"),value);
    }


    @Test
    public void toExponentialForPositiveInfinity() throws Exception {
        ESObject number = ESNumber.valueOf(Double.POSITIVE_INFINITY).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toExponential", new ESValue[] { ESNumber.valueOf(4) });
        assertEquals(new ESString("Infinity"),value);
    }

    @Test
    public void toExponentialForNaN() throws Exception {
        ESObject number = ESNumber.NaN.toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toExponential", new ESValue[] { ESNumber.valueOf(4) });
        assertEquals(new ESString("NaN"),value);
    }

    @Test
    public void toExponentialZero() throws Exception {
        ESObject number = ESNumber.valueOf(0).toESObject(evaluator);
        ESValue value = number.doIndirectCall(evaluator, number, "toExponential", new ESValue[] { ESNumber.valueOf(3) });
        assertEquals(new ESString("0.000e+0"),value);
    }
}
