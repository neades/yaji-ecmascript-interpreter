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
                // String literals
                { "'\\a';",
                    "Program" + eol
                    + " Statement" + eol
                    + "  [a]" + eol
                },
                { "'\\b\\t\\n\\v\\f\\r\\\"\\''",
                    "Program" + eol
                    + " Statement" + eol
                    + "  [\b\t\n\u000B\f\r\"']" + eol
                },
                { "'one\\\r\ntwo'",
                    "Program" + eol
                    + " Statement" + eol
                    + "  [onetwo]" + eol
                },
                { "return \"#\\\\\" + a;",
                    "Program" + eol
                    + " Statement" + eol
                    + "  ReturnStatement" + eol 
                    + "   BinaryExpressionSequence" + eol 
                    + "    [#\\]" + eol
                    + "    <\"+\">" + eol
                    + "    <a>" + eol
                },
                { "\"#\\\\\"",
                    "Program" + eol
                    + " Statement" + eol
                    + "  [#\\]" + eol
                },
                { "var r = /[a-z]*/g;",
                    "Program" + eol
                    + " Statement" + eol
                    + "  VariableDeclaration" + eol
                    + "   <r>" + eol
                    + "   /[a-z]*/g" + eol
                },
                { "var r = /[a-z]\\*/;",
                    "Program" + eol
                    + " Statement" + eol
                    + "  VariableDeclaration" + eol
                    + "   <r>" + eol
                    + "   /[a-z]\\*/" + eol
                },
                { "var a = { get b() { return 1; } };",
                    "Program" + eol
                    +" Statement" + eol
                    +"  VariableDeclaration" + eol
                    +"   <a>" + eol
                    +"   ObjectLiteral" + eol
                    +"    PropertyNameAndValue" + eol
                    +"     GetAccessor" + eol
                    +"     <b>" + eol
                    +"     StatementList" + eol
                    +"      Statement" + eol
                    +"       ReturnStatement" + eol
                    +"        [1]" + eol
                },
                { "var a = { get : 1 };",
                    "Program" + eol
                    +" Statement" + eol
                    +"  VariableDeclaration" + eol
                    +"   <a>" + eol
                    +"   ObjectLiteral" + eol
                    +"    PropertyNameAndValue" + eol
                    +"     <get>" + eol
                    +"     [1]" + eol
                },
                { "var d1 = BI_FV/yt, d2 = (1<<BI_F1)/yt, e = 1<<BI_F2;",
                    "Program" + eol
                    +" Statement" + eol
                    +"  StatementList" + eol
                    +"   VariableDeclaration" + eol
                    +"    <d1>" + eol
                    +"    BinaryExpressionSequence" + eol
                    +"     <BI_FV>" + eol
                    +"     <\"/\">" + eol
                    +"     <yt>" + eol
                    +"   VariableDeclaration" + eol
                    +"    <d2>" + eol
                    +"    BinaryExpressionSequence" + eol
                    +"     BinaryExpressionSequence" + eol
                    +"      [1]" + eol
                    +"      <\"<<\">" + eol
                    +"      <BI_F1>" + eol
                    +"     <\"/\">" + eol
                    +"     <yt>" + eol
                    +"   VariableDeclaration" + eol
                    +"    <e>" + eol
                    +"    BinaryExpressionSequence" + eol
                    +"     [1]" + eol
                    +"     <\"<<\">" + eol
                    +"     <BI_F2>" + eol
                },
                { "x /= y/z;",
                   "Program" + eol
                   +" Statement" + eol
                   +"  AssignmentExpression" + eol
                   +"   <x>" + eol
                   +"   <\"/=\">" + eol
                   +"   BinaryExpressionSequence" + eol
                   +"    <y>" + eol
                   +"    <\"/\">" + eol
                   +"    <z>" + eol
                },
                { "func(/ab/g);",
                    "Program" + eol
                    + " Statement" + eol 
                    + "  CompositeReference" + eol 
                    + "   <func>" + eol
                    + "   FunctionCallParameters" + eol 
                    + "    /ab/g" + eol
                },
                {
                    "new Date().getTimezoneOffset()",
                    "Program" + eol
                    + " Statement" + eol
                    + "  CompositeReference" + eol 
                    + "   AllocationExpression" + eol
                    + "    <Date>" + eol
                    + "    FunctionCallParameters" + eol
                    + "   PropertyIdentifierReference" + eol
                    + "    <getTimezoneOffset>" + eol
                    + "   FunctionCallParameters" + eol 
                },
                {  "// single line comment",
                    "Program" + eol
                }
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
