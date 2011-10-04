package org.yaji.json;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import FESI.Data.ESNumber;
import FESI.Data.ESString;
import FESI.Exceptions.EcmaScriptException;

public class JsonIndentTest {

    @Test
    public void testNullJsonIndent() {
        JsonIndent indent = JsonIndent.create();
        assertEquals("",indent.start());
        assertEquals(",",indent.separator());
        assertEquals("",indent.preValue());
        assertEquals("",indent.end());
        indent.push();
        assertEquals("",indent.start());
        assertEquals(",",indent.separator());
        assertEquals("",indent.preValue());
        assertEquals("",indent.end());
        indent.pop();
        assertEquals("",indent.start());
        assertEquals(",",indent.separator());
        assertEquals("",indent.preValue());
        assertEquals("",indent.end());
    }

    @Test
    public void testSingleSpaceJsonIndent() throws EcmaScriptException {
        JsonIndent indent = JsonIndent.create(ESString.valueOf(" "));
        indent.push();
        assertEquals("\n ",indent.start());
        assertEquals(",\n ",indent.separator());
        assertEquals(" ",indent.preValue());
        assertEquals("\n",indent.end());
    }

    @Test
    public void testSingleSpaceJsonIndentPushedTwice() throws EcmaScriptException {
        JsonIndent indent = JsonIndent.create(ESString.valueOf(" "));
        indent.push();
        indent.push();
        assertEquals("\n  ",indent.start());
        assertEquals(",\n  ",indent.separator());
        assertEquals(" ",indent.preValue());
        assertEquals("\n ",indent.end());
    }

    @Test
    public void testSingleSpaceAsNumberJsonIndentPushed() throws EcmaScriptException {
        JsonIndent indent = JsonIndent.create(ESNumber.valueOf(11));
        indent.push();
        assertEquals("\n          ",indent.start());
        assertEquals(",\n          ",indent.separator());
        assertEquals(" ",indent.preValue());
        assertEquals("\n",indent.end());
    }

    @Test
    public void test11SoacesJsonIndentPushed() throws EcmaScriptException {
        JsonIndent indent = JsonIndent.create(ESString.valueOf("            "));
        indent.push();
        assertEquals("\n          ",indent.start());
        assertEquals(",\n          ",indent.separator());
        assertEquals(" ",indent.preValue());
        assertEquals("\n",indent.end());
    }

}
