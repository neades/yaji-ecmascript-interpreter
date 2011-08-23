package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

public class StringPrototypeTest {
    private Evaluator evaluator;

    @Before
    public void setUp() {
        evaluator = new Evaluator();
    }
    @Test
    public void shouldDefinePrototype() throws Exception {
        ESObject stringObject = (ESObject) evaluator.getGlobalObject().getProperty("String","String".hashCode());
        ESValue stringPrototype = stringObject.getProperty("prototype","prototype".hashCode());
        assertTrue(stringPrototype instanceof ESObject);
    }
    
    @Test
    public void shouldDefineConstructorOnPrototype() throws Exception {
        ESObject stringObject = (ESObject) evaluator.getGlobalObject().getProperty("String","String".hashCode());
        ESObject stringPrototype = (ESObject) stringObject.getProperty("prototype","prototype".hashCode());
        assertSame(stringObject,stringPrototype.getProperty("constructor","constructor".hashCode()));
    }
    
    @Test
    public void shouldConcat2Strings() throws Exception {
        ESObject originalObject = new ESString("start").toESObject(evaluator);
        ESValue result = originalObject.doIndirectCall(evaluator, originalObject, "concat", new ESValue[] { new ESString("end") });
        assertEquals(new ESString("startend"),result);
    }
    
    @Test
    public void charAtWorksWithString() throws Exception {
        ESObject originalObject = new ESString("start").toESObject(evaluator);
        ESValue result = originalObject.doIndirectCall(evaluator, originalObject, "charAt", new ESValue[] { ESNumber.valueOf(1) });
        assertEquals(new ESString("t"),result);
    }
    
    @Test
    public void charAtWorksPassedString() throws Exception {
        ESObject originalObject = new ESString("start").toESObject(evaluator);
        ESValue result = originalObject.doIndirectCall(evaluator, originalObject, "charAt", new ESValue[] { ESNumber.valueOf(10) });
        assertEquals(new ESString(""),result);
    }
    
    @Test
    public void charAtWorksPassedNumber() throws Exception {
        ESObject originalObject = ESNumber.valueOf(123).toESObject(evaluator);
        ESValue  charAtFunction = getStringPrototype().getProperty("charAt", "charAt".hashCode());
        ESValue result = charAtFunction.callFunction(originalObject, new ESValue[] { ESNumber.valueOf(1) });
        assertEquals(new ESString("2"),result);
    }
    private ESObject getStringPrototype() throws EcmaScriptException {
        ESObject stringObject = (ESObject) evaluator.getGlobalObject().getProperty("String","String".hashCode());
        ESObject stringPrototype = (ESObject) stringObject.getProperty(StandardProperty.PROTOTYPEstring,StandardProperty.PROTOTYPEhash);
        return stringPrototype;
    }
}
