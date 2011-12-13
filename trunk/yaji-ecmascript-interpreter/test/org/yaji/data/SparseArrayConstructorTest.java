package org.yaji.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FESI.Data.ESBoolean;
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESValue;
import FESI.Data.GlobalObject;
import FESI.Data.ObjectObject;
import FESI.Data.ObjectPrototype;
import FESI.Data.StandardProperty;
import FESI.Exceptions.RangeError;
import FESI.Interpreter.Evaluator;

public class SparseArrayConstructorTest {

    private boolean originalUseSparseState;
    private Evaluator evaluator;
    private ESObject arrayConstructor;
    private ESObject objectConstructor;
    private GlobalObject globalObject;

    @Before
    public void setUp() throws Exception {
        originalUseSparseState = GlobalObject.useSparse;
        GlobalObject.useSparse = true;
        evaluator = new Evaluator();
        globalObject = evaluator.getGlobalObject();
        arrayConstructor = (ESObject) globalObject.getProperty("Array","Array".hashCode());
        objectConstructor = (ESObject) evaluator.getGlobalObject().getProperty("Object","Object".hashCode());
    }

    @After
    public void tearDown() throws Exception {
        GlobalObject.useSparse = originalUseSparseState;
    }

    @Test
    public void testCallFunctionReturnsArray() throws Exception {
        ESObject value = (ESObject) arrayConstructor.callFunction(globalObject, new ESValue[0]);
        assertEquals("Array",value.getESClassName());
    }
    
    @Test
    public void testNewReturnsArray() throws Exception {
        ESObject value = arrayConstructor.doConstruct(new ESValue[0]);
        assertEquals("Array",value.getESClassName());
    }
    
    @Test
    public void arrayObjectPrototypeHasCorrectPermissions() throws Exception {
        ESObject descriptor = (ESObject) objectConstructor.doIndirectCall(evaluator, objectConstructor, "getOwnPropertyDescriptor", new ESValue[] { arrayConstructor, new ESString("prototype")});
        assertEquals(ESBoolean.FALSE, descriptor.getProperty(StandardProperty.WRITABLEstring,StandardProperty.WRITABLEhash));
        assertEquals(ESBoolean.FALSE, descriptor.getProperty(StandardProperty.ENUMERABLEstring,StandardProperty.ENUMERABLEhash));
        assertEquals(ESBoolean.FALSE, descriptor.getProperty(StandardProperty.CONFIGURABLEstring,StandardProperty.CONFIGURABLEhash));
    }
    
    @Test
    public void arrayPrototypeIsAnArray() throws Exception {
        ESObject value = (ESObject) arrayConstructor.callFunction(evaluator.getGlobalObject(), new ESValue[0]);
        assertEquals("Array",value.getPrototype().getESClassName());
    }
    
    @Test
    public void arrayPrototypeIsArrayObjectPrototype() throws Exception {
        ESValue arrayObjectPrototype = arrayConstructor.getProperty(StandardProperty.PROTOTYPEstring,StandardProperty.PROTOTYPEhash);
        ESObject value = (ESObject) arrayConstructor.callFunction(evaluator.getGlobalObject(), new ESValue[0]);
        assertSame(arrayObjectPrototype,value.getPrototype());
    }
    
    @Test
    public void testCallFunctionReturnsArrayOfZeroLength() throws Exception {
        ESObject value = (ESObject) arrayConstructor.callFunction(evaluator.getGlobalObject(), new ESValue[0]);
        assertEquals(ESNumber.valueOf(0),value.getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash));
    }

    @Test
    public void shouldInitialiseWithArgumentsPassed() throws Exception {
        ESObject value = (ESObject) arrayConstructor.callFunction(evaluator.getGlobalObject(), new ESValue[] { ESNumber.valueOf(123), ESNumber.valueOf(456) });
        assertEquals(ESNumber.valueOf(2),value.getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash));
        assertEquals(ESNumber.valueOf(123), value.getProperty("0","0".hashCode()));
        assertEquals(ESNumber.valueOf(456), value.getProperty("1","1".hashCode()));
    }

    @Test
    public void shouldInitialiseLengthToSingleArgumentPassed() throws Exception {
        ESObject value = (ESObject) arrayConstructor.callFunction(evaluator.getGlobalObject(), new ESValue[] { ESNumber.valueOf(123) });
        assertEquals(ESNumber.valueOf(123),value.getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash));
    }
    
    @Test
    public void shouldInitialiseLengthToMaxUnsignedInteger() throws Exception {
        ESObject value = (ESObject) arrayConstructor.callFunction(evaluator.getGlobalObject(), new ESValue[] { ESNumber.valueOf(0xFFFFFFFFL) });
        assertEquals(ESNumber.valueOf(Math.pow(2, 32)-1),value.getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash));
    }
    
    @Test(expected=RangeError.class)
    public void shouldThrowIfNumberExceedsMaxUnsignedInteger() throws Exception {
        arrayConstructor.callFunction(evaluator.getGlobalObject(), new ESValue[] { ESNumber.valueOf(0x100000000L) });
    }
    
    @Test(expected=RangeError.class)
    public void shouldThrowIfNumberIsNotInteger() throws Exception {
        arrayConstructor.callFunction(evaluator.getGlobalObject(), new ESValue[] { ESNumber.valueOf(123.456) });
    }
    
    @Test
    public void shouldSetArgumentIfNotInteger() throws Exception {
        ESObject value = (ESObject) arrayConstructor.callFunction(evaluator.getGlobalObject(), new ESValue[] { new ESString("123.456") });
        assertEquals(new ESString("123.456"), value.getProperty("0","0".hashCode()));
        assertEquals(ESNumber.valueOf(1), value.getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash));
    }
    
    @Test
    public void shouldProvideIsArrayMethod() throws Exception {
        ESObject value = (ESObject) arrayConstructor.callFunction(evaluator.getGlobalObject(), new ESValue[0]);
        ESValue result = arrayConstructor.doIndirectCall(evaluator, arrayConstructor, "isArray", new ESValue[] { value });
        assertEquals(ESBoolean.TRUE, result);
    }
    
    @Test
    public void isArrayMethodFalseForNonObject() throws Exception {
        ESValue result = arrayConstructor.doIndirectCall(evaluator, arrayConstructor, "isArray", new ESValue[] { new ESString("blah") });
        assertEquals(ESBoolean.FALSE, result);
    }

    @Test
    public void isArrayMethodFalseForObject() throws Exception {
        ObjectPrototype object = ObjectObject.createObject(evaluator);
        ESValue result = arrayConstructor.doIndirectCall(evaluator, arrayConstructor, "isArray", new ESValue[] { object });
        assertEquals(ESBoolean.FALSE, result);
    }
    
    @Test
    public void prototypeLengthIsZero() throws Exception {
        ESObject prototype = (ESObject) arrayConstructor.getProperty(StandardProperty.PROTOTYPEstring, StandardProperty.PROTOTYPEhash);
        ESValue length = prototype.getProperty(StandardProperty.LENGTHstring, StandardProperty.LENGTHhash);
        assertEquals(ESNumber.valueOf(0),length);
    }
    
}
