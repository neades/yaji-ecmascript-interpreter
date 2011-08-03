package FESI.AST;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ASTLiteralTest {

    private final String testName;
    private final String input;
    private final String output;

    public ASTLiteralTest(String testName, String input, String output) {
        this.testName = testName;
        this.input = input;
        this.output = output;
    }
    @Parameters
    public static Collection<String[]> data() {
        return Arrays.asList(new String[][] {
                { "EmptyString", "", "" },
                { "Simple Characters", "abc", "abc" },
                { "LineContinuationCRLF", "1\\\r\n2", "12" },
                { "LineContinuationLF", "1\\\n2", "12" },
                { "LineContinuationLS", "1\\\u20282", "12" },
                { "LineContinuationPS", "1\\\u20292", "12" },
                { "LineContinuationCR", "1\\\r2", "12" },
                { "EscapeSequence","\\b\\t\\n\\v\\f\\r\\\"\\'","\b\t\n\u000B\f\r\"'" },
                { "HexEscape", "\\x40", "@" },
                { "UnicodeEscape", "\\u2029\\u1234\\u1567\\u189a\\u1AbB\\u1CcD\\u1dEe\\u1Ff0", "\u2029\u1234\u1567\u189a\u1abb\u1ccd\u1dee\u1ff0" },
                { "OctalEscape", "\\100\\60\\61\\62\\63\\64\\65\\66\\67\\68", "@01234567\u00068" },
                { "IncompleteUnicodeEscape", "\\u123", "u123" },
                { "IncompleteHexEscape", "\\x1", "x1" },
                { "InvalidUnicodeSequence", "\\u123g", "u123g" },
                { "InvalidHexEscape", "\\xag", "xag" },
                { "TrailingBackSlash", "#\\\\", "#\\" },
                { "Nul", "\\0", "\0" }
                
        });
    }
        
    @Test
    public void test() {
        ASTLiteral literal = new ASTLiteral(0);
        literal.setStringValue(input);
        assertEquals(testName,output,literal.getValue().toString());
    }

}
