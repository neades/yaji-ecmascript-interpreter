package org.yaji.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Enumeration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FESI.Data.ESBoolean;
import FESI.Data.ESNull;
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESUndefined;
import FESI.Data.ESValue;
import FESI.Data.EvaluatorTestCase;
import FESI.Data.GlobalObject;
import FESI.Data.StandardProperty;
import FESI.Exceptions.TypeError;

public class SparseArrayPrototypeTest extends EvaluatorTestCase {

    private boolean originalUseSparseState;

    @Override
    @Before
    public void setUp() throws Exception {
        originalUseSparseState = GlobalObject.useSparse;
        GlobalObject.useSparse = true;
        super.setUp();
    }
    
    @Override
    @After public void tearDown() throws Exception {
        super.tearDown();
        GlobalObject.useSparse = originalUseSparseState;
    }

    @Test
    public void prototypeConstructorIsConstructor() throws Exception {
        ESObject array = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        ESValue constructor = array.getProperty(StandardProperty.CONSTRUCTORstring, StandardProperty.CONSTRUCTORhash);
        assertSame(constructor,arrayObject);
    }
    
    @Test
    public void getOwnPropertyNamesIncludesIndices() throws Exception {
        ESObject array = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        array.putProperty("10", ESNumber.valueOf(123), "10".hashCode());
        Enumeration<String> ownPropertyNames = array.getOwnPropertyNames();
        StringBuilder sb = new StringBuilder();
        String sep = "";
        while (ownPropertyNames.hasMoreElements()) {
            String string = ownPropertyNames.nextElement();
            sb.append(sep).append(string);
            sep = ",";
        }
        assertEquals("10,length",sb.toString());
    }
    
    @Test
    public void prototypeSetsLength() throws Exception {
        ESObject array = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        array.putProperty("10", ESNumber.valueOf(123), "10".hashCode());
        assertEquals(ESNumber.valueOf(11),array.getProperty(StandardProperty.LENGTHstring, StandardProperty.LENGTHhash));
    }
    
    @Test
    public void joinWithNoSeparatorSpecifiedUsesComma() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), s("two") });
        ESValue value = array.doIndirectCall(evaluator, array, "join", ESValue.EMPTY_ARRAY);
        assertEquals(s("1,two"),value);
    }

    @Test
    public void joinOfObjectWithNoSeparatorSpecifiedUsesComma() throws Exception {
        ESObject arrayPrototype = (ESObject)arrayObject.getProperty(StandardProperty.PROTOTYPEstring,StandardProperty.PROTOTYPEhash);
        ESValue joinFunction = arrayPrototype.getProperty("join", "join".hashCode());
        ESObject object = objectObject.doConstruct(ESValue.EMPTY_ARRAY);
        object.putProperty("0",n(1),"0".hashCode());
        object.putProperty("1",s("two"),"1".hashCode());
        object.putProperty("length",n(2),"length".hashCode());
        ESValue value = joinFunction.callFunction(object, ESValue.EMPTY_ARRAY);
        assertEquals(s("1,two"),value);
    }

    @Test
    public void joinWithSeparatorUsesValueSpecified() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), s("two") });
        ESValue value = array.doIndirectCall(evaluator, array, "join", new ESValue[] { s("-|-") });
        assertEquals(s("1-|-two"),value);
    }

    @Test
    public void joinOfEmptyArrayReturnsEmptyString() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[0]);
        ESValue value = array.doIndirectCall(evaluator, array, "join", new ESValue[] { s("-|-") });
        assertEquals(s(""),value);
    }

    @Test
    public void joinOfArrayContainingNullReturnsEmptyString() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { ESNull.theNull });
        ESValue value = array.doIndirectCall(evaluator, array, "join", new ESValue[] { s(",") });
        assertEquals(s(""),value);
    }

    @Test
    public void joinOfArrayContainingUndefinedReturnsEmptyString() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(22),ESNull.theNull,n(33) });
        ESValue value = array.doIndirectCall(evaluator, array, "join", new ESValue[] { s(",") });
        assertEquals(s("22,,33"),value);
    }

    @Test
    public void toStringOnArraySameAsJoin() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), s("two") });
        ESValue value = array.doIndirectCall(evaluator, array, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(s("1,two"),value);
    }
    
    @Test
    public void toStringOnArrayUsesObjectToStringIfJoinBroken() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), s("two") });
        array.putProperty("join",ESUndefined.theUndefined, "join".hashCode());
        ESValue value = array.doIndirectCall(evaluator, array, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(s("[object Array]"),value);
    }

    @Test
    public void concatCreatesANewArray() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), s("two") });
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "concat", new ESValue[] { n(.25), s("four") });
        ESValue string = value.doIndirectCall(evaluator, value, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(s("1,two,0.25,four"),string);
    }
    
    @Test
    public void concatSkipsUndefined() throws Exception {
        ESObject array = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        array.putProperty(1L, n(1));
        ESObject paramArray = arrayObject.doConstruct(new ESValue[] { n(.25), s("four") });
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "concat", new ESValue[] { paramArray });
        ESValue string = value.doIndirectCall(evaluator, value, "toString", ESValue.EMPTY_ARRAY);
        assertEquals(s(",1,0.25,four"),string);
    }
    
    @Test
    public void popReturnsLastElement() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), s("two") });
        ESValue value = array.doIndirectCall(evaluator, array, "pop", ESValue.EMPTY_ARRAY);
        assertEquals(s("two"),value);
    }
    
    @Test
    public void popSetsLengthToOneLess() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), s("two") });
        array.doIndirectCall(evaluator, array, "pop", ESValue.EMPTY_ARRAY);
        ESValue length = array.getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash);
        assertEquals(n(1L),length);
    }
    
    @Test
    public void popDeletesEntryRemoved() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), s("two") });
        array.doIndirectCall(evaluator, array, "pop", ESValue.EMPTY_ARRAY);
        ESValue previousProperty = array.getProperty(1L);
        assertEquals(ESUndefined.theUndefined,previousProperty);
    }
    
    @Test
    public void popFromEmptyArrayReturnsUndefined() throws Exception {
        ESObject array = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        ESValue value = array.doIndirectCall(evaluator, array, "pop", ESValue.EMPTY_ARRAY);
        assertEquals(ESUndefined.theUndefined,value);
    }
    
    @Test
    public void popFromEmptyArrayLeavesLengthAsZero() throws Exception {
        ESObject array = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        array.doIndirectCall(evaluator, array, "pop", ESValue.EMPTY_ARRAY);
        ESValue length = array.getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash);
        assertEquals(n(0L),length);
    }
    
    @Test
    public void pushValue() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), s("two") });
        ESValue value = array.doIndirectCall(evaluator, array, "push", new ESValue[] { n(3) });
        assertEquals(s("1,two,3"),array);
        assertEquals(n(3),value);
    }
    
    @Test
    public void reverseBasicOperation() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), s("two") });
        array.putProperty(3L, n(4));
        ESValue value = array.doIndirectCall(evaluator, array, "reverse", ESValue.EMPTY_ARRAY);
        assertEquals(s("4,,two,1"),array);
        assertSame(array,value);
    }
    
    @Test
    public void shiftBasicOperation() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), s("two") });
        ESValue value = array.doIndirectCall(evaluator, array, "shift", ESValue.EMPTY_ARRAY);
        assertEquals(s("two"),array);
        assertEquals(n(1L),value);
    }
    
    @Test
    public void shiftShiftsSpaces() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), s("two") });
        array.putProperty(3L, n(4));
        array.doIndirectCall(evaluator, array, "shift", ESValue.EMPTY_ARRAY);
        assertEquals(s("two,,4"),array);
    }
    
    @Test
    public void shiftDeletesLastElement() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), s("two") });
        array.putProperty(3L, n(4));
        array.doIndirectCall(evaluator, array, "shift", ESValue.EMPTY_ARRAY);
        assertEquals(ESUndefined.theUndefined,array.getProperty(3L));
    }
    
    @Test
    public void shiftEmptyArrayReturnsUndefined() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), s("two") });
        array.putProperty(StandardProperty.LENGTHstring, ESNull.theNull);
        ESValue value = array.doIndirectCall(evaluator, array, "shift", ESValue.EMPTY_ARRAY);
        assertEquals(ESUndefined.theUndefined,value);
        assertEquals(n(0L),array.getProperty(StandardProperty.LENGTHstring, StandardProperty.LENGTHhash));
    }
    
    @Test
    public void sliceCreatesNewArray() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), n(2), n(3), n(4), n(5) });
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "slice", new ESValue[] { n(1), n(3) });
        assertEquals(s("2,3"),value);
        assertEquals(s("1,2,3,4,5"),array);
    }

    @Test
    public void sliceWithNegativeStart() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), n(2), n(3), n(4), n(5) });
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "slice", new ESValue[] { n(-4.5), n(3) });
        assertEquals(s("2,3"),value);
    }

    @Test
    public void sliceWithUndefinedEnd() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), n(2), n(3), n(4), n(5) });
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "slice", new ESValue[] { n(-4.5) });
        assertEquals(s("2,3,4,5"),value);
    }

    @Test
    public void sliceWithUndefinedStartNegativeEnd() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), n(2), n(3), n(4), n(5) });
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "slice", new ESValue[] { ESUndefined.theUndefined, n(-2) });
        assertEquals(s("1,2,3"),value);
    }

    @Test
    public void sliceWithSparseValuesOmitsFromLength() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), n(2), n(3), n(4), n(5) });
        array.putProperty(10L, n(10));
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "slice", new ESValue[] { ESUndefined.theUndefined, n(-1) });
        assertEquals(s("1,2,3,4,5"),value);
    }
    
    @Test
    public void sortSortaWorks() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(5), n(2), n(3), n(1), n(4) });
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "sort", ESValue.EMPTY_ARRAY);
        assertEquals(s("1,2,3,4,5"),value);
    }
    
    @Test
    public void sortUsesComparatorFunction() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("x"),s("y"),s("if (x==3) x=100; if (y===3) y=100; return x>y;") });
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(5), n(2), n(3), n(1), n(4) });
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "sort", new ESValue[] { function } );
        assertEquals(s("1,2,4,5,3"),value);
    }    
    
    @Test
    public void sortSortsUndefinedToEnd() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("x"),s("y"),s("return x>y;") });
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(5), n(2), ESUndefined.theUndefined, n(3), n(1), n(4) });
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "sort", new ESValue[] { function } );
        assertEquals(s("1,2,3,4,5,"),value);
    }    
    
    @Test
    public void sortSortsUnsetAfterUndefined() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("x"),s("y"),s("return x>y;") });
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(5), n(2), ESUndefined.theUndefined, n(3), n(1), n(4) });
        array.deleteProperty(4);
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "sort", new ESValue[] { function } );
        assertEquals(s("2,3,4,5,,"),value);
        assertNull(array.getPropertyIfAvailable(5L));
    }    

    @Test
    public void spliceDeletes() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c") });
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "splice", new ESValue[] { n(1), n(2) } );
        assertEquals(s("b,c"),value);
        assertEquals(s("a"),array);
    }    

    @Test
    public void spliceDeletesLimitedByLength() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c") });
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "splice", new ESValue[] { n(1), n(3) } );
        assertEquals(s("b,c"),value);
        assertEquals(s("a"),array);
    }    

    @Test
    public void spliceDeletesLeavingValuesAfter() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c") });
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "splice", new ESValue[] { n(1), n(1) } );
        assertEquals(s("b"),value);
        assertEquals(s("a,c"),array);
    }    

    @Test
    public void spliceInsertsLessThanDeleted() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c") });
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "splice", new ESValue[] { n(1), n(2), s("d") } );
        assertEquals(s("b,c"),value);
        assertEquals(s("a,d"),array);
    }    

    @Test
    public void spliceInsertsGreaterThanDeleted() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c") });
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "splice", new ESValue[] { n(1), n(1), s("d"), s("e"), s("f") } );
        assertEquals(s("b"),value);
        assertEquals(s("a,d,e,f,c"),array);
    }    

    @Test
    public void spliceInsertsEqualDeleted() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c") });
        ESObject value = (ESObject) array.doIndirectCall(evaluator, array, "splice", new ESValue[] { n(1), n(2), s("d"), s("e") } );
        assertEquals(s("b,c"),value);
        assertEquals(s("a,d,e"),array);
    }
    
    @Test
    public void unshiftInsertsAtStart() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c") });
        ESValue value = array.doIndirectCall(evaluator, array, "unshift", new ESValue[] { s("d"), s("e") } );
        assertEquals(n(5),value);
        assertEquals(s("d,e,a,b,c"),array);
    }
    
    @Test
    public void indexOfReturnsIndexFound() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c") });
        ESValue value = array.doIndirectCall(evaluator, array, "indexOf", new ESValue[] { s("b") } );
        assertEquals(n(1),value);
    }

    @Test
    public void indexOfReturnsNegativeOneIfNotFound() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c") });
        ESValue value = array.doIndirectCall(evaluator, array, "indexOf", new ESValue[] { s("d") } );
        assertEquals(n(-1),value);
    }

    @Test
    public void indexOfMatchesType() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("0"), s("c") });
        ESValue value = array.doIndirectCall(evaluator, array, "indexOf", new ESValue[] { n(0) } );
        assertEquals(n(-1),value);
    }

    @Test
    public void indexOfStartsFromOffset() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "indexOf", new ESValue[] { s("b"), n(2) } );
        assertEquals(n(4),value);
    }

    @Test
    public void indexOfStartsFromOffsetFromEnd() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "indexOf", new ESValue[] { s("b"), n(-2) } );
        assertEquals(n(4),value);
    }


    @Test
    public void indexOfStartsFromOffsetFromEndIfTooBig() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "indexOf", new ESValue[] { s("b"), n(-10) } );
        assertEquals(n(1),value);
    }


    @Test
    public void indexOfNegaiveIfPassedEnd() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "indexOf", new ESValue[] { s("b"), n(10) } );
        assertEquals(n(-1),value);
    }

    @Test
    public void lastIndexOfReturnsIndexFound() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "lastIndexOf", new ESValue[] { s("b") } );
        assertEquals(n(3),value);
    }

    @Test
    public void lastIndexOfReturnsNegativeOneIfNotFound() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c") });
        ESValue value = array.doIndirectCall(evaluator, array, "lastIndexOf", new ESValue[] { s("d") } );
        assertEquals(n(-1),value);
    }

    @Test
    public void lastIndexOfMatchesType() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("0"), s("c") });
        ESValue value = array.doIndirectCall(evaluator, array, "lastIndexOf", new ESValue[] { n(0) } );
        assertEquals(n(-1),value);
    }

    @Test
    public void lastIndexOfStartsFromOffset() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "lastIndexOf", new ESValue[] { s("b"), n(2) } );
        assertEquals(n(1),value);
    }

    @Test
    public void lastIndexOfStartsFromOffsetFromEnd() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "lastIndexOf", new ESValue[] { s("b"), n(-2) } );
        assertEquals(n(1),value);
    }


    @Test
    public void lastIndexOfStartsFromOffsetFromEndIfTooBig() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "lastIndexOf", new ESValue[] { s("b"), n(-10) } );
        assertEquals(n(-1),value);
    }


    @Test
    public void lastIndexOfNegaiveIfPassedEnd() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "lastIndexOf", new ESValue[] { s("b"), n(10) } );
        assertEquals(n(4),value);
    }
    
    @Test
    public void everyCallsFunction() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("v"),s("k"),s("o"),s("thisPassed = this; valuePassed = v; keyPassed = k; objectPassed = o; return false;") });
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "every", new ESValue[] { function } );
        
        assertEquals(ESBoolean.FALSE,value);
        assertEquals(s("a"), globalObject.getProperty("valuePassed", "valuePassed".hashCode()));
    }

    @Test
    public void everyPassesThisAsUndefined() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("v"),s("k"),s("o"),s("'use strict'; thisPassed = this; valuePassed = v; keyPassed = k; objectPassed = o; return false;") });
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        array.doIndirectCall(evaluator, array, "every", new ESValue[] { function } );
        
        assertEquals(ESUndefined.theUndefined, globalObject.getProperty("thisPassed", "thisPassed".hashCode()));
    }

    @Test
    public void everyPassesThisAsParamterPassed() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("v"),s("k"),s("o"),s("thisPassed = this; valuePassed = v; keyPassed = k; objectPassed = o; return false;") });
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        array.doIndirectCall(evaluator, array, "every", new ESValue[] { function, globalObject } );
        
        assertSame(globalObject, globalObject.getProperty("thisPassed", "thisPassed".hashCode()));
    }

    @Test
    public void everyStepsThroughEveryItem() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("v"),s("k"),s("o"),s("valuesPassed.push(k+'='+v);return true;") });
        ESObject valuesPassed = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        globalObject.putProperty("valuesPassed", valuesPassed);
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "every", new ESValue[] { function } );
        
        assertEquals(ESBoolean.TRUE,value);
        assertEquals("0=a,1=b,2=c,3=a,4=b",valuesPassed.toString());
    }

    @Test
    public void everySkipsSparseValues() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("v"),s("k"),s("o"),s("valuesPassed.push(k+'='+v);return true;") });
        ESObject valuesPassed = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        globalObject.putProperty("valuesPassed", valuesPassed);
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b") });
        array.putProperty(3L, s("c"));
        ESValue value = array.doIndirectCall(evaluator, array, "every", new ESValue[] { function } );
        
        assertEquals(ESBoolean.TRUE,value);
        assertEquals("0=a,1=b,3=c",valuesPassed.toString());
    }

    @Test
    public void everyForEmptyArrayReturnsTrue() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("v"),s("k"),s("o"),s("valuesPassed.push(k+'='+v);return true;") });
        ESObject valuesPassed = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        globalObject.putProperty("valuesPassed", valuesPassed);
        ESObject array = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        ESValue value = array.doIndirectCall(evaluator, array, "every", new ESValue[] { function } );
        
        assertEquals(ESBoolean.TRUE,value);
        assertEquals("",valuesPassed.toString());
    }

    @Test
    public void someCallsFunction() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("v"),s("k"),s("o"),s("thisPassed = this; valuePassed = v; keyPassed = k; objectPassed = o; return true;") });
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "some", new ESValue[] { function } );
        
        assertEquals(ESBoolean.TRUE,value);
        assertEquals(s("a"), globalObject.getProperty("valuePassed", "valuePassed".hashCode()));
    }

    @Test
    public void someStepsThroughEveryItem() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("v"),s("k"),s("o"),s("valuesPassed.push(k+'='+v);return false;") });
        ESObject valuesPassed = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        globalObject.putProperty("valuesPassed", valuesPassed);
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "some", new ESValue[] { function } );
        
        assertEquals(ESBoolean.FALSE,value);
        assertEquals("0=a,1=b,2=c,3=a,4=b",valuesPassed.toString());
    }

    @Test
    public void someSkipsSparseValues() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("v"),s("k"),s("o"),s("valuesPassed.push(k+'='+v);return false;") });
        ESObject valuesPassed = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        globalObject.putProperty("valuesPassed", valuesPassed);
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b") });
        array.putProperty(3L, s("c"));
        ESValue value = array.doIndirectCall(evaluator, array, "some", new ESValue[] { function } );
        
        assertEquals(ESBoolean.FALSE,value);
        assertEquals("0=a,1=b,3=c",valuesPassed.toString());
    }

    @Test
    public void someForEmptyArrayReturnsTrue() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("v"),s("k"),s("o"),s("valuesPassed.push(k+'='+v);return false;") });
        ESObject valuesPassed = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        globalObject.putProperty("valuesPassed", valuesPassed);
        ESObject array = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        ESValue value = array.doIndirectCall(evaluator, array, "some", new ESValue[] { function } );
        
        assertEquals(ESBoolean.FALSE,value);
        assertEquals("",valuesPassed.toString());
    }

    @Test
    public void forEachStepsThroughEveryItem() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("v"),s("k"),s("o"),s("valuesPassed.push(k+'='+v);return false;") });
        ESObject valuesPassed = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        globalObject.putProperty("valuesPassed", valuesPassed);
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "forEach", new ESValue[] { function } );
        
        assertEquals(ESUndefined.theUndefined,value);
        assertEquals("0=a,1=b,2=c,3=a,4=b",valuesPassed.toString());
    }

    @Test
    public void forEachSkipsSparseValues() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("v"),s("k"),s("o"),s("valuesPassed.push(k+'='+v);return true;") });
        ESObject valuesPassed = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        globalObject.putProperty("valuesPassed", valuesPassed);
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b") });
        array.putProperty(3L, s("c"));
        ESValue value = array.doIndirectCall(evaluator, array, "forEach", new ESValue[] { function } );
        
        assertEquals(ESUndefined.theUndefined,value);
        assertEquals("0=a,1=b,3=c",valuesPassed.toString());
    }

    @Test
    public void mapStepsThroughEveryItem() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("v"),s("k"),s("o"),s("return k+'='+v;") });
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "map", new ESValue[] { function } );
        
        assertEquals("0=a,1=b,2=c,3=a,4=b",value.toString());
    }

    @Test
    public void mapSkipsSparseValues() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("v"),s("k"),s("o"),s("return k+'='+v;") });
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b") });
        array.putProperty(3L, s("c"));
        ESValue value = array.doIndirectCall(evaluator, array, "map", new ESValue[] { function } );
        
        assertEquals("0=a,1=b,,3=c",value.toString());
    }

    @Test
    public void filterStepsThroughEveryItem() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("v"),s("k"),s("o"),s("return (k % 2) == 0") });
        ESObject array = arrayObject.doConstruct(new ESValue[] { s("a"), s("b"), s("c"), s("a"), s("b") });
        ESValue value = array.doIndirectCall(evaluator, array, "filter", new ESValue[] { function } );
        
        assertEquals("a,c,b",value.toString());
    }
    
    @Test
    public void reduceStepsThroughEveryItem() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("a"), s("v"),s("k"),s("o"),s("return a + v;") });
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), n(2), n(3), n(4), n(5) });
        ESValue value = array.doIndirectCall(evaluator, array, "reduce", new ESValue[] { function, n(100) } );
        
        assertEquals(ESNumber.valueOf(115),value);
    }
    
    @Test
    public void reduceUsesFirstElementAsInitialValue() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("a"), s("v"),s("k"),s("o"),s("return a + v;") });
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), n(2), n(3), n(4), n(5) });
        ESValue value = array.doIndirectCall(evaluator, array, "reduce", new ESValue[] { function } );
        
        assertEquals(ESNumber.valueOf(15),value);
    }
    
    @Test(expected=TypeError.class)
    public void reduceThrowsExceptionOnEmptyArrayWithNoInitialValue() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("a"), s("v"),s("k"),s("o"),s("return a + v;") });
        ESObject array = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        array.doIndirectCall(evaluator, array, "reduce", new ESValue[] { function } );
    }
    
    @Test(expected=TypeError.class)
    public void reduceThrowsExceptionOnEmptySparseArrayWithNoInitialValue() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("a"), s("v"),s("k"),s("o"),s("return a + v;") });
        ESObject array = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        array.putProperty("length", n(10));
        array.doIndirectCall(evaluator, array, "reduce", new ESValue[] { function } );
    }
    
    @Test
    public void reduceRightStepsThroughEveryItem() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("a"), s("v"),s("k"),s("o"),s("return ''+a+v;") });
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), n(2), n(3), n(4), n(5) });
        ESValue value = array.doIndirectCall(evaluator, array, "reduceRight", new ESValue[] { function, n(100) } );
        
        assertEquals(new ESString("10054321"),value);
    }
    
    @Test
    public void reduceRightUsesLastElementAsInitialValue() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("a"), s("v"),s("k"),s("o"),s("return '' + a + v;") });
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(1), n(2), n(3), n(4), n(5) });
        ESValue value = array.doIndirectCall(evaluator, array, "reduceRight", new ESValue[] { function } );
        
        assertEquals(new ESString("54321"),value);
    }
    
    @Test(expected=TypeError.class)
    public void reduceRightThrowsExceptionOnEmptyArrayWithNoInitialValue() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("a"), s("v"),s("k"),s("o"),s("return '' + a + v;") });
        ESObject array = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        array.doIndirectCall(evaluator, array, "reduceRight", new ESValue[] { function } );
    }
    
    @Test(expected=TypeError.class)
    public void reduceRightThrowsExceptionOnEmptySparseArrayWithNoInitialValue() throws Exception {
        ESValue functionConstructor = globalObject.getProperty("Function", "Function".hashCode());
        ESObject function = functionConstructor.doConstruct(new ESValue[] { s("a"), s("v"),s("k"),s("o"),s("return '' + a + v;") });
        ESObject array = arrayObject.doConstruct(ESValue.EMPTY_ARRAY);
        array.putProperty("length", n(10));
        array.doIndirectCall(evaluator, array, "reduceRight", new ESValue[] { function } );
    }
    
    @Test
    public void reducingLengthDeletesProperties() throws Exception {
        ESObject array = arrayObject.doConstruct(new ESValue[] { n(0), n(1), n(2), n(3), n(4) });
        array.putProperty(StandardProperty.LENGTHstring, n(2), StandardProperty.LENGTHhash);
        assertEquals(ESUndefined.theUndefined, array.getProperty(2L));
    }
    
    private static ESNumber n(double value) {
        return ESNumber.valueOf(value);
    }
    
    private static ESString s(String s) {
        return new ESString(s);
    }
}
