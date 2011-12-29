package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.TypeError;

public class BoundFunctionPrototypeTest extends EvaluatorTestCase {

    private ESObject functionObject;
    private ESObject aFunction;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        functionObject = (ESObject) globalObject.getProperty("Function");
        aFunction = functionObject.doConstruct(new ESValue[] { s("a"), s("b"), s("return [this,a,b];") });
    }

    private ESString s(String value) {
        return new ESString(value);
    }

    @Test
    public void boundFunctionSHouldHaveFunctionPrototype() throws Exception {
        ESObject prototype = (ESObject) functionObject.getProperty(StandardProperty.PROTOTYPEstring, StandardProperty.PROTOTYPEhash);
        ESObject value = (ESObject) prototype.doIndirectCall(evaluator, prototype, "bind", ESValue.EMPTY_ARRAY);
        assertSame(evaluator.getFunctionPrototype(),value.getPrototype());
    }

    @Test
    public void bindShouldCreateNewFunction() throws Exception {
        ESObject prototype = (ESObject) functionObject.getProperty(StandardProperty.PROTOTYPEstring, StandardProperty.PROTOTYPEhash);
        ESObject value = (ESObject) prototype.doIndirectCall(evaluator, prototype, "bind", ESValue.EMPTY_ARRAY);
        assertEquals("Function",value.getESClassName());
    }

    @Test(expected=TypeError.class)
    public void bindShouldThrowTypeErrorIfThisIsNotCallable() throws Exception {
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        ESObject prototype = (ESObject) functionObject.getProperty(StandardProperty.PROTOTYPEstring, StandardProperty.PROTOTYPEhash);
        prototype.doIndirectCall(evaluator, object, "bind", ESValue.EMPTY_ARRAY);
    }

    @Test
    public void boundFunctionShouldHaveLengthOfFunction() throws Exception {
        ESObject value = (ESObject) aFunction.doIndirectCall(evaluator, aFunction, "bind", ESValue.EMPTY_ARRAY);
        assertEquals(ESNumber.valueOf(2),value.getProperty(StandardProperty.LENGTHstring));
    }

    @Test
    public void boundFunctionShouldHaveLengthOfFunctionLessAdditionArguments() throws Exception {
        ESObject value = (ESObject) aFunction.doIndirectCall(evaluator, aFunction, "bind", new ESValue[] { ESUndefined.theUndefined, ESNumber.valueOf(1) });
        assertEquals(ESNumber.valueOf(1),value.getProperty(StandardProperty.LENGTHstring));
    }

    @Test
    public void boundFunctionShouldBeExtensible() throws Exception {
        ESObject value = (ESObject) aFunction.doIndirectCall(evaluator, aFunction, "bind", new ESValue[] { ESUndefined.theUndefined, ESNumber.valueOf(1) });
        assertTrue(value.isExtensible());
    }

    @Test(expected=TypeError.class)
    public void shouldntBeAbleToSetCallerOnBoundFunction() throws Exception {
        ESObject value = (ESObject) aFunction.doIndirectCall(evaluator, aFunction, "bind", new ESValue[] { ESUndefined.theUndefined, ESNumber.valueOf(1) });
        value.putProperty(StandardProperty.CALLERstring, ESNull.theNull);
    }

    @Test(expected=TypeError.class)
    public void shouldntBeAbleToSetArgumentsOnBoundFunction() throws Exception {
        ESObject value = (ESObject) aFunction.doIndirectCall(evaluator, aFunction, "bind", new ESValue[] { ESUndefined.theUndefined, ESNumber.valueOf(1) });
        value.putProperty(StandardProperty.ARGUMENTSstring, ESNull.theNull);
    }

    @Test(expected=TypeError.class)
    public void shouldntBeAbleToGetCallerOnBoundFunction() throws Exception {
        ESObject value = (ESObject) aFunction.doIndirectCall(evaluator, aFunction, "bind", new ESValue[] { ESUndefined.theUndefined, ESNumber.valueOf(1) });
        value.getProperty(StandardProperty.CALLERstring);
    }

    @Test(expected=TypeError.class)
    public void shouldntBeAbleToGetArgumentsOnBoundFunction() throws Exception {
        ESObject value = (ESObject) aFunction.doIndirectCall(evaluator, aFunction, "bind", new ESValue[] { ESUndefined.theUndefined, ESNumber.valueOf(1) });
        value.getProperty(StandardProperty.ARGUMENTSstring);
    }
    
    @Test
    public void callPassesBoundValues() throws Exception {
        ESObject value = (ESObject) aFunction.doIndirectCall(evaluator, aFunction, "bind", new ESValue[] { ESNumber.valueOf(3), ESNumber.valueOf(1) });
        ESObject result = (ESObject) value.callFunction(ESUndefined.theUndefined, new ESValue[] { ESNumber.valueOf(2) });
        assertEquals("3,1,2",result.toString());
    }
    
    @Test
    public void doConstructPassesBoundValues() throws Exception {
        ESObject prototype = (ESObject) functionObject.getProperty(StandardProperty.PROTOTYPEstring, StandardProperty.PROTOTYPEhash);
        ESObject value = (ESObject) prototype.doIndirectCall(evaluator, arrayObject, "bind", new ESValue[] { ESNumber.valueOf(3), ESNumber.valueOf(1) });
        ESObject result = value.doConstruct(new ESValue[] { ESNumber.valueOf(2) });
        assertEquals("1,2",result.toString());
    }
    
    @Test
    public void hasPropertyShouldReturnTrueForCaller() throws Exception {
        ESObject boundFunction = (ESObject) aFunction.doIndirectCall(evaluator, aFunction, "bind", new ESValue[] { ESUndefined.theUndefined, ESNumber.valueOf(1) });
        ESValue value = boundFunction.doIndirectCall(evaluator, boundFunction, "hasOwnProperty", new ESValue[] { new ESString(StandardProperty.CALLERstring) });
        assertEquals(ESBoolean.TRUE,value);
    }

    @Test
    public void hasPropertyShouldReturnTrueForArguments() throws Exception {
        ESObject boundFunction = (ESObject) aFunction.doIndirectCall(evaluator, aFunction, "bind", new ESValue[] { ESUndefined.theUndefined, ESNumber.valueOf(1) });
        ESValue value = boundFunction.doIndirectCall(evaluator, boundFunction, "hasOwnProperty", new ESValue[] { new ESString(StandardProperty.ARGUMENTSstring) });
        assertEquals(ESBoolean.TRUE,value);
    }
    
    @Test
    public void hasPropertyShouldReturnTrueForLength() throws Exception {
        ESObject boundFunction = (ESObject) aFunction.doIndirectCall(evaluator, aFunction, "bind", new ESValue[] { ESUndefined.theUndefined, ESNumber.valueOf(1) });
        ESValue value = boundFunction.doIndirectCall(evaluator, boundFunction, "hasOwnProperty", new ESValue[] { new ESString(StandardProperty.LENGTHstring) });
        assertEquals(ESBoolean.TRUE,value);
    }
    
    @Test
    public void shouldntBeAbleToSetLength() throws Exception {
        ESObject boundFunction = (ESObject) aFunction.doIndirectCall(evaluator, aFunction, "bind", new ESValue[] { ESUndefined.theUndefined, ESNumber.valueOf(1) });
        ESValue originalLength = boundFunction.getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash);
        boundFunction.putProperty(StandardProperty.LENGTHstring, ESNumber.valueOf(100), StandardProperty.LENGTHhash);
        assertEquals(originalLength,boundFunction.getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash));
    }
}
