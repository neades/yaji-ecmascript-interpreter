package FESI.Data;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.EcmaScriptException;

public class RegExpPrototypeTest extends EvaluatorTestCase {


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void shouldSetGlobalProperty() throws EcmaScriptException {
        RegExpPrototype regexp = new RegExpPrototype(evaluator.getRegExpPrototype(), evaluator, "[a-z]","g");
        assertEquals(ESBoolean.valueOf(true), regexp.getProperty(StandardProperty.GLOBALstring,StandardProperty.GLOBALhash));
    }

    @Test
    public void allFlagsClearIfNoFlagsSupplied() throws EcmaScriptException {
        RegExpPrototype regexp = new RegExpPrototype(evaluator.getRegExpPrototype(), evaluator, "[a-z]","");
        assertEquals(ESBoolean.valueOf(false), regexp.getProperty(StandardProperty.GLOBALstring,StandardProperty.GLOBALhash));
        assertEquals(ESBoolean.valueOf(false), regexp.getProperty(StandardProperty.IGNORE_CASEstring,StandardProperty.IGNORE_CASEhash));
        assertEquals(ESBoolean.valueOf(false), regexp.getProperty(StandardProperty.MULTILINEstring,StandardProperty.MULTILINEhash));
        assertEquals(ESNumber.valueOf(0), regexp.getProperty(StandardProperty.LAST_INDEXstring, StandardProperty.LAST_INDEXhash));
    }

    @Test
    public void testShouldMatch() throws Exception {
        RegExpPrototype regexp = new RegExpPrototype(evaluator.getRegExpPrototype(), evaluator, "[a-z]","");
        assertEquals(ESBoolean.valueOf(true), regexp.doIndirectCall(evaluator, regexp, "test", new ESValue[] { new ESString("a") }));
    }
    
    @Test
    public void testShouldNotMatch() throws Exception {
        RegExpPrototype regexp = new RegExpPrototype(evaluator.getRegExpPrototype(), evaluator, "[a-z]","");
        assertEquals(ESBoolean.valueOf(false), regexp.doIndirectCall(evaluator, regexp, "test", new ESValue[] { new ESString("A") }));
    }
    
    @Test
    public void testShouldMatchCaseInsensitive() throws Exception {
        RegExpPrototype regexp = new RegExpPrototype(evaluator.getRegExpPrototype(), evaluator, "[a-z]","i");
        assertEquals(ESBoolean.valueOf(true), regexp.doIndirectCall(evaluator, regexp, "test", new ESValue[] { new ESString("A") }));
    }
    
    @Test
    public void testSuccessiveCallsShouldStep() throws Exception {
        ESString esString = new ESString("abc");
        RegExpPrototype regexp = new RegExpPrototype(evaluator.getRegExpPrototype(), evaluator, "[a-z]","g");
        ESObject array = (ESObject) regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(new ESString("a"), array.getProperty(0L));
        array = (ESObject) regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(new ESString("b"), array.getProperty(0L));
        array = (ESObject) regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(new ESString("c"), array.getProperty(0L));
        ESValue lastResult = regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(ESNull.theNull, lastResult);
        array = (ESObject) regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(new ESString("a"), array.getProperty(0L));
    }
    
    @Test
    public void testNonGlobalExpressionDontStep() throws Exception {
        ESString esString = new ESString("abc");
        RegExpPrototype regexp = new RegExpPrototype(evaluator.getRegExpPrototype(), evaluator, "[a-z]","");
        ESObject array = (ESObject) regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(new ESString("a"), array.getProperty(0L));
        array = (ESObject) regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(new ESString("a"), array.getProperty(0L));
    }
    
    @Test
    public void testNoProblemWithShortenedString() throws Exception {
        ESString esString = new ESString("abcABCabc");
        RegExpPrototype regexp = new RegExpPrototype(evaluator.getRegExpPrototype(), evaluator, "[a-z]+","g");
        ESObject array = (ESObject) regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(new ESString("abc"), array.getProperty(0L));
        ESValue v = regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { new ESString("a") });
        assertEquals(ESNull.theNull, v);
    }    
    
    @Test
    public void testShouldMatchGlobal() throws Exception {
        RegExpPrototype regexp = new RegExpPrototype(evaluator.getRegExpPrototype(), evaluator, "([a-z])B([a-z])B","g");
        ESValue result = regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { new ESString("aBcB") });
        ESObject array = (ESObject) result;
        assertEquals(new ESString("aBcB"),array.getProperty(0L));
        assertEquals(new ESString("a"),array.getProperty(1L));
        assertEquals(new ESString("c"),array.getProperty(2L));
        assertEquals(ESNumber.valueOf(0),array.getProperty(StandardProperty.INDEXstring));
        assertEquals(ESNumber.valueOf(4),regexp.getProperty(StandardProperty.LAST_INDEXstring));
        assertEquals(ESNumber.valueOf(3),array.getProperty(StandardProperty.LENGTHstring));
        assertEquals(new ESString("aBcB"),array.getProperty(StandardProperty.INPUTstring));
    }
    
    @Test
    public void shouldSetSourceProperty() throws Exception {
        RegExpPrototype regexp = new RegExpPrototype(evaluator.getRegExpPrototype(), evaluator, "([a-z])B([a-z])B","g");
        assertEquals(new ESString("([a-z])B([a-z])B"),regexp.getProperty(StandardProperty.SOURCEstring));
    }
    
    @Test
    public void toStringShouldReturnPatternSurrondedInSlashes() throws Exception {
        RegExpPrototype regexp = new RegExpPrototype(evaluator.getRegExpPrototype(), evaluator, "([a-z])B([a-z])B","g");
        assertEquals(new ESString("/([a-z])B([a-z])B/g"),regexp.doIndirectCall(evaluator, regexp, "toString", ESValue.EMPTY_ARRAY));
    }
}
