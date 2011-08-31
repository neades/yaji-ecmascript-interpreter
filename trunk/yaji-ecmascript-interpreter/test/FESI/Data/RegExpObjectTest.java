package FESI.Data;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.SyntaxError;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;

public class RegExpObjectTest {

    private Evaluator evaluator;
    private RegExpObject regExpObject;

    @Before
    public void setUp() throws Exception {
        evaluator = new Evaluator();
        regExpObject = (RegExpObject)evaluator.getGlobalObject().getProperty("RegExp");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testConstructWith2Strings() throws Exception {
        RegExpPrototype regexp = (RegExpPrototype) regExpObject.doConstruct(regExpObject, new ESValue[] { new ESString("[a-z]"), new ESString("g") });
        assertEquals("/[a-z]/g",regexp.toString());
    }

    @Test
    public void testConstructWith1Strings() throws Exception {
        RegExpPrototype regexp = (RegExpPrototype) regExpObject.doConstruct(regExpObject, new ESValue[] { new ESString("[a-z]") });
        assertEquals("/[a-z]/",regexp.toString());
    }

    @Test
    public void testConstructWithRegExp() throws Exception {
        RegExpPrototype regexp1 = (RegExpPrototype) regExpObject.doConstruct(regExpObject, new ESValue[] { new ESString("[a-z]"), new ESString("gim") });
        RegExpPrototype regexp = (RegExpPrototype) regExpObject.doConstruct(regExpObject, new ESValue[] { regexp1 });
        assertEquals("/[a-z]/gim",regexp.toString());
    }

    @Test
    public void testConstructWithRegExpAlternateFlagErro() throws Exception {
        RegExpPrototype regexp = (RegExpPrototype) regExpObject.doConstruct(regExpObject, new ESValue[] { new ESString("[a-z]"), new ESString("mig") });
        assertEquals("/[a-z]/gim",regexp.toString());
    }

    @Test(expected=TypeError.class)
    public void testConstructWithRegExpWithTwoParameters() throws Exception {
        RegExpPrototype regexp1 = (RegExpPrototype) regExpObject.doConstruct(regExpObject, new ESValue[] { new ESString("[a-z]"), new ESString("gim") });
        regExpObject.doConstruct(regExpObject, new ESValue[] { regexp1, new ESString("gim") });
    }

    @Test(expected=SyntaxError.class)
    public void testInvalidFlags() throws Exception {
        regExpObject.doConstruct(regExpObject, new ESValue[] { new ESString("[a-z]"), new ESString("s") });
    }

    @Test(expected=SyntaxError.class)
    public void testInvalidFlagsRepeated() throws Exception {
        regExpObject.doConstruct(regExpObject, new ESValue[] { new ESString("[a-z]"), new ESString("gig") });
    }
    
    @Test
    public void shouldReturnRegexpIfCalledAsFunction() throws Exception {
        RegExpPrototype regexp = (RegExpPrototype) regExpObject.doConstruct(regExpObject, new ESValue[] { new ESString("[a-z]") });
        ESValue result = regExpObject.callFunction(regExpObject, new ESValue[] { regexp });
        assertSame(result,regexp);
    }
}
