package FESI.Data;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import org.junit.Test;

import FESI.AST.ASTProgram;
import FESI.Interpreter.EcmaScriptFunctionVisitor;
import FESI.Interpreter.Evaluator;
import FESI.Parser.EcmaScript;


public class FunctionConstructorTest {

    @Test public void shouldImplementCallOnFunctionDeclarations() throws Exception {
        Evaluator evaluator = new Evaluator();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));

        EcmaScript es = new EcmaScript(new StringReader("function blah() { return 'called'; }"));
        EcmaScriptFunctionVisitor functionVisitor = new EcmaScriptFunctionVisitor(evaluator);
        functionVisitor.visit((ASTProgram)es.Program(), null);
        
        ESObject function = (ESObject) evaluator.getGlobalObject().getProperty("blah","blah".hashCode());
        ESValue value = function.doIndirectCall(evaluator, function, "call", new ESValue[] { evaluator.getGlobalObject() });

        assertEquals("called",value.toString());
    }
}
