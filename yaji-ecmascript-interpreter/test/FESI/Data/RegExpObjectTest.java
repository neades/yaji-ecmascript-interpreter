package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.SyntaxError;
import FESI.Exceptions.TypeError;

public class RegExpObjectTest extends EvaluatorTestCase {

    private RegExpObject regExpObject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        regExpObject = (RegExpObject)globalObject.getProperty("RegExp");
    }

    @Test
    public void testConstructWith2Strings() throws Exception {
        RegExpPrototype regexp = (RegExpPrototype) regExpObject.doConstruct(new ESValue[] { new ESString("[a-z]"), new ESString("g") });
        assertEquals("/[a-z]/g",regexp.toString());
    }

    @Test
    public void testConstructWith1Strings() throws Exception {
        RegExpPrototype regexp = (RegExpPrototype) regExpObject.doConstruct(new ESValue[] { new ESString("[a-z]") });
        assertEquals("/[a-z]/",regexp.toString());
    }

    @Test
    public void testConstructWithRegExp() throws Exception {
        RegExpPrototype regexp1 = (RegExpPrototype) regExpObject.doConstruct(new ESValue[] { new ESString("[a-z]"), new ESString("gim") });
        RegExpPrototype regexp = (RegExpPrototype) regExpObject.doConstruct(new ESValue[] { regexp1 });
        assertEquals("/[a-z]/gim",regexp.toString());
    }

    @Test
    public void testConstructWithRegExpAlternateFlagErro() throws Exception {
        RegExpPrototype regexp = (RegExpPrototype) regExpObject.doConstruct(new ESValue[] { new ESString("[a-z]"), new ESString("mig") });
        assertEquals("/[a-z]/gim",regexp.toString());
    }

    @Test(expected=TypeError.class)
    public void testConstructWithRegExpWithTwoParameters() throws Exception {
        RegExpPrototype regexp1 = (RegExpPrototype) regExpObject.doConstruct(new ESValue[] { new ESString("[a-z]"), new ESString("gim") });
        regExpObject.doConstruct(new ESValue[] { regexp1, new ESString("gim") });
    }

    @Test(expected=SyntaxError.class)
    public void testInvalidFlags() throws Exception {
        regExpObject.doConstruct(new ESValue[] { new ESString("[a-z]"), new ESString("s") });
    }

    @Test(expected=SyntaxError.class)
    public void testInvalidFlagsRepeated() throws Exception {
        regExpObject.doConstruct(new ESValue[] { new ESString("[a-z]"), new ESString("gig") });
    }
    
    @Test
    public void shouldReturnRegexpIfCalledAsFunction() throws Exception {
        RegExpPrototype regexp = (RegExpPrototype) regExpObject.doConstruct(new ESValue[] { new ESString("[a-z]") });
        ESValue result = regExpObject.callFunction(regExpObject, new ESValue[] { regexp });
        assertSame(result,regexp);
    }
}
