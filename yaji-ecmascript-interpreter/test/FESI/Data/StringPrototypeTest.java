package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

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
}
