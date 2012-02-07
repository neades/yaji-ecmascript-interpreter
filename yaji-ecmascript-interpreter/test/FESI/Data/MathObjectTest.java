package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.EcmaScriptException;

public class MathObjectTest extends EvaluatorTestCase {

    private static final ESValue[] NAN_ARGS = new ESValue[] {
            ESNumber.valueOf(2), new ESString("string"), ESNumber.valueOf(1.34) };
    private static final ESValue[] MULTI_ARGS = new ESValue[] {
            ESNumber.valueOf(-3.2), ESNumber.valueOf(3.2), ESNumber.valueOf(0),
            ESNumber.valueOf(-1) };
    private static final ESValue[] NO_ARGS = new ESValue[] {};
    private MathObject mathObject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        mathObject = (MathObject) globalObject.getProperty("Math",
                "Math".hashCode());

    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testMinNoArgs() throws EcmaScriptException,
            NoSuchMethodException {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "min", NO_ARGS);
        assertEquals(Double.POSITIVE_INFINITY, result.doubleValue(), 0);
    }

    @Test
    public void testMinNaN() throws EcmaScriptException, NoSuchMethodException {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "min", NAN_ARGS);
        assertTrue(Double.isNaN(result.doubleValue()));
    }

    @Test
    public void testMinMultiArgs() throws EcmaScriptException,
            NoSuchMethodException {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "min", MULTI_ARGS);
        assertEquals(-3.2, result.doubleValue(), 0);
    }

    @Test
    public void testMaxNoArgs() throws EcmaScriptException,
            NoSuchMethodException {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "max", NO_ARGS);
        assertEquals(Double.NEGATIVE_INFINITY, result.doubleValue(), 0);
    }

    @Test
    public void testMaxNaN() throws EcmaScriptException, NoSuchMethodException {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "max", NAN_ARGS);
        assertTrue(Double.isNaN(result.doubleValue()));
    }

    @Test
    public void testMaxMultiArgs() throws EcmaScriptException,
            NoSuchMethodException {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "max", MULTI_ARGS);
        assertEquals(3.2, result.doubleValue(), 0);
    }

    /**
     * round If x is NaN, the result is NaN. If x is +0, the result is +0. If x
     * is −0, the result is −0. If x is +∞, the result is +∞. If x is −∞, the
     * result is −∞. If x is greater than 0 but less than 0.5, the result is +0.
     * If x is less than 0 but greater than or equal to -0.5, the result is −0.
     * 
     * @throws Exception
     */
    @Test
    public void roundInfinity() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "round",
                new ESValue[] { ESNumber.valueOf(Double.POSITIVE_INFINITY) });
        assertEquals(Double.POSITIVE_INFINITY, result.doubleValue(), 0);
    }

    @Test
    public void roundNegativeInfinity() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "round",
                new ESValue[] { ESNumber.valueOf(Double.NEGATIVE_INFINITY) });
        assertEquals(Double.NEGATIVE_INFINITY, result.doubleValue(), 0);
    }

    @Test
    public void roundNaN() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "round", new ESValue[] { ESNumber.valueOf(Double.NaN) });
        assertNaN(result);
    }

    @Test
    public void roundNegativeZero() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "round", new ESValue[] { ESNumber.valueOf(-0.0) });
        assertEquals(Double.NEGATIVE_INFINITY, 1.0 / result.doubleValue(), 0);
    }

    @Test
    public void roundZero() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "round", new ESValue[] { ESNumber.valueOf(0.0) });
        assertEquals(Double.POSITIVE_INFINITY, 1.0 / result.doubleValue(), 0);
    }

    @Test
    public void roundNegativeHalf() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "round", new ESValue[] { ESNumber.valueOf(-0.4) });
        assertEquals(Double.NEGATIVE_INFINITY, 1.0 / result.doubleValue(), 0);
    }

    /**
     * 15.8.2.1 abs (x)
     * 
     * Returns the absolute value of x; the result has the same magnitude as x
     * but has positive sign.
     */
    @Test
    public void abs() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "abs", new ESValue[] { ESNumber.valueOf(-0.4) });
        assertEquals(0.4, result.doubleValue(), 0);
    }

    // If x is NaN, the result is NaN.
    @Test
    public void absNaN() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "abs", new ESValue[] { ESNumber.valueOf(Double.NaN) });
        assertNaN(result);
    }

    // If x is −0, the result is +0.
    @Test
    public void absNegativeZero() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "abs", new ESValue[] { ESNumber.valueOf(-0.0) });
        assertPositiveZero(result);
    }

    // If x is −∞, the result is +∞.
    @Test
    public void absNegativeInfinity() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "abs",
                new ESValue[] { ESNumber.valueOf(Double.NEGATIVE_INFINITY) });
        assertEquals(Double.POSITIVE_INFINITY, result.doubleValue(), 0);
    }

    /**
     * 15.8.2.2 acos (x) Returns an implementation-dependent approximation to
     * the arc cosine of x. The result is expressed in radians and ranges from
     * +0 to +π.
     */
    // If x is NaN, the result is NaN.
    @Test
    public void acosNaN() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "acos", new ESValue[] { ESNumber.valueOf(Double.NaN) });
        assertNaN(result);
    }

    // If x is greater than 1, the result is NaN.
    @Test
    public void acosGreaterOne() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "acos", new ESValue[] { ESNumber.valueOf(1.00001) });
        assertNaN(result);
    }

    // If x is less than −1, the result is NaN.
    @Test
    public void acosLessThanNegativeOne() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "acos", new ESValue[] { ESNumber.valueOf(-1.00001) });
        assertNaN(result);
    }

    // If x is exactly 1, the result is +0.
    @Test
    public void acosOne() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "acos", new ESValue[] { ESNumber.valueOf(1.0) });
        assertPositiveZero(result);
    }

    /**
     * 15.8.2.3 asin (x) Returns an implementation-dependent approximation to
     * the arc sine of x. The result is expressed in radians and ranges from
     * −π/2 to +π/2.
     */
    // If x is NaN, the result is NaN.
    @Test
    public void asinNaN() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "asin", new ESValue[] { ESNumber.valueOf(Double.NaN) });
        assertNaN(result);
    }

    // If x is greater than 1, the result is NaN.
    @Test
    public void asinGreaterOne() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "asin", new ESValue[] { ESNumber.valueOf(1.00001) });
        assertNaN(result);
    }

    // If x is less than –1, the result is NaN.
    @Test
    public void asinLessThanNegativeOne() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "asin", new ESValue[] { ESNumber.valueOf(-1.00001) });
        assertNaN(result);
    }

    // If x is +0, the result is +0.
    @Test
    public void asinPositiveZero() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "asin", new ESValue[] { ESNumber.valueOf(0.0) });
        assertPositiveZero(result);
    }

    // If x is −0, the result is −0.
    @Test
    public void asinNegativeZero() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "asin", new ESValue[] { ESNumber.valueOf(-0.0) });
        assertNegativeZero(result);
    }

    /**
     * 15.8.2.4 atan (x) Returns an implementation-dependent approximation to
     * the arc tangent of x. The result is expressed in radians and ranges from
     * −π/2 to +π/2.
     */

    // If x is NaN, the result is NaN.
    @Test
    public void atanNaN() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan", new ESValue[] { ESNumber.valueOf(Double.NaN) });
        assertNaN(result);
    }

    // If x is +0, the result is +0.
    @Test
    public void atanPositiveZero() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan", new ESValue[] { ESNumber.valueOf(0.0) });
        assertPositiveZero(result);
    }

    // If x is −0, the result is −0.
    @Test
    public void atanNegativeZero() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan", new ESValue[] { ESNumber.valueOf(-0.0) });
        assertNegativeZero(result);
    }

    // If x is +∞, the result is an implementation-dependent approximation to
    // +π/2.
    @Test
    public void atanPositiveInfinity() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan",
                new ESValue[] { ESNumber.valueOf(Double.POSITIVE_INFINITY) });
        assertEquals(Math.PI / 2, result.doubleValue(), 0);
    }

    // If x is −∞, the result is an implementation-dependent approximation to
    // −π/2.
    @Test
    public void atanNegativeInfinity() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan",
                new ESValue[] { ESNumber.valueOf(Double.NEGATIVE_INFINITY) });
        assertEquals(-Math.PI / 2, result.doubleValue(), 0);
    }

    /**
     * 15.8.2.5 atan2 (y, x)
     * 
     * Returns an implementation-dependent approximation to the arc tangent of
     * the quotient y/x of the arguments y and x, where the signs of y and x are
     * used to determine the quadrant of the result. Note that it is intentional
     * and traditional for the two-argument arc tangent function that the
     * argument named y be first and the argument named x be second. The result
     * is expressed in radians and ranges from −π to +π.
     */
    // If either x or y is NaN, the result is NaN.
    @Test
    public void atan2xNan() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan2",
                new ESValue[] { ESNumber.valueOf(Double.NEGATIVE_INFINITY),
                        ESNumber.valueOf(Double.NaN) });
        assertNaN(result);
    }

    @Test
    public void atan2yNan() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan2", new ESValue[] { ESNumber.valueOf(Double.NaN),
                        ESNumber.ZERO });
        assertNaN(result);
    }

    // If y>0 and x is +0, the result is an implementation-dependent
    // approximation to +π/2.
    @Test
    public void atan2yGt0xis0() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan2", new ESValue[] { ESNumber.valueOf(1000000.0),
                        ESNumber.ZERO });
        assertEquals(Math.PI / 2, result.doubleValue(), 0);
    }

    // If y>0 and x is −0, the result is an implementation-dependent
    // approximation to +π/2.
    @Test
    public void atan2yGt0xisneg0() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan2", new ESValue[] { ESNumber.valueOf(1000000.0),
                        ESNumber.NEGATIVE_ZERO });
        assertEquals(Math.PI / 2, result.doubleValue(), 0);
    }

    // If y is +0 and x>0, the result is +0.
    @Test
    public void atan2yis0xgt0() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan2",
                new ESValue[] { ESNumber.ZERO, ESNumber.valueOf(100000.0) });
        assertPositiveZero(result);
    }

    // If y is +0 and x is +0, the result is +0.
    @Test
    public void atan2yis0xis0() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan2", new ESValue[] { ESNumber.ZERO, ESNumber.ZERO });
        assertPositiveZero(result);
    }

    // If y is +0 and x is −0, the result is an implementation-dependent
    // approximation to +π.
    @Test
    public void atan2yis0xisneg0() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan2",
                new ESValue[] { ESNumber.ZERO, ESNumber.NEGATIVE_ZERO });
        assertEquals(Math.PI, result.doubleValue(), 0);
    }

    // If y is +0 and x<0, the result is an implementation-dependent
    // approximation to +π.
    @Test
    public void atan2yis0xislt0() throws Exception {
        ESValue result = mathObject
                .doIndirectCall(evaluator, mathObject, "atan2", new ESValue[] {
                        ESNumber.ZERO, ESNumber.valueOf(-0.0000000001) });
        assertEquals(Math.PI, result.doubleValue(), 0);
    }

    // If y is −0 and x>0, the result is −0.
    @Test
    public void atan2yisneg0xisgt0() throws Exception {
        ESValue result = mathObject.doIndirectCall(
                evaluator,
                mathObject,
                "atan2",
                new ESValue[] { ESNumber.NEGATIVE_ZERO,
                        ESNumber.valueOf(0.0000000001) });
        assertNegativeZero(result);
    }

    // If y is −0 and x is +0, the result is −0.
    @Test
    public void atan2yisneg0xis0() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan2",
                new ESValue[] { ESNumber.NEGATIVE_ZERO, ESNumber.ZERO });
        assertNegativeZero(result);
    }

    // If y is −0 and x is −0, the result is an implementation-dependent
    // approximation to −π.
    @Test
    public void atan2yisneg0xisneg0() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan2", new ESValue[] { ESNumber.NEGATIVE_ZERO,
                        ESNumber.NEGATIVE_ZERO });
        assertEquals(-Math.PI, result.doubleValue(), 0);
    }

    // If y is −0 and x<0, the result is an implementation-dependent
    // approximation to −π.
    @Test
    public void atan2yisneg0xislt0() throws Exception {
        ESValue result = mathObject.doIndirectCall(
                evaluator,
                mathObject,
                "atan2",
                new ESValue[] { ESNumber.NEGATIVE_ZERO,
                        ESNumber.valueOf(-0.000001) });
        assertEquals(-Math.PI, result.doubleValue(), 0);
    }

    // If y<0 and x is +0, the result is an implementation-dependent
    // approximation to −π/2.
    @Test
    public void atan2yislt0xis0() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan2", new ESValue[] { ESNumber.valueOf(-0.0000001),
                        ESNumber.ZERO });
        assertEquals(-Math.PI / 2, result.doubleValue(), 0);
    }

    // If y<0 and x is −0, the result is an implementation-dependent
    // approximation to −π/2.
    @Test
    public void atan2yislt0xisneg0() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "atan2", new ESValue[] { ESNumber.valueOf(-0.0000001),
                        ESNumber.NEGATIVE_ZERO });
        assertEquals(-Math.PI / 2, result.doubleValue(), 0);
    }

    // If y > 0 and y is finite and x is +∞, the result is +0.
    @Test
    public void atan2yisgt0xInf() throws Exception {
        ESValue result = mathObject.doIndirectCall(
                evaluator,
                mathObject,
                "atan2",
                new ESValue[] { ESNumber.valueOf(0.0000001),
                        ESNumber.valueOf(Double.POSITIVE_INFINITY) });
        assertPositiveZero(result);
    }

    // If y > 0 and y is finite and x is −∞, the result if an
    // implementation-dependent approximation to +π.
    @Test
    public void atan2yisgt0xnegInf() throws Exception {
        ESValue result = mathObject.doIndirectCall(
                evaluator,
                mathObject,
                "atan2",
                new ESValue[] { ESNumber.valueOf(0.0000001),
                        ESNumber.valueOf(Double.NEGATIVE_INFINITY) });
        assertEquals(Math.PI, result.doubleValue(), 0);
    }

    // If y < 0 and y is finite and x is +∞, the result is −0.
    @Test
    public void atan2ylt0xInf() throws Exception {
        ESValue result = mathObject.doIndirectCall(
                evaluator,
                mathObject,
                "atan2",
                new ESValue[] { ESNumber.valueOf(-0.0000001),
                        ESNumber.valueOf(Double.POSITIVE_INFINITY) });
        assertNegativeZero(result);
    }

    // If y <0 and y is finite and x is −∞, the result is an
    // implementation-dependent approximation to −π.
    @Test
    public void atan2ylt0xnegInf() throws Exception {
        ESValue result = mathObject.doIndirectCall(
                evaluator,
                mathObject,
                "atan2",
                new ESValue[] { ESNumber.valueOf(-0.0000001),
                        ESNumber.valueOf(Double.NEGATIVE_INFINITY) });
        assertEquals(-Math.PI, result.doubleValue(), 0);
    }

    // If y is +∞ and x is finite, the result is an implementation-dependent
    // approximation to +π/2.
    // If y is −∞ and x is finite, the result is an implementation-dependent
    // approximation to −π/2.
    // If y is +∞ and x is +∞, the result is an implementation-dependent
    // approximation to +π/4.
    // If y is +∞ and x is −∞, the result is an implementation-dependent
    // approximation to +3π/4.
    // If y is −∞ and x is +∞, the result is an implementation-dependent
    // approximation to −π/4.
    // If y is −∞ and x is −∞, the result is an implementation-dependent
    // approximation to −3π/4.

    /**
     * 15.8.2.6 ceil (x)
     * 
     * Returns the smallest (closest to −∞) Number value that is not less than x
     * and is equal to a mathematical integer. If x is already an integer, the
     * result is x.
     */
    // If x is NaN, the result is NaN.
    @Test
    public void ceilNaN() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "ceil", new ESValue[] { ESNumber.valueOf(Double.NaN) });
        assertNaN(result);
    }

    // If x is +0, the result is +0.
    @Test
    public void ceilPositiveZero() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "ceil", new ESValue[] { ESNumber.ZERO });
        assertPositiveZero(result);
    }

    // If x is −0, the result is −0.
    @Test
    public void ceilNegativeZero() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "ceil", new ESValue[] { ESNumber.NEGATIVE_ZERO });
        assertNegativeZero(result);
    }

    // If x is +∞, the result is +∞.
    @Test
    public void ceilInfinity() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "ceil",
                new ESValue[] { ESNumber.valueOf(Double.POSITIVE_INFINITY) });
        assertEquals(Double.POSITIVE_INFINITY, result.doubleValue(), 0);
    }

    // If x is −∞, the result is −∞.
    @Test
    public void ceilNegInfinity() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "ceil",
                new ESValue[] { ESNumber.valueOf(Double.NEGATIVE_INFINITY) });
        assertEquals(Double.NEGATIVE_INFINITY, result.doubleValue(), 0);
    }

    // If x is less than 0 but greater than -1, the result is −0.
    @Test
    public void ceilSmallNegative() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "ceil", new ESValue[] { ESNumber.valueOf(-0.99999999) });
        assertNegativeZero(result);
    }

    /**
     * 15.8.2.7 cos (x)
     * 
     * Returns an implementation-dependent approximation to the cosine of x. The
     * argument is expressed in radians.
     */
    // If x is NaN, the result is NaN.
    // If x is +0, the result is 1.
    // If x is −0, the result is 1.
    // If x is +∞, the result is NaN.
    // If x is −∞, the result is NaN.
    /**
     * 15.8.2.8 exp (x)
     * 
     * Returns an implementation-dependent approximation to the exponential
     * function of x (e raised to the power of x, where e is the base of the
     * natural logarithms).
     */
    // If x is NaN, the result is NaN.
    // If x is +0, the result is 1.
    // If x is −0, the result is 1.
    // If x is +∞, the result is +∞.
    // If x is −∞, the result is +0.
    /**
     * 15.8.2.9 floor (x)
     * 
     * Returns the greatest (closest to +∞) Number value that is not greater
     * than x and is equal to a mathematical integer. If x is already an
     * integer, the result is x.
     */
    // If x is NaN, the result is NaN.
    @Test
    public void floorNaN() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "floor", new ESValue[] { ESNumber.valueOf(Double.NaN) });
        assertNaN(result);
    }

    // If x is +0, the result is +0.
    @Test
    public void floorPositiveZero() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "floor", new ESValue[] { ESNumber.ZERO });
        assertPositiveZero(result);
    }

    // If x is −0, the result is −0.
    @Test
    public void floorNegativeZero() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "floor", new ESValue[] { ESNumber.NEGATIVE_ZERO });
        assertNegativeZero(result);
    }

    // If x is +∞, the result is +∞.
    @Test
    public void floorInfinity() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "floor",
                new ESValue[] { ESNumber.valueOf(Double.POSITIVE_INFINITY) });
        assertEquals(Double.POSITIVE_INFINITY, result.doubleValue(), 0);
    }

    // If x is −∞, the result is −∞.
    @Test
    public void floorNegInfinity() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "floor",
                new ESValue[] { ESNumber.valueOf(Double.NEGATIVE_INFINITY) });
        assertEquals(Double.NEGATIVE_INFINITY, result.doubleValue(), 0);
    }

    // If x is greater than 0 but less than 1, the result is +0.
    @Test
    public void floorSmallPositive() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "floor", new ESValue[] { ESNumber.valueOf(0.99999999) });
        assertPositiveZero(result);
    }

    /**
     * 15.8.2.10 log (x)
     * 
     * Returns an implementation-dependent approximation to the natural
     * logarithm of x.
     */
    // If x is NaN, the result is NaN.
    // If x is less than 0, the result is NaN.
    // If x is +0 or −0, the result is −∞.
    // If x is 1, the result is +0.
    // If x is +∞, the result is +∞.
    /**
     * 15.8.2.11 max ( [ value1 [ , value2 [ , … ] ] ] )
     * 
     * Given zero or more arguments, calls ToNumber on each of the arguments and
     * returns the largest of the resulting values.
     */
    // If no arguments are given, the result is −∞.
    // If any value is NaN, the result is NaN.
    // The comparison of values to determine the largest value is done as in
    // 11.8.5 except that +0 is considered to be larger than −0.
    // The length property of the max method is 2.
    /**
     * 15.8.2.12 min ( [ value1 [ , value2 [ , … ] ] ] )
     * 
     * Given zero or more arguments, calls ToNumber on each of the arguments and
     * returns the smallest of the resulting values.
     */
    // If no arguments are given, the result is +∞.
    // If any value is NaN, the result is NaN.
    // The comparison of values to determine the smallest value is done as in
    // 11.8.5 except that +0 is considered to be larger than −0.
    // The length property of the min method is 2.
    /**
     * 15.8.2.13 pow (x, y)
     * 
     * Returns an implementation-dependent approximation to the result of
     * raising x to the power y.
     */
    // If y is NaN, the result is NaN.
    @Test
    public void powNaN() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "pow", new ESValue[] { ESNumber.ZERO, ESNumber.valueOf(Double.NaN) });
        assertNaN(result);
    }

    // If y is +0, the result is 1, even if x is NaN.
    @Test
    public void powy0xNaN() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "pow", new ESValue[] { ESNumber.valueOf(Double.NaN), ESNumber.ZERO });
        assertEquals(1.0, result.doubleValue(), 0);
    }

    // If y is −0, the result is 1, even if x is NaN.
    @Test
    public void powyneg0xNaN() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "pow", new ESValue[] { ESNumber.valueOf(Double.NaN), ESNumber.NEGATIVE_ZERO });
        assertEquals(1.0, result.doubleValue(), 0);
    }

    // If x is NaN and y is nonzero, the result is NaN.
    @Test
    public void powynot0xNaN() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "pow", new ESValue[] { ESNumber.valueOf(Double.NaN), ESNumber.valueOf(0.00001) });
        assertNaN(result);
    }

    // If abs(x)>1 and y is +∞, the result is +∞.
    @Test
    public void powabsxgt1yInf() throws Exception {
        ESValue result = mathObject.doIndirectCall(evaluator, mathObject,
                "pow", new ESValue[] { ESNumber.valueOf(-1.1), ESNumber.valueOf(Double.POSITIVE_INFINITY) });
        assertEquals(Double.POSITIVE_INFINITY,result.doubleValue(),0);
    }

    // If abs(x)>1 and y is −∞, the result is +0.
    // If abs(x)==1 and y is +∞, the result is NaN.
    // If abs(x)==1 and y is −∞, the result is NaN.
    // If abs(x)<1 and y is +∞, the result is +0.
    // If abs(x)<1 and y is −∞, the result is +∞.
    // If x is +∞ and y>0, the result is +∞.
    // If x is +∞ and y<0, the result is +0.
    // If x is −∞ and y>0 and y is an odd integer, the result is −∞.
    // If x is −∞ and y>0 and y is not an odd integer, the result is +∞.
    // If x is −∞ and y<0 and y is an odd integer, the result is −0.
    // If x is −∞ and y<0 and y is not an odd integer, the result is +0.
    // If x is +0 and y>0, the result is +0.
    // If x is +0 and y<0, the result is +∞.
    // If x is −0 and y>0 and y is an odd integer, the result is −0.
    // If x is −0 and y>0 and y is not an odd integer, the result is +0.
    // If x is −0 and y<0 and y is an odd integer, the result is −∞.
    // If x is −0 and y<0 and y is not an odd integer, the result is +∞.
    // If x<0 and x is finite and y is finite and y is not an integer, the
    // result is NaN.

    /**
     * 15.8.2.16 sin (x)
     * 
     * Returns an implementation-dependent approximation to the sine of x. The
     * argument is expressed in radians.
     */
    // If x is NaN, the result is NaN.
    // If x is +0, the result is +0.
    // If x is −0, the result is −0.
    // If x is +∞ or −∞, the result is NaN.
    /**
     * 15.8.2.17 sqrt (x)
     * 
     * Returns an implementation-dependent approximation to the square root of
     * x.
     */
    // If x is NaN, the result is NaN.
    // If x is less than 0, the result is NaN.
    // If x is +0, the result is +0.
    // If x is −0, the result is −0.
    // If x is +∞, the result is +∞.

    /**
     * 15.8.2.18 tan (x)
     * 
     * Returns an implementation-dependent approximation to the tangent of x.
     * The argument is expressed in radians.
     */
    // If x is NaN, the result is NaN.
    // If x is +0, the result is +0.
    // If x is −0, the result is −0.
    // If x is +∞ or −∞, the result is NaN.

    private static void assertPositiveZero(ESValue result)
            throws EcmaScriptException {
        assertEquals(0.0, result.doubleValue(), 0);
        assertEquals(Double.POSITIVE_INFINITY, 1.0 / result.doubleValue(), 0);
    }

    private static void assertNegativeZero(ESValue result)
            throws EcmaScriptException {
        assertEquals(0.0, result.doubleValue(), 0);
        assertEquals(Double.NEGATIVE_INFINITY, 1.0 / result.doubleValue(), 0);
    }

    private static void assertNaN(ESValue result) throws EcmaScriptException {
        assertTrue(result.doubleValue() != result.doubleValue());
    }

}
