package FESI.Data;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

public class RegExpPrototypeTest {

    private Evaluator evaluator;

    @Before
    public void setUp() throws Exception {
        evaluator = new Evaluator();
    }

    @After
    public void tearDown() throws Exception {
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
        ArrayPrototype array = (ArrayPrototype) regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(new ESString("a"), array.getProperty(0));
        array = (ArrayPrototype) regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(new ESString("b"), array.getProperty(0));
        array = (ArrayPrototype) regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(new ESString("c"), array.getProperty(0));
        ESValue lastResult = regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(ESNull.theNull, lastResult);
        array = (ArrayPrototype) regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(new ESString("a"), array.getProperty(0));
    }
    
    @Test
    public void testNonGlobalExpressionDontStep() throws Exception {
        ESString esString = new ESString("abc");
        RegExpPrototype regexp = new RegExpPrototype(evaluator.getRegExpPrototype(), evaluator, "[a-z]","");
        ArrayPrototype array = (ArrayPrototype) regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(new ESString("a"), array.getProperty(0));
        array = (ArrayPrototype) regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(new ESString("a"), array.getProperty(0));
    }
    
    @Test
    public void testNoProblemWithShortenedString() throws Exception {
        ESString esString = new ESString("abcABCabc");
        RegExpPrototype regexp = new RegExpPrototype(evaluator.getRegExpPrototype(), evaluator, "[a-z]+","g");
        ArrayPrototype array = (ArrayPrototype) regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { esString });
        assertEquals(new ESString("abc"), array.getProperty(0));
        ESValue v = regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { new ESString("a") });
        assertEquals(ESNull.theNull, v);
    }    
    
    @Test
    public void testShouldMatchGlobal() throws Exception {
        RegExpPrototype regexp = new RegExpPrototype(evaluator.getRegExpPrototype(), evaluator, "([a-z])B([a-z])B","g");
        ESValue result = regexp.doIndirectCall(evaluator, regexp, "exec", new ESValue[] { new ESString("aBcB") });
        ArrayPrototype array = (ArrayPrototype) result;
        assertEquals(new ESString("aBcB"),array.getProperty(0));
        assertEquals(new ESString("a"),array.getProperty(1));
        assertEquals(new ESString("c"),array.getProperty(2));
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
}