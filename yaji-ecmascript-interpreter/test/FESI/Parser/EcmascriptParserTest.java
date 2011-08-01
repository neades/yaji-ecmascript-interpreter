package FESI.Parser;


import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import FESI.AST.EcmaScriptDumpVisitor;

@RunWith(Parameterized.class)
public class EcmascriptParserTest {
    private String source;
    private String expected;
    private static String eol = System.getProperty("line.separator");

    @Parameters
    public static Collection<String[]> data() {
        return Arrays.asList(new String[][] {
                // try/catch
                { "try { true; } catch( e ) { false; }", 
                    "Program" + eol
                    + " Statement" + eol 
                    + "  TryStatement" + eol 
                    + "   StatementList" +  eol
                    + "    Statement" +  eol
                    + "     [true]" +  eol
                    + "   Catch" +  eol
                    + "    <e>" +  eol
                    + "    StatementList" +  eol
                    + "     Statement" + eol 
                    + "      [false]" + eol
                },
                // try/catch/finally
                { "try { true; } catch( e ) { false; } finally { true; }", 
                    "Program" + eol
                    + " Statement" + eol 
                    + "  TryStatement" + eol 
                    + "   StatementList" +  eol
                    + "    Statement" +  eol
                    + "     [true]" +  eol
                    + "   Catch" +  eol
                    + "    <e>" +  eol
                    + "    StatementList" +  eol
                    + "     Statement" + eol 
                    + "      [false]" + eol
                    + "   Finally" + eol
                    + "    StatementList" +  eol
                    + "     Statement" + eol 
                    + "      [true]" + eol
                },
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
                // Strict not equals
                { "var result = null !== void 0", "Program" + eol 
                	 + " Statement" + eol
                     + "  VariableDeclaration" + eol
                     + "   <result>" + eol
                	 + "   BinaryExpressionSequence" + eol
                	 + "    [null]" + eol
                	 + "    <\"!==\">" + eol
                	 + "    UnaryExpression" + eol
                	 + "     <\"void\">" + eol
                	 + "     [0]" + eol
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
                  + "    Elision" + eol
                  + "    [1]" + eol
                  + "    CompositeReference" + eol
                  + "     <Math>" + eol
                  + "     PropertyIdentifierReference" + eol
                  + "      <round>" + eol
                  + "     FunctionCallParameters" + eol
                  + "      [3.14159]" + eol
                  + "    Elision" + eol
                  + "    Elision" + eol
                },
                // throw Statement
                { "throw new Error();", "Program" + eol  
                  + " Statement" + eol
                  + "  ThrowStatement" + eol
                  + "   AllocationExpression" + eol
                  + "    <Error>" + eol
                  + "    FunctionCallParameters" + eol
                },
                { "x instanceof TypeError", "Program" + eol
                    + " Statement" + eol
                    + "  BinaryExpressionSequence" + eol
                    + "   <x>" + eol
                    + "   <\"instanceof\">" + eol
                    + "   <TypeError>" + eol
                },
                { "x in object", "Program" + eol
                    + " Statement" + eol
                    + "  BinaryExpressionSequence" + eol
                    + "   <x>" + eol
                    + "   <\"in\">" + eol
                    + "   <object>" + eol
                },
                { "switch (x) { case 'x': r = 1; break; default: r=2; }", "Program" + eol
                    + " Statement" + eol
                    + "  SwitchStatement" + eol
                    + "   <x>" + eol
                    + "   CaseClause" + eol
                    + "    [x]" + eol 
                    + "    StatementList" + eol 
                    + "     Statement" + eol 
                    + "      AssignmentExpression" + eol 
                    + "       <r>" + eol 
                    + "       <\"=\">" + eol 
                    + "       [1]" + eol 
                    + "     Statement" + eol 
                    + "      BreakStatement" + eol 
                    + "   DefaultClause" + eol 
                    + "    StatementList" + eol 
                    + "     Statement" + eol 
                    + "      AssignmentExpression" + eol 
                    + "       <r>" + eol
                    + "       <\"=\">" + eol 
                    + "       [2]" + eol
                },
                // do-while
                { "do { x(); } while (true)",
                    "Program" + eol
                    + " Statement" + eol
                    + "  DoWhileStatement" + eol
                    + "   Statement" + eol
                    + "    StatementList" + eol 
                    + "     Statement" + eol 
                    + "      CompositeReference" + eol
                    + "       <x>" + eol 
                    + "       FunctionCallParameters" + eol
                    + "   [true]" + eol
                },
                // Dereferencing Literals
                { "'string'.charCodeAt(0)",
                    "Program" + eol
                    + " Statement" + eol
                    + "  CompositeReference" + eol
                    + "   [string]" + eol
                    + "   PropertyIdentifierReference" + eol
                    + "    <charCodeAt>" + eol
                    + "   FunctionCallParameters" + eol
                    + "    [0]" + eol
                },
//                { "'\\a'",
//                    "Program" + eol
//                    + " Statement" + eol
//                    + "  [\\a]" + eol
//                }
            });
    }

    public EcmascriptParserTest(String source, String expected) {
        super();
        this.source = source;
        this.expected = expected;
    }

    @Test
    public void shouldParse() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        EcmaScript es = new EcmaScript(new StringReader(source));
        EcmaScriptDumpVisitor dumper = new EcmaScriptDumpVisitor(new PrintStream(baos));
        dumper.visit(es.Program(),null);
        
        String result = new String(baos.toByteArray());
        assertEquals("Parsing "+source,expected,result);
    }
}
