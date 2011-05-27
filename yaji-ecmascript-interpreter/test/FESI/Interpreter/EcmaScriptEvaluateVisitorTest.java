package FESI.Interpreter;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import org.junit.Test;

import FESI.AST.ASTObjectLiteral;
import FESI.AST.ASTStatement;
import FESI.AST.ASTVariableDeclaration;
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Parser.EcmaScript;


public class EcmaScriptEvaluateVisitorTest {

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
}
