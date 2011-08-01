package FESI.Interpreter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FESI.AST.ASTBinaryExpressionSequence;
import FESI.AST.ASTFunctionExpression;
import FESI.AST.ASTObjectLiteral;
import FESI.AST.ASTStatement;
import FESI.AST.ASTVariableDeclaration;
import FESI.Data.ESBoolean;
import FESI.Data.ESNull;
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESValue;
import FESI.Data.ObjectObject;
import FESI.Data.ObjectPrototype;
import FESI.Exceptions.TypeError;
import FESI.Parser.EcmaScript;
import FESI.Parser.ParseException;


public class EcmaScriptEvaluateVisitorTest {

    private PrintStream out;
    private Evaluator evaluator;

    @Before
    public void setUp() {
        out = System.out;
    }
    
    @After
    public void tearDown() {
        System.setOut(out);
    }
    
    @Test
    public void shouldCreateAnObject() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        String sourceText = "var result = { test: 123, \"test2\" : 'aString' };";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        ASTVariableDeclaration varDec = (ASTVariableDeclaration) statement.jjtGetChild(0);
        ASTObjectLiteral objectLiteral = (ASTObjectLiteral) varDec.jjtGetChild(1);
        
        EcmaScriptEvaluateVisitor visitor = new EcmaScriptEvaluateVisitor(new Evaluator());
        ESObject object = (ESObject) visitor.visit(objectLiteral,EcmaScriptEvaluateVisitor.FOR_VALUE);
        
        assertEquals(ESNumber.valueOf(123), object.getProperty("test", "test".hashCode()));
        assertEquals(ESString.valueOf("aString"), object.getProperty("test2", "test2".hashCode()));
    }
    
    @Test
    public void shouldCreateFunctionObject() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        String sourceText = "var result = function () { return 123 };";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        ASTVariableDeclaration varDec = (ASTVariableDeclaration) statement.jjtGetChild(0);
        ASTFunctionExpression functionExpression = (ASTFunctionExpression) varDec.jjtGetChild(1);
        
        EcmaScriptEvaluateVisitor visitor = new EcmaScriptEvaluateVisitor(new Evaluator());
        ESObject object = (ESObject) visitor.visit(functionExpression,EcmaScriptEvaluateVisitor.FOR_VALUE);
        ESValue result = object.callFunction(object, ESValue.EMPTY_ARRAY);
        
        assertEquals(ESNumber.valueOf(123), result);
    }

    @Test
    public void shouldEvaluateStrictEquality_Equals() throws Exception {
        ESValue result = evaluateBinaryExpression("1 === 1");
        
        assertEquals(ESBoolean.valueOf(true), result);
    }

    @Test
    public void shouldEvaluateStrictEquality_UnequalType() throws Exception {
        ESValue result = evaluateBinaryExpression("'1' === 1");
        
        assertEquals(ESBoolean.valueOf(false), result);
    }

    @Test
    public void shouldEvaluateStrictEquality_UndefinedEquals() throws Exception {
        ESValue result = evaluateBinaryExpression("undefined === undefined");
        
        assertEquals(ESBoolean.valueOf(true), result);
    }

    @Test
    public void shouldEvaluateStrictEquality_NullEquals() throws Exception {
        ESValue result = evaluateBinaryExpression("null === null");
        
        assertEquals(ESBoolean.valueOf(true), result);
    }

    @Test
    public void shouldEvaluateStrictEquality_NanNotEquals() throws Exception {
        ESValue result = evaluateBinaryExpression("NaN === NaN");
        
        assertEquals(ESBoolean.valueOf(false), result);
    }

    @Test
    public void shouldEvaluateStrictEquality_NanNotEquals1() throws Exception {
        ESValue result = evaluateBinaryExpression("1 === NaN");
        
        assertEquals(ESBoolean.valueOf(false), result);
    }

    @Test
    public void shouldEvaluateStrictEquality_1NotEqualsNaN() throws Exception {
        ESValue result = evaluateBinaryExpression("NaN === 1");
        
        assertEquals(ESBoolean.valueOf(false), result);
    }
    
    @Test
    public void shouldEvaluateInAsTrue() throws Exception {
        EcmaScript es = new EcmaScript(new StringReader("'property' in p"));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        ASTBinaryExpressionSequence expression = (ASTBinaryExpressionSequence) statement.jjtGetChild(0);
        
        evaluator = new Evaluator() {
            private static final long serialVersionUID = -7746833632204914424L;
            {
                theScopeChain = globalScope;
            }
        };
        EcmaScriptEvaluateVisitor visitor = new EcmaScriptEvaluateVisitor(evaluator);
        ObjectPrototype p = ObjectObject.createObject(evaluator);
        p.putProperty("property", ESNull.theNull, "property".hashCode());
        evaluator.getGlobalObject().putProperty("p", p, "p".hashCode());
        ESValue result = (ESValue) visitor.visit(expression,EcmaScriptEvaluateVisitor.FOR_VALUE);
        
        assertEquals(ESBoolean.valueOf(true), result);
    }
    
    @Test
    public void shouldEvaluateInAsFalse() throws Exception {
        EcmaScript es = new EcmaScript(new StringReader("'prop' in p"));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        ASTBinaryExpressionSequence expression = (ASTBinaryExpressionSequence) statement.jjtGetChild(0);
        
        evaluator = new Evaluator() {
            private static final long serialVersionUID = -7746833632204914424L;
            {
                theScopeChain = globalScope;
            }
        };
        EcmaScriptEvaluateVisitor visitor = new EcmaScriptEvaluateVisitor(evaluator);
        ObjectPrototype p = ObjectObject.createObject(evaluator);
        p.putProperty("property", ESNull.theNull, "property".hashCode());
        evaluator.getGlobalObject().putProperty("p", p, "p".hashCode());
        ESValue result = (ESValue) visitor.visit(expression,EcmaScriptEvaluateVisitor.FOR_VALUE);
        
        assertEquals(ESBoolean.valueOf(false), result);
    }
    
    @Test
    public void inShouldThrowExceptionIfNotObject() throws Exception {
        EcmaScript es = new EcmaScript(new StringReader("'prop' in 'p'"));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        ASTBinaryExpressionSequence expression = (ASTBinaryExpressionSequence) statement.jjtGetChild(0);
        
        evaluator = new Evaluator() {
            private static final long serialVersionUID = -7746833632204914424L;
            {
                theScopeChain = globalScope;
            }
        };
        EcmaScriptEvaluateVisitor visitor = new EcmaScriptEvaluateVisitor(evaluator);
        ObjectPrototype p = ObjectObject.createObject(evaluator);
        p.putProperty("property", ESNull.theNull, "property".hashCode());
        evaluator.getGlobalObject().putProperty("p", p, "p".hashCode());
        try {
            visitor.visit(expression,EcmaScriptEvaluateVisitor.FOR_VALUE);
            fail("Should throw exception");
        } catch (PackagedException e) {
            assertTrue(e.exception instanceof TypeError);
        }
    }
    
    @Test
    public void shouldCatchThrownMessage() throws Exception {
        String sourceText = "try { var t = does.not.exist; } catch ( e ) { return true; } return false;";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESBoolean.valueOf(true),result);
    }

    @Test
    public void shouldExecuteFinallyAfterException() throws Exception {
        String sourceText = "try { var t = does.not.exist; } catch ( e ) { return true; } finally { global = true; } return false;";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESBoolean.valueOf(true),result);
        assertEquals(ESBoolean.valueOf(true),evaluator.getGlobalObject().getProperty("global", "global".hashCode()));
    }

    @Test
    public void shouldExecuteFinallyWithoutException() throws Exception {
        String sourceText = "try { 2; } catch ( e ) { return true; } finally { global = true; }";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESNumber.valueOf(2),result);
        assertEquals(ESBoolean.valueOf(true),evaluator.getGlobalObject().getProperty("global", "global".hashCode()));
    }

    @Test
    public void shouldExecuteFinallyWithoutExceptionAndReturnValue() throws Exception {
        String sourceText = "try { 2; } catch ( e ) { return true; } finally { return 4; }";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESNumber.valueOf(4),result);
    }

    @Test
    public void shouldPassError() throws Exception {
        String sourceText = "try { var error = does.not.exist; } catch ( e ) { return e.name === 'ReferenceError'; }";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESBoolean.valueOf(true),result);
    }
    
    @Test
    public void switchShouldHandleSimpleCaseSelection() throws Exception {
        String sourceText = "switch ( 'x' ) { case 'x' : return 123; }";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESNumber.valueOf(123),result);
    }

    @Test
    public void switchShouldHandleSimpleDefaultSelection() throws Exception {
        String sourceText = "switch ( 'y' ) { case 'x' : return 456; default: return 123 }";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESNumber.valueOf(123),result);
    }

    @Test
    public void switchShouldHandleCaseAfterDefault() throws Exception {
        String sourceText = "switch ( 'y' ) { case 'x' : return 456; default: return 678; case 'y': return 123; }";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESNumber.valueOf(123),result);
    }

    @Test
    public void switchShouldfallThrough() throws Exception {
        String sourceText = "switch ( 'y' ) { case 'y': var x = 0; case 'x' : return 123; default: return 678; }";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESNumber.valueOf(123),result);
    }

    @Test
    public void switchShouldFallThroughToDefault() throws Exception {
        String sourceText = "switch ( 'y' ) { case 'x' : return 678; case 'y': var x = 0; default: return 123; }";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESNumber.valueOf(123),result);
    }

    @Test
    public void switchShouldFallThroughDefault() throws Exception {
        String sourceText = "switch ( 'y' ) { case 'x' : return 678; default: var x = 0; case 'z': return 123; }";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESNumber.valueOf(123),result);
    }

    @Test
    public void switchShouldFallWithNoStatementList() throws Exception {
        String sourceText = "switch ( 'x' ) { case 'x' : default: return 123; }";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESNumber.valueOf(123),result);
    }

    @Test
    public void switchShouldBreak() throws Exception {
        String sourceText = "switch ( 'x' ) { case 'x' : 123; break; default: 456; }";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(EcmaScriptEvaluateVisitor.C_NORMAL,visitor.completionCode);
    }

    @Test
    public void switchShouldFallDefaultWithNoStatementList() throws Exception {
        String sourceText = "switch ( 'x' ) { case 'x' : default: case 'q' : return 123; }";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESNumber.valueOf(123),result);
    }

    @Test
    public void shouldThrowException() throws Exception {
        String sourceText = "throw new Error('test');";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        try {
            visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
            fail("Should throw exception");
        } catch (PackagedException e) {
            ESValue errorObject = e.exception.getErrorObject(evaluator);
            assertEquals("Error: test",errorObject.toString());
        }
    }

    @Test
    public void shouldInvokeFunctionsOnLiterals() throws Exception {
        String sourceText = "return 'A'.charCodeAt(0);";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESNumber.valueOf(65),result);
    }
    
    private EcmaScriptEvaluateVisitor createVisitor() {
        evaluator = new Evaluator() {
            private static final long serialVersionUID = -7746833632204914424L;
            {
                theScopeChain = globalScope;
            }
        };
        return new EcmaScriptEvaluateVisitor(evaluator) { {
            completionCode = C_NORMAL;
        } };
    }

    private ESValue evaluateBinaryExpression(String sourceText)
            throws ParseException {
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        ASTBinaryExpressionSequence expression = (ASTBinaryExpressionSequence) statement.jjtGetChild(0);

        EcmaScriptEvaluateVisitor visitor = new EcmaScriptEvaluateVisitor(new Evaluator() {
            private static final long serialVersionUID = -7746833632204914424L;
            {
                theScopeChain = globalScope;
            }
        });
        ESValue result = (ESValue) visitor.visit(expression,EcmaScriptEvaluateVisitor.FOR_VALUE);
        return result;
    }
}
