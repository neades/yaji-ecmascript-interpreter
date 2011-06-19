package FESI.Interpreter;

import static org.junit.Assert.assertEquals;

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
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESValue;
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
        
        assertEquals(ESBoolean.makeBoolean(true), result);
    }

    @Test
    public void shouldEvaluateStrictEquality_UnequalType() throws Exception {
        ESValue result = evaluateBinaryExpression("'1' === 1");
        
        assertEquals(ESBoolean.makeBoolean(false), result);
    }

    @Test
    public void shouldEvaluateStrictEquality_UndefinedEquals() throws Exception {
        ESValue result = evaluateBinaryExpression("undefined === undefined");
        
        assertEquals(ESBoolean.makeBoolean(true), result);
    }

    @Test
    public void shouldEvaluateStrictEquality_NullEquals() throws Exception {
        ESValue result = evaluateBinaryExpression("null === null");
        
        assertEquals(ESBoolean.makeBoolean(true), result);
    }

    @Test
    public void shouldEvaluateStrictEquality_NanNotEquals() throws Exception {
        ESValue result = evaluateBinaryExpression("NaN === NaN");
        
        assertEquals(ESBoolean.makeBoolean(false), result);
    }

    @Test
    public void shouldEvaluateStrictEquality_NanNotEquals1() throws Exception {
        ESValue result = evaluateBinaryExpression("1 === NaN");
        
        assertEquals(ESBoolean.makeBoolean(false), result);
    }

    @Test
    public void shouldEvaluateStrictEquality_1NotEqualsNaN() throws Exception {
        ESValue result = evaluateBinaryExpression("NaN === 1");
        
        assertEquals(ESBoolean.makeBoolean(false), result);
    }
    
    @Test
    public void shouldCatchThrownMessage() throws Exception {
        String sourceText = "try { var t = does.not.exist; } catch ( e ) { return true; } return false;";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESBoolean.makeBoolean(true),result);
    }

    @Test
    public void shouldExecuteFinallyAfterException() throws Exception {
        String sourceText = "try { var t = does.not.exist; } catch ( e ) { return true; } finally { global = true; } return false;";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESBoolean.makeBoolean(true),result);
        assertEquals(ESBoolean.makeBoolean(true),evaluator.getGlobalObject().getProperty("global", "global".hashCode()));
    }

    @Test
    public void shouldExecuteFinallyWithoutException() throws Exception {
        String sourceText = "try { 2; } catch ( e ) { return true; } finally { global = true; }";
        EcmaScript es = new EcmaScript(new StringReader(sourceText));
        
        ASTStatement statement = (ASTStatement) es.Program().jjtGetChild(0);
        
        EcmaScriptEvaluateVisitor visitor = createVisitor();
        ESValue result = (ESValue) visitor.visit(statement,EcmaScriptEvaluateVisitor.FOR_VALUE);
        assertEquals(ESNumber.valueOf(2),result);
        assertEquals(ESBoolean.makeBoolean(true),evaluator.getGlobalObject().getProperty("global", "global".hashCode()));
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
        assertEquals(ESBoolean.makeBoolean(true),result);
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
