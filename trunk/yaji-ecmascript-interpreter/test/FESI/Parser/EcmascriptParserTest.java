package FESI.Parser;


import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import FESI.AST.EcmaScriptDumpVisitor;

@RunWith(Parameterized.class)
public class EcmascriptParserTest {
    private String source;
    private String expected;
    private PrintStream originalOut;
    private static String eol = System.getProperty("line.separator");

    @Parameters
    public static Collection<String[]> data() {
        return Arrays.asList(new String[][] {
                // Simple test of parsing a program
                { "true;", "Program" + eol
                    + " Statement" + eol
                    + "  [true]" + eol
                },
                // A simple object literal
                { "var object = { f:1 };", "Program" + eol
                    + " Statement" + eol
                    + "  VariableDeclaration" + eol 
                    + "   <object>" + eol
                    + "   ObjectLiteral" + eol
                    + "    PropertyNameAndValue" + eol 
                    + "     <f>" + eol 
                    + "     [1]" + eol
                },
                // More Complete Object literal
                { "var object = { f:1, 1:'ten', 'x':\"y\", \"z\" : { } , };", "Program" + eol
                    + " Statement" + eol
                    + "  VariableDeclaration" + eol 
                    + "   <object>" + eol
                    + "   ObjectLiteral" + eol
                    + "    PropertyNameAndValue" + eol 
                    + "     <f>" + eol 
                    + "     [1]" + eol
                    + "    PropertyNameAndValue" + eol 
                    + "     [1]" +  eol
                    + "     [ten]" + eol 
                    + "    PropertyNameAndValue" + eol 
                    + "     [x]" + eol
                    + "     [y]" + eol
                    + "    PropertyNameAndValue" + eol 
                    + "     [z]" + eol
                    + "     ObjectLiteral" + eol
                },
                // Simple function expression
                { "var func = function () {};", "Program" + eol 
                        + " Statement" + eol
                        + "  VariableDeclaration" + eol
                        + "   <func>" + eol
                        + "   FunctionExpression" + eol 
                        + "    FormalParameterList" + eol
                        + "    StatementList" + eol 
                },
                // Named function expression
                { "var func = function namedFunction () {};", "Program" + eol 
                        + " Statement" + eol
                        + "  VariableDeclaration" + eol
                        + "   <func>" + eol
                        + "   FunctionExpression" + eol
                        + "    <namedFunction>" + eol
                        + "    FormalParameterList" + eol
                        + "    StatementList" + eol 
                },
                // Strict equals
                { "var result = '1' === 1;", "Program" + eol 
                        + " Statement" + eol
                        + "  VariableDeclaration" + eol
                        + "   <result>" + eol
                        + "   BinaryExpressionSequence" + eol
                        + "    [1]" + eol
                        + "    <\"===\">" + eol
                        + "    [1]" + eol
                },
                // A simple array literal
                { "var array = [1];", "Program" + eol
                    + " Statement" + eol
                    + "  VariableDeclaration" + eol
                    + "   <array>" + eol
                    + "   ArrayLiteral" + eol
                    + "    [1]" + eol
                },
                // More complete array literal
                { "var array = ['a', ,1,Math.round(3.14159),,];", "Program" + eol
                    + " Statement" + eol
                    + "  VariableDeclaration" + eol
                    + "   <array>" + eol
                    + "   ArrayLiteral" + eol
                    + "    [a]" + eol
                    + "    [1]" + eol
                    + "    CompositeReference" + eol
                    + "     <Math>" + eol
                    + "     PropertyIdentifierReference" + eol
                    + "      <round>" + eol
                    + "     FunctionCallParameters" + eol
                    + "      [3.14159]" + eol
                }
            });
    }

    public EcmascriptParserTest(String source, String expected) {
        super();
        this.source = source;
        this.expected = expected;
    }

    @Before
    public void setUp() {
        originalOut = System.out;
    }
    
    @After
    public void tearDown() {
        System.setOut(originalOut);
    }
    
    @Test
    public void shouldParse() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        EcmaScript es = new EcmaScript(new StringReader(source));
        EcmaScriptDumpVisitor dumper = new EcmaScriptDumpVisitor();
        dumper.visit(es.Program(),null);
        
        String result = new String(baos.toByteArray());
        assertEquals("Parsing "+source,expected,result);
    }
}
