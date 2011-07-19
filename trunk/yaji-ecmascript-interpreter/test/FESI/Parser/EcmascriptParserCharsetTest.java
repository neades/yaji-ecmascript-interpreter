package FESI.Parser;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.junit.Test;

import FESI.AST.EcmaScriptDumpVisitor;


public class EcmascriptParserCharsetTest {
    private static String eol = System.getProperty("line.separator");

    @Test
    public void shouldParseUTF8BOM() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        String source = "true;";
        byte[] bytes = source.getBytes("UTF-8");
        byte[] byteSource = new byte[3 + bytes.length];
        byteSource[0] = (byte) 0xEF;
        byteSource[1] = (byte) 0xBB;
        byteSource[2] = (byte) 0xBF;
        System.arraycopy(bytes, 0, byteSource, 3, bytes.length);

        InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(byteSource),"UTF-8");
        EcmaScript es = new EcmaScript(reader);
        EcmaScriptDumpVisitor dumper = new EcmaScriptDumpVisitor(new PrintStream(baos));
        dumper.visit(es.Program(),null);
        
        String result = new String(baos.toByteArray());
        String expected = "Program" + eol
        + " Statement" + eol
        + "  [true]" + eol;
        assertEquals("Parsing "+source,expected,result);
    }

}
