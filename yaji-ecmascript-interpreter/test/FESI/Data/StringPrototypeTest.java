package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

public class StringPrototypeTest {
    private Evaluator evaluator;

    private static final String NON_ASCII_WHITE_SPACE = 
         "\u00A0" //   no-break space
        +"\u1680" //   ogham space mark
        +"\u180E" //   mongolian vowel separator
        +"\u2000" //   en quad Common
        +"\u2001" //   em quad Common
        +"\u2002" //   en space
        +"\u2003" //   em space
        +"\u2004" //   three-per-em space
        +"\u2005" //   four-per-em space
        +"\u2006" //   six-per-em space
        +"\u2007" //   figure space
        +"\u2008" //   punctuation space
        +"\u2009" //   thin space
        +"\u200A" //   hair space 
        +"\u2028" //   line separator
        +"\u2029" //   paragraph separator
        +"\u202F" //   narrow no-break space
        +"\u205F" //   medium mathematical space 
        +"\u3000" //   ideographic space
        +"\u2028" //   Line separator
        +"\u2029";//   Paragraph separator

    private static final String ASCII_WHITE_SPACE = "\u0009\n\u000B\u000C\r\u0020";
    
    @Before
    public void setUp() {
        evaluator = new Evaluator();
    }
    @Test
    public void shouldDefinePrototype() throws Exception {
        ESObject stringObject = (ESObject) evaluator.getGlobalObject().getProperty("String","String".hashCode());
        ESValue stringPrototype = stringObject.getProperty("prototype","prototype".hashCode());
        assertTrue(stringPrototype instanceof ESObject);
    }
    
    @Test
    public void shouldDefineConstructorOnPrototype() throws Exception {
        ESObject stringObject = (ESObject) evaluator.getGlobalObject().getProperty("String","String".hashCode());
        ESObject stringPrototype = (ESObject) stringObject.getProperty("prototype","prototype".hashCode());
        assertSame(stringObject,stringPrototype.getProperty("constructor","constructor".hashCode()));
    }
    
    @Test
    public void shouldConcat2Strings() throws Exception {
        ESObject originalObject = new ESString("start").toESObject(evaluator);
        ESValue result = originalObject.doIndirectCall(evaluator, originalObject, "concat", new ESValue[] { new ESString("end") });
        assertEquals(new ESString("startend"),result);
    }
    
    @Test
    public void charAtWorksWithString() throws Exception {
        ESObject originalObject = new ESString("start").toESObject(evaluator);
        ESValue result = originalObject.doIndirectCall(evaluator, originalObject, "charAt", new ESValue[] { ESNumber.valueOf(1) });
        assertEquals(new ESString("t"),result);
    }
    
    @Test
    public void charAtWorksPassedString() throws Exception {
        ESObject originalObject = new ESString("start").toESObject(evaluator);
        ESValue result = originalObject.doIndirectCall(evaluator, originalObject, "charAt", new ESValue[] { ESNumber.valueOf(10) });
        assertEquals(new ESString(""),result);
    }
    
    @Test
    public void charAtWorksPassedNumber() throws Exception {
        ESObject originalObject = ESNumber.valueOf(123).toESObject(evaluator);
        ESValue  charAtFunction = getStringPrototype().getProperty("charAt", "charAt".hashCode());
        ESValue result = charAtFunction.callFunction(originalObject, new ESValue[] { ESNumber.valueOf(1) });
        assertEquals(new ESString("2"),result);
    }
    
    @Test
    public void indexedAccessToString() throws Exception {
        ESObject originalObject = new ESString("start").toESObject(evaluator);
        assertEquals(new ESString("a"), originalObject.getProperty("2","2".hashCode()));
    }
    
    @Test
    public void accessToPropertyOnStringObject() throws Exception {
        ESObject originalObject = new ESString("start").toESObject(evaluator);
        originalObject.putProperty("z", new ESString("x"), "z".hashCode());
        assertEquals(new ESString("x"), originalObject.getProperty("z","z".hashCode()));
    }

    @Test
    public void trimShoulRemoveLeadingSpaces() throws Exception {
        ESObject originalObject = new ESString(" start").toESObject(evaluator);
        ESValue trimmed = originalObject.doIndirectCall(evaluator, originalObject, "trim", ESValue.EMPTY_ARRAY);
        assertEquals("start",trimmed.toString());
    }
    
    @Test
    public void trimShouldRemoveLeadingAndTrilingUnicodeSpaces() throws Exception {
        ESObject originalObject = new ESString(ASCII_WHITE_SPACE +
                 "\u0001sta rt"
                + NON_ASCII_WHITE_SPACE
		).toESObject(evaluator);
        ESValue trimmed = originalObject.doIndirectCall(evaluator, originalObject, "trim", ESValue.EMPTY_ARRAY);
        assertEquals("\u0001sta rt",trimmed.toString());
    }
    
    @Test
    public void trimShoulRemoveTrailingSpaces() throws Exception {
        ESObject originalObject = new ESString(" start  ").toESObject(evaluator);
        ESValue trimmed = originalObject.doIndirectCall(evaluator, originalObject, "trim", ESValue.EMPTY_ARRAY);
        assertEquals("start",trimmed.toString());
    }
    
    @Test
    public void trimShouldTrimeEmptyString() throws Exception {
        ESObject originalObject = new ESString("").toESObject(evaluator);
        ESValue trimmed = originalObject.doIndirectCall(evaluator, originalObject, "trim", ESValue.EMPTY_ARRAY);
        assertEquals("",trimmed.toString());
    }
    
    @Test
    public void trimShouldTrimeWhiteSpaceString() throws Exception {
        ESObject originalObject = new ESString(ASCII_WHITE_SPACE+NON_ASCII_WHITE_SPACE).toESObject(evaluator);
        ESValue trimmed = originalObject.doIndirectCall(evaluator, originalObject, "trim", ESValue.EMPTY_ARRAY);
        assertEquals("",trimmed.toString());
    }
    
    private ESObject getStringPrototype() throws EcmaScriptException {
        ESObject stringObject = (ESObject) evaluator.getGlobalObject().getProperty("String","String".hashCode());
        ESObject stringPrototype = (ESObject) stringObject.getProperty(StandardProperty.PROTOTYPEstring,StandardProperty.PROTOTYPEhash);
        return stringPrototype;
    }
    
}
