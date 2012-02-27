package org.yaji.regex.jur;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JavaUtilRegexTest {

    @Test
    public void shouldConvertBackslashBToBS() throws Exception {
        assertEquals("[\u0008]",JavaUtilRegex.convertRegExp("[\\b]"));
    }
    
    @Test
    public void shouldNotConvertBackslashBToBSOutsideClassRange() throws Exception {
        assertEquals("\\[\\b\\]",JavaUtilRegex.convertRegExp("\\[\\b\\]"));
    }
    
    @Test
    public void shouldConvertEmptyClassRange() throws Exception {
        assertEquals("[^\\w\\W\\u0000]",JavaUtilRegex.convertRegExp("[]"));
    }
    
    @Test
    public void shouldConvertInverseEmptyClassRange() throws Exception {
        assertEquals(".",JavaUtilRegex.convertRegExp("[^]"));
    }
    
    @Test
    public void shouldConvertDecimalEscapeInClassToHex() throws Exception {
        assertEquals("[\\u000a\\u000b]",JavaUtilRegex.convertRegExp("[\\12\\13]"));
    }

}
