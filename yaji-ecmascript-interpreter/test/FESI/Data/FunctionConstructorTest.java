package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

import FESI.AST.ASTProgram;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.EcmaScriptFunctionVisitor;
import FESI.Parser.EcmaScript;


public class FunctionConstructorTest extends EvaluatorTestCase {

    private ESObject function;
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        EcmaScript es = new EcmaScript(new StringReader("function blah() { var result = 'Called:';for( var i=0; i<arguments.length; i++ ) { result += arguments[i]; } return result; }"));
        EcmaScriptFunctionVisitor functionVisitor = new EcmaScriptFunctionVisitor(evaluator);
        functionVisitor.visit((ASTProgram)es.Program(), null);

        function = (ESObject) evaluator.getGlobalObject().getProperty("blah","blah".hashCode());
    }
    @Test public void shouldImplementCallOnFunctionDeclarations() throws Exception {
        ESValue value = function.doIndirectCall(evaluator, function, "call", new ESValue[] { evaluator.getGlobalObject() });

        assertEquals("Called:",value.toString());
    }

    @Test public void shouldImplementApplyOnFunctionDeclarations() throws Exception {
        ESObject array = evaluator.createArray();
        array.doIndirectCall(evaluator,array,"push",new ESValue[] { new ESString("param1"), new ESString("param2") });
        ESValue value = function.doIndirectCall(evaluator, function, "apply", new ESValue[] { evaluator.getGlobalObject(), array });

        assertEquals("Called:param1param2",value.toString());
    }
    
    @Test public void shouldImplementApplyOnFunctionDeclarations_UndefinedArgs() throws Exception {
        ESValue value = function.doIndirectCall(evaluator, function, "apply", new ESValue[] { evaluator.getGlobalObject(), ESUndefined.theUndefined });

        assertEquals("Called:",value.toString());
    }
    
    @Test public void shouldImplementApplyOnFunctionDeclarations_NoArgs() throws Exception {
        ESValue value = function.doIndirectCall(evaluator, function, "apply", new ESValue[] { evaluator.getGlobalObject() });

        assertEquals("Called:",value.toString());
    }
    
    @Test public void shouldImplementApplyOnFunctionDeclarations_ArgsNotArray() throws Exception {
        ObjectPrototype object = ObjectObject.createObject(evaluator);
        object.putProperty(StandardProperty.LENGTHstring, ESNumber.valueOf(2), StandardProperty.LENGTHhash);
        object.putProperty("0", new ESString("op1:"), "0".hashCode());
        object.putProperty("1", new ESString("op2:"), "1".hashCode());
        ESValue value = function.doIndirectCall(evaluator, function, "apply", new ESValue[] { evaluator.getGlobalObject(), object });

        assertEquals("Called:op1:op2:",value.toString());
    }
    
    @Test public void shouldImplementApplyOnFunctionDeclarations_InvalidArgs() throws Exception {
        try {
            function.doIndirectCall(evaluator, function, "apply", new ESValue[] { evaluator.getGlobalObject(), new ESString("arguments") });
            fail("Should throw error");
        } catch (TypeError e) {
            // expected result
        }

    }
}
