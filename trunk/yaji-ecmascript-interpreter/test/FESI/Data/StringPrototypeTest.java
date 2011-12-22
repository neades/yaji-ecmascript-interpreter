package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.TypeError;

public class StringPrototypeTest extends EvaluatorTestCase  {
    private static final Locale TURKISH = new Locale("tr");

    public static final String  NON_ASCII_WHITE_SPACE= 
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
        +"\u2029" //   Paragraph separator
        +"\uFEFF";//   Byte Order Mark

    public static final String ASCII_WHITE_SPACE = "\u0009\n\u000B\u000C\r\u0020";

    private Locale defaultLocale;
    
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        defaultLocale = Locale.getDefault();
    }
    
    @After
    public void tearDown() {
        Locale.setDefault(defaultLocale);
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

    @Test
    public void toLowerCaseIsLocaleAgnositic() throws Exception {
        Locale.setDefault(TURKISH);
        ESObject originalObject = new ESString("TITLE").toESObject(evaluator);
        ESValue lowerCase = originalObject.doIndirectCall(evaluator, originalObject, "toLowerCase", ESValue.EMPTY_ARRAY);
        assertEquals("title",lowerCase.toString());
    }
    
    @Test(expected=TypeError.class)
    public void toLowerCaseDisallowsNull() throws Exception {
        ESValue function = getStringPrototype().getProperty("toLowerCase");
        function.callFunction(ESNull.theNull, ESValue.EMPTY_ARRAY);
    }
    
    @Test(expected=TypeError.class)
    public void toLowerCaseDisallowsUndefined() throws Exception {
        ESValue function = getStringPrototype().getProperty("toLowerCase");
        function.callFunction(ESUndefined.theUndefined, ESValue.EMPTY_ARRAY);
    }
    
    @Test
    public void toLocaleLowerCaseIsntLocaleAgnositic() throws Exception {
        Locale.setDefault(TURKISH);
        ESObject originalObject = new ESString("TITLE").toESObject(evaluator);
        ESValue lowerCase = originalObject.doIndirectCall(evaluator, originalObject, "toLocaleLowerCase", ESValue.EMPTY_ARRAY);
        assertEquals("t\u0131tle",lowerCase.toString());
    }
    

    @Test
    public void toUpperCaseIsLocaleAgnositic() throws Exception {
        Locale.setDefault(TURKISH);
        ESObject originalObject = new ESString("title").toESObject(evaluator);
        ESValue upperCase = originalObject.doIndirectCall(evaluator, originalObject, "toUpperCase", ESValue.EMPTY_ARRAY);
        assertEquals("TITLE",upperCase.toString());
    }
    
    @Test(expected=TypeError.class)
    public void toUpperCaseDisallowsNull() throws Exception {
        ESValue function = getStringPrototype().getProperty("toUpperCase");
        function.callFunction(ESNull.theNull, ESValue.EMPTY_ARRAY);
    }
    
    @Test(expected=TypeError.class)
    public void toUpperCaseDisallowsUndefined() throws Exception {
        ESValue function = getStringPrototype().getProperty("toUpperCase");
        function.callFunction(ESUndefined.theUndefined, ESValue.EMPTY_ARRAY);
    }
    
    @Test
    public void  substringBasicOperation() throws Exception {
        assertEquals("it",execSubstring(1, 3));
    }

    @Test
    public void  substringEndPastEndSetToEnd() throws Exception {
        assertEquals("le",execSubstring(3, 10));
    }
    
    @Test
    public void  substringStartPastEndSetToEndAndReversed() throws Exception {
        assertEquals("le",execSubstring(10, 3));
    }
    
    @Test
    public void  substringStartNegativeSetToZero() throws Exception {
        assertEquals("tit",execSubstring(-1, 3));
    }
    
    @Test
    public void  substringEndNegativeSetToZero() throws Exception {
        assertEquals("tit",execSubstring(3, -1));
    }
    
    @Test
    public void  substringStartAsNanSetToZero() throws Exception {
        ESObject originalObject = new ESString("title").toESObject(evaluator);
        ESValue result = originalObject.doIndirectCall(evaluator, originalObject, "substring", new ESValue[] { ESNumber.valueOf(Double.NaN), ESNumber.valueOf(3) });
        assertEquals("tit",result.toString());
    }
    
    @Test
    public void  substringEndAsNanSetToZero() throws Exception {
        ESObject originalObject = new ESString("title").toESObject(evaluator);
        ESValue result = originalObject.doIndirectCall(evaluator, originalObject, "substring", new ESValue[] { ESNumber.valueOf(3), ESNumber.valueOf(Double.NaN) });
        assertEquals("tit",result.toString());
    }
    
    @Test(expected=TypeError.class)
    public void  substringAppliedToNullGeneratesError() throws Exception {
        ESValue function = getStringPrototype().getProperty("substring");
        function.callFunction(ESNull.theNull, ESValue.EMPTY_ARRAY);
    }
    
    @Test(expected=TypeError.class)
    public void  substringAppliedToUndefinedGeneratesError() throws Exception {
        ESValue function = getStringPrototype().getProperty("substring");
        function.callFunction(ESUndefined.theUndefined, ESValue.EMPTY_ARRAY);
    }
    
    @Test
    public void toLocaleUpperCaseIsntLocaleAgnositic() throws Exception {
        Locale.setDefault(TURKISH);
        ESObject originalObject = new ESString("title").toESObject(evaluator);
        ESValue lowerCase = originalObject.doIndirectCall(evaluator, originalObject, "toLocaleUpperCase", ESValue.EMPTY_ARRAY);
        assertEquals("T\u0130TLE",lowerCase.toString());
    }

    @Test
    public void splitWithNoSeparatorReturnsString() throws Exception {
        ESObject originalObject = new ESString("title").toESObject(evaluator);
        ESValue splitResult = originalObject.doIndirectCall(evaluator, originalObject, "split", ESValue.EMPTY_ARRAY);
        assertEquals(new ESString("title"),splitResult);
    }
    
    @Test
    public void splitWithStringReturnsArrayOfStrings() throws Exception {
        ESObject originalObject = new ESString("a,b,c").toESObject(evaluator);
        ESObject splitResult = (ESObject) originalObject.doIndirectCall(evaluator, originalObject, "split", new ESValue[] { new ESString(",") });
        assertEquals("a",splitResult.getProperty(0L).toString());
        assertEquals("b",splitResult.getProperty(1L).toString());
        assertEquals("c",splitResult.getProperty(2L).toString());
        assertEquals(3,splitResult.getProperty(StandardProperty.LENGTHstring).toInt32());
    }
    
    @Test
    public void splitWithEmptyStringReturnsArrayOfcharacters() throws Exception {
        ESObject originalObject = new ESString("abc").toESObject(evaluator);
        ESObject splitResult = (ESObject) originalObject.doIndirectCall(evaluator, originalObject, "split", new ESValue[] { new ESString("") });
        assertEquals("a",splitResult.getProperty(0L).toString());
        assertEquals("b",splitResult.getProperty(1L).toString());
        assertEquals("c",splitResult.getProperty(2L).toString());
        assertEquals(3,splitResult.getProperty(StandardProperty.LENGTHstring).toInt32());
    }
    @Test
    public void splitAcceptsNonStringValue() throws Exception {
        ESValue arrayObject = evaluator.getGlobalObject().getProperty("Array");
        ESObject a = arrayObject.doConstruct(new ESValue[] { new ESString("abc"), ESNumber.valueOf("123"), new ESString("xyz") });
        ESValue function = getStringPrototype().getProperty("split");
        ESObject splitResult = (ESObject) function.callFunction(a, new ESValue[] { new ESString(",") });
        assertEquals("abc",splitResult.getProperty(0L).toString());
        assertEquals(new ESString("123"),splitResult.getProperty(1L));
        assertEquals("xyz",splitResult.getProperty(2L).toString());
        assertEquals(3,splitResult.getProperty(StandardProperty.LENGTHstring).toInt32());
    }


    @Test(expected=TypeError.class)
    public void splitRejectsNullAsThis() throws Exception {
        ESValue function = getStringPrototype().getProperty("split");
        function.callFunction(ESNull.theNull, new ESValue[] { new ESString(",") });
    }
    
    @Test(expected=TypeError.class)
    public void splitRejectsUndefinedAsThis() throws Exception {
        ESValue function = getStringPrototype().getProperty("split");
        function.callFunction(ESUndefined.theUndefined, new ESValue[] { new ESString(",") });
    }
    
    @Test
    public void splitWithRegexpReturnsArrayOfStrings() throws Exception {
        ESObject originalObject = new ESString("A<B>bold</B>").toESObject(evaluator);
        ESObject splitResult = (ESObject) originalObject.doIndirectCall(evaluator, originalObject, "split", new ESValue[] { createRegExp("[<>]") });
        assertEquals("A",splitResult.getProperty(0L).toString());
        assertEquals("B",splitResult.getProperty(1L).toString());
        assertEquals("bold",splitResult.getProperty(2L).toString());
        assertEquals("/B",splitResult.getProperty(3L).toString());
        assertEquals("",splitResult.getProperty(4L).toString());
        assertEquals(5,splitResult.getProperty(StandardProperty.LENGTHstring).toInt32());
    }

    @Test
    public void splitWithRegexpReturnsLimitedArrayOfStrings() throws Exception {
        ESObject originalObject = new ESString("A<B>bold</B>").toESObject(evaluator);
        ESObject splitResult = (ESObject) originalObject.doIndirectCall(evaluator, originalObject, "split", new ESValue[] { createRegExp("[<>]"), ESNumber.valueOf(2) });
        assertEquals("A",splitResult.getProperty(0L).toString());
        assertEquals("B",splitResult.getProperty(1L).toString());
        assertEquals(2,splitResult.getProperty(StandardProperty.LENGTHstring).toInt32());
    }

    @Test
    public void splitIncludesCaptureGroups() throws Exception {
        ESObject originalObject = new ESString("A<B>bold</B>and<CODE>coded</CODE>").toESObject(evaluator);
        ESObject splitResult = (ESObject) originalObject.doIndirectCall(evaluator, originalObject, "split", new ESValue[] { createRegExp("<(\\/)?([^<>]+)>") });
        assertEquals("A,,B,bold,/,B,and,,CODE,coded,/,CODE,",splitResult.doIndirectCall(evaluator, splitResult, "toString", ESValue.EMPTY_ARRAY).toString());
        assertEquals(13,splitResult.getProperty(StandardProperty.LENGTHstring).toInt32());
    }

    @Test
    public void splitBehavesSesnsiblyWithSimpleRegexp() throws Exception {
        ESObject originalObject = new ESString("a,b:c").toESObject(evaluator);
        ESObject splitResult = (ESObject) originalObject.doIndirectCall(evaluator, originalObject, "split", new ESValue[] { createRegExp("[,:]") });
        assertEquals("a,b,c",splitResult.doIndirectCall(evaluator, splitResult, "toString", ESValue.EMPTY_ARRAY).toString());
        assertEquals(3,splitResult.getProperty(StandardProperty.LENGTHstring).toInt32());
    }

//    Current behaviour doesn't meet spec. The folling two tests should work
//    @Test
//    public void splitDoesntBacktrack() throws Exception {
//        ESObject originalObject = new ESString("ab").toESObject(evaluator);
//        ArrayPrototype splitResult = (ArrayPrototype) originalObject.doIndirectCall(evaluator, originalObject, "split", new ESValue[] { createRegExp("a*?") });
//        assertEquals("a,b",splitResult.doIndirectCall(evaluator, splitResult, "toString", ESValue.EMPTY_ARRAY).toString());
//        assertEquals(2,splitResult.size());
//    }
//    
//    @Test
//    public void splitDoesntBacktrack2() throws Exception {
//        ESObject originalObject = new ESString("ab").toESObject(evaluator);
//        ArrayPrototype splitResult = (ArrayPrototype) originalObject.doIndirectCall(evaluator, originalObject, "split", new ESValue[] { createRegExp("a*") });
//        assertEquals(",b",splitResult.doIndirectCall(evaluator, splitResult, "toString", ESValue.EMPTY_ARRAY).toString());
//        assertEquals(2,splitResult.size());
//    }
    
    @Test
    public void sliceShouldDoABasicSlice() throws Exception {
        assertEquals(new ESString("bcd"),callSlice("abcdef", ESNumber.valueOf(1), ESNumber.valueOf(4)));
    }

    @Test
    public void sliceNegativeStartShouldCountFromEnd() throws Exception {
        assertEquals(new ESString("de"),callSlice("abcdef", ESNumber.valueOf(-3), ESNumber.valueOf(5)));
    }
    
    @Test
    public void sliceNegativeEndShouldCountFromEnd() throws Exception {
        assertEquals(new ESString("abcde"),callSlice("abcdef", ESNumber.valueOf(0), ESNumber.valueOf(-1)));
    }
    
    @Test
    public void sliceUndefinedEndShouldBeStringLength() throws Exception {
        assertEquals(new ESString("cdef"),callSlice("abcdef", ESNumber.valueOf(2), ESUndefined.theUndefined));
    }
    
    @Test
    public void sliceEndGreaterThanStartReturnsEmptyString() throws Exception {
        assertEquals(new ESString(""),callSlice("abcdef", ESNumber.valueOf(2), ESNumber.valueOf(1)));
    }
    
    @Test
    public void sliceNegativeStartGreaterThenLengthIsZero() throws Exception {
        assertEquals(new ESString("abc"),callSlice("abcdef", ESNumber.valueOf(-10), ESNumber.valueOf(3)));
    }
    
    @Test
    public void sliceStartGreaterThanEndIsEmptyString() throws Exception {
        assertEquals(new ESString(""),callSlice("abcdef", ESNumber.valueOf(10), ESNumber.valueOf(13)));
    }
    
    @Test
    public void sliceNegativeEndBeforeStringStartReturnsEmptyString() throws Exception {
        assertEquals(new ESString(""),callSlice("abcdef", ESNumber.valueOf(10), ESNumber.valueOf(-13)));
    }
    
    @Test
    public void sliceEndGreaterLengthShouldBeLength() throws Exception {
        assertEquals(new ESString("def"),callSlice("abcdef", ESNumber.valueOf(3), ESNumber.valueOf(13)));
    }

    @Test
    public void localeCompareReturnsZeroForSameValue() throws Exception {
        ESObject theThis = new ESString("aaaaaa").toESObject(evaluator);
        assertEquals(ESNumber.valueOf(0), theThis.doIndirectCall(evaluator, theThis, "localeCompare", new ESValue[] { new ESString("aaaaaa") }));
    }
    
    @Test
    public void localeCompareReturnsGreaterZeroForThisGreaterThanThatValue() throws Exception {
        ESObject theThis = new ESString("bbbbbb").toESObject(evaluator);
        assertTrue(theThis.doIndirectCall(evaluator, theThis, "localeCompare", new ESValue[] { new ESString("aaaaaa") }).toInt32() > 0);
    }
    
    @Test
    public void localeCompareReturnsLessThanZeroForThisLessThanThatValue() throws Exception {
        ESObject theThis = new ESString("bbbbbb").toESObject(evaluator);
        assertTrue(theThis.doIndirectCall(evaluator, theThis, "localeCompare", new ESValue[] { new ESString("cccccc") }).toInt32() < 0);
    }
    
//    @Test
//    public void localeCompareComparesInEnglish() throws Exception {
//        evaluator.setDefaultLocale(Locale.ENGLISH);
//        ESObject theThis = new ESString("p").toESObject(evaluator);
//        assertTrue(theThis.doIndirectCall(evaluator, theThis, "localeCompare", new ESValue[] { new ESString("\u00F6") }).toInt32() < 0);
//    }
//    
//    @Test
//    public void localeCompareComparesInGerman() throws Exception {
//        evaluator.setDefaultLocale(new Locale("sv_SE"));
//        ESObject theThis = new ESString("p").toESObject(evaluator);
//        assertTrue(theThis.doIndirectCall(evaluator, theThis, "localeCompare", new ESValue[] { new ESString("\u00F6") }).toInt32() < 0);
//    }
//    
    private ESObject getStringPrototype() throws EcmaScriptException {
        ESObject stringObject = (ESObject) evaluator.getGlobalObject().getProperty("String","String".hashCode());
        ESObject stringPrototype = (ESObject) stringObject.getProperty(StandardProperty.PROTOTYPEstring,StandardProperty.PROTOTYPEhash);
        return stringPrototype;
    }

    private String execSubstring(int start, int end) throws EcmaScriptException, NoSuchMethodException {
        ESObject originalObject = new ESString("title").toESObject(evaluator);
        ESValue result = originalObject.doIndirectCall(evaluator, originalObject, "substring", new ESValue[] { ESNumber.valueOf(start), ESNumber.valueOf(end) });
        String string = result.toString();
        return string;
    }

    private ESObject createRegExp(String regexp) throws EcmaScriptException {
        ESValue regExpConstructor = evaluator.getGlobalObject().getProperty("RegExp");
        ESObject regExp = regExpConstructor.doConstruct( new ESValue[] { new ESString(regexp) });
        return regExp;
    }
    
    private ESValue callSlice(String string, ESValue start, ESValue end) throws EcmaScriptException, NoSuchMethodException {
        ESObject originalObject = new ESString(string).toESObject(evaluator);
        ESValue value = originalObject.doIndirectCall(evaluator, originalObject, "slice", new ESValue[] { start, end });
        return value;
    }

}
