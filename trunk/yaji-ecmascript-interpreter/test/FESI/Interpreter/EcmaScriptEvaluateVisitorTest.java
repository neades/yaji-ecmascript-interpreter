package FESI.Interpreter;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FESI.AST.ASTFunctionExpression;
import FESI.AST.ASTObjectLiteral;
import FESI.AST.ASTStatement;
import FESI.AST.ASTVariableDeclaration;
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESValue;
import FESI.Parser.EcmaScript;


public class EcmaScriptEvaluateVisitorTest {

    private PrintStream out;

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

}
