package org.yaji.data;

import java.util.Collections;
import java.util.Comparator;

import FESI.Data.BuiltinFunctionObject;
import FESI.Data.ESBoolean;
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESUndefined;
import FESI.Data.ESValue;
import FESI.Data.FunctionPrototype;
import FESI.Data.ObjectPrototype;
import FESI.Data.StandardProperty;
import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.RangeError;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.PackagedException;

public class SparseArrayConstructor extends BuiltinFunctionObject {
    private static final String JOINstring = "join";
    private static final int JOINhash = JOINstring.hashCode();

    private static abstract class AbstractArrayPrototypeCallbackFunction extends AbstractArrayPrototypeFunction {
        
        private static final long serialVersionUID = 6291503290995311855L;

        private AbstractArrayPrototypeCallbackFunction( ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        protected ESValue loopOver(ESObject thisObject, ESValue[] arguments, ESObject data) throws EcmaScriptException {
            long length = getLength(thisObject);
            ESValue callbackFn = getArg(arguments,0);
            if (!callbackFn.isCallable()) {
                throw new TypeError(getFunctionName() + "(callbackFn,thisArg) : a function callbackFn must be supplied");
            }
            ESValue thisArg = getArg(arguments,1);
            ESValue[] functionArgs = new ESValue[3];
            functionArgs[2] = thisObject;

            adjustLength(data,length);
            
            ESValue exitCode = null;
            for( long k=0; k<length && exitCode == null; k++ ) {
                ESValue v = thisObject.getPropertyIfAvailable(k);
                if (v != null) {
                    functionArgs[0] = v;
                    functionArgs[1] = ESNumber.valueOf(k);
                    ESValue result = callbackFn.callFunction(thisArg, functionArgs);
                    exitCode = getExitCode(k,v, result, data);
                }
            }
            return exitCode;
        }

        protected void adjustLength(ESObject data, long length) throws EcmaScriptException {
            // nothing by default;
        }

        protected abstract ESValue getExitCode(long index, ESValue originalValue, ESValue result, ESObject data) throws EcmaScriptException;
    }
    
    private static abstract class AbstractArrayPrototypeReduceFunction extends AbstractArrayPrototypeFunction {
        private static final long serialVersionUID = -2347237482759579505L;

        public AbstractArrayPrototypeReduceFunction(ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        protected ESValue reduce(ESObject thisObject, ESValue[] arguments, Op op) throws EcmaScriptException, TypeError {
            long length = getLength(thisObject);
            ESValue callbackFn = getArg(arguments,0);
            ESValue thisArg = ESUndefined.theUndefined;
            ESValue[] functionArgs = new ESValue[4];
            functionArgs[3] = thisObject;

            if (!callbackFn.isCallable()) {
                throw new TypeError(getFunctionName()+"(fn,initialValue) : a function fn must be supplied");
            }
            ESValue accumulator = null;
            if (arguments.length > 1) {
                accumulator = arguments[1];
            } else {
                if (length == 0) {
                    throw new TypeError(getFunctionName()+"(fn,initialValue) : Initial value must be supplied if applied to empty array");
                }
            }

            for( long k=op.start(length-1); op.atEnd(k, length); k = op.next(k) ) {
                ESValue v = thisObject.getPropertyIfAvailable(k);
                if (v != null) {
                    if (accumulator == null) {
                        accumulator = v;
                    } else {
                        functionArgs[0] = accumulator;
                        functionArgs[1] = v;
                        functionArgs[2] = ESNumber.valueOf(k);
                        accumulator = callbackFn.callFunction(thisArg, functionArgs);
                    }
                }
            }
            if (accumulator == null) {
                throw new TypeError(getFunctionName()+"(fn,initialValue) : Initial value must be supplied if applied to an array containing no items");
            }
            return accumulator;
        }

    }

    private static class ArrayPrototypeReduce extends AbstractArrayPrototypeReduceFunction {
        
        private static final long serialVersionUID = 6291503290995311855L;

        private ArrayPrototypeReduce( ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        public ESValue callFunction(ESObject thisObject, ESValue[] arguments) throws EcmaScriptException {
            return reduce(thisObject, arguments, Op.FORWARD);
        }
    }
    
    private static class ArrayPrototypeReduceRight extends AbstractArrayPrototypeReduceFunction {
        
        private static final long serialVersionUID = 6291503290995311855L;

        private ArrayPrototypeReduceRight( ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        public ESValue callFunction(ESObject thisObject, ESValue[] arguments) throws EcmaScriptException {
            return reduce(thisObject, arguments, Op.BACKWARD_TO_ZERO);
        }
    }
    
    private static final class ArrayPrototypeEvery extends AbstractArrayPrototypeCallbackFunction {
        private static final long serialVersionUID = 5041355975337134239L;

        private ArrayPrototypeEvery( ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        protected ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            if (loopOver(thisObject, arguments, null) == ESBoolean.FALSE) {
                return ESBoolean.FALSE;
            }
            return ESBoolean.TRUE;
        }

        @Override
        protected ESValue getExitCode(long index, ESValue originalValue, ESValue result, ESObject data) throws EcmaScriptException {
            if (!result.booleanValue()) {
                return ESBoolean.FALSE;
            }
            return null;
        }
    }

    private static final class ArrayPrototypeSome extends AbstractArrayPrototypeCallbackFunction {
        private static final long serialVersionUID = 5041355975337134239L;

        private ArrayPrototypeSome( ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }


        @Override
        protected ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            if (loopOver(thisObject, arguments, null) == ESBoolean.TRUE) {
                return ESBoolean.TRUE;
            }
            return ESBoolean.FALSE;
        }

        @Override
        protected ESValue getExitCode(long index, ESValue originalValue, ESValue result, ESObject data) throws EcmaScriptException {
            if (result.booleanValue()) {
                return ESBoolean.TRUE;
            }
            return null;
        }
    }

    private static final class ArrayPrototypeForEach extends AbstractArrayPrototypeCallbackFunction {

        private static final long serialVersionUID = 3700602868158923461L;

        private ArrayPrototypeForEach( ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }


        @Override
        protected ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            loopOver(thisObject, arguments, null);
            return ESUndefined.theUndefined;
        }

        @Override
        protected ESValue getExitCode(long index, ESValue originalValue, ESValue result, ESObject data) throws EcmaScriptException {
            return null;
        }
    }

    private static final class ArrayPrototypeMap extends AbstractArrayPrototypeCallbackFunction {

        private static final long serialVersionUID = 3700602868158923461L;

        private ArrayPrototypeMap( ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }


        @Override
        protected ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESObject array = createArray();
            loopOver(thisObject, arguments, array);
            return array;
        }
        
        @Override
        protected void adjustLength(ESObject data, long length) throws EcmaScriptException {
            setLength(data,length);
        }

        @Override
        protected ESValue getExitCode(long index, ESValue originalValue, ESValue result, ESObject data) throws EcmaScriptException {
            data.putProperty(index, result);
            return null;
        }
    }

    private static final class ArrayPrototypeFilter extends AbstractArrayPrototypeCallbackFunction {

        private static final long serialVersionUID = 3700602868158923461L;

        private ArrayPrototypeFilter( ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }


        @Override
        protected ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESObject array = createArray();
            loopOver(thisObject, arguments, array);
            return array;
        }

        @Override
        protected ESValue getExitCode(long index, ESValue originalValue, ESValue result, ESObject data) throws EcmaScriptException {
            if (result.booleanValue()) {
                long length = getLength(data);
                data.putProperty(length, originalValue);
            }
            return null;
        }
    }

    private static final class ArrayPrototypeLastIndexOf extends AbstractArrayPrototypeIndexFunction {
        private static final long serialVersionUID = 7434987247465599789L;

        private ArrayPrototypeLastIndexOf( ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        protected ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESValue value = getArg(arguments,0);
            long length = getLength(thisObject);
            double start;
            if (arguments.length < 2) {
                start = length-1;
            } else {
                ESValue startValue = getArg(arguments,1);
                double doubleValue = startValue.doubleValue();
                if (Double.isInfinite(doubleValue)) {
                    start = (long) doubleValue;
                } else {
                    start = startValue.toInt32();
                }
            }
            start = (start>=0)?Math.min(start,length-1):length+start;
            return search(thisObject, value, (long) start, 0, Op.BACKWARD);
        }
    }
    private static abstract class Op {
        public static final Op FORWARD = new Op() {
            @Override public long start(long length) { return 0L; }
            @Override public boolean atEnd(long k, long length) { return k<length; }
            @Override public long next(long k) { return k+1; }
        };
        public static final Op BACKWARD = new Op() {
            @Override public long start(long length) { return length; }
            @Override public boolean atEnd(long k, long end) { return k>=end; }
            @Override public long next(long k) { return k-1; }
        };
        public static final Op BACKWARD_TO_ZERO = new Op() {
            @Override public long start(long length) { return length; }
            @Override public boolean atEnd(long k, long end) { return k>=0L; }
            @Override public long next(long k) { return k-1; }
        };
        
        public abstract long start(long length);
        public abstract boolean atEnd(long k, long end);
        public abstract long next(long k);
    }
    private static abstract class AbstractArrayPrototypeIndexFunction extends AbstractArrayPrototypeFunction {
        private static final long serialVersionUID = -8665273231324670975L;

        private AbstractArrayPrototypeIndexFunction( ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        protected ESValue search(ESObject thisObject, ESValue value, long start,
                long end, Op op) throws EcmaScriptException {
            for( long k=start; op.atEnd(k,end); k = op.next(k)) {
                ESValue v = thisObject.getPropertyIfAvailable(k);
                if (v != null && v.strictEqual(value)) {
                    return ESNumber.valueOf(k);
                }
            }
            return ESNumber.valueOf(-1);
        }
    }
    private static class ArrayPrototypeIndexOf extends AbstractArrayPrototypeIndexFunction {
        private static final long serialVersionUID = -6241106443340287500L;

        private ArrayPrototypeIndexOf( ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        protected ESValue callFunction(ESObject thisObject, ESValue[] arguments) throws EcmaScriptException {
            ESValue value = getArg(arguments,0);
            long length = getLength(thisObject);
            ESValue startValue = getArg(arguments,1);
            double start = startValue.doubleValue();
            if (start > length) {
                return ESNumber.valueOf(-1);
            }
            start = (long) start;
            start = (start>=0)?start:Math.max(length+start, 0);
            return search(thisObject, value, (long)start, length, Op.FORWARD);
        }
    }

    private static final class ArrayPrototypeUnshift extends AbstractArrayPrototypeFunction {
        private static final long serialVersionUID = 206678619907564785L;

        private ArrayPrototypeUnshift(ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        protected ESValue callFunction(ESObject thisObject, ESValue[] arguments) throws EcmaScriptException {
            int argLength = arguments.length;
            long length = getLength(thisObject);
            for (long k=length-1; k>=0; k--) {
                ESValue v = thisObject.getPropertyIfAvailable(k);
                if (v == null) {
                    thisObject.deleteProperty(k+argLength);
                } else {
                    thisObject.putProperty(k+argLength, v);
                }
            }
            for(int k=0; k<argLength; k++) {
                thisObject.putProperty((long)k, getArg(arguments,k));
            }
            return ESNumber.valueOf(length+argLength);
        }
    }

    private static final class ArrayPrototypeSplice extends AbstractArrayPrototypeFunction {
        private static final long serialVersionUID = 7599487781557227781L;

        private ArrayPrototypeSplice( ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        protected ESValue callFunction(ESObject thisObject, ESValue[] arguments) throws EcmaScriptException {
            ESObject result = createArray();
            long length = getLength(thisObject);
            double relativeStart = getArgAsInteger(arguments,0);
            long start = limitIndex(length, relativeStart);
            double deleteCount = getArgAsInteger(arguments,1);
            long actualDeleteCount = Math.min(Math.max((long)deleteCount, 0L), length-start);
            int itemCount = arguments.length-2;
            for (long k=0L; k<actualDeleteCount; k++) {
                ESValue v = thisObject.getPropertyIfAvailable(start+k);
                if (v != null) {
                    result.putProperty(k, v);
                }
            }
            if (itemCount < actualDeleteCount) {
                for(long k=start; k<length-actualDeleteCount; k++) {
                    long from = k+actualDeleteCount;
                    long to = k+itemCount;
                    ESValue v = thisObject.getPropertyIfAvailable(from);
                    if (v != null) {
                        thisObject.putProperty(to, v);
                    } else {
                        thisObject.deleteProperty(to);
                    }
                }
                for (long k=length-actualDeleteCount+itemCount; k<length; k++) {
                    thisObject.deleteProperty(k);
                }
            } else if (itemCount > actualDeleteCount) {
                for(long k=length-actualDeleteCount; k>start; k--) {
                    long from = k+actualDeleteCount-1;
                    long to = k+itemCount-1;
                    ESValue v = thisObject.getPropertyIfAvailable(from);
                    if (v != null) {
                        thisObject.putProperty(to, v);
                    } else {
                        thisObject.deleteProperty(to);
                    }
                }
            }
            for( int k=0; k<itemCount; k++) {
                thisObject.putProperty(k+start, getArg(arguments,k+2));
            }
            setLength(thisObject, length-actualDeleteCount+itemCount);
            return result;
        }
    }

    private static final class ArrayPrototypeSort extends
            AbstractArrayPrototypeFunction {
        private static final long serialVersionUID = 5041355975337134239L;

        private ArrayPrototypeSort( ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        protected ESValue callFunction(ESObject thisObject, ESValue[] arguments) throws EcmaScriptException {
            ESObjectListWrapper wrapper = new ESObjectListWrapper(thisObject);
            final ESValue comparator = getArg(arguments,0);
            try {
                Collections.sort(wrapper, new Comparator<ESValue>() {

                    public int compare(ESValue x, ESValue y) {
                        if (x == null) {
                            return (y==null)?0:1;
                        } else if (y == null) {
                            return -1;
                        }
                        if (x == ESUndefined.theUndefined) {
                            return (y == ESUndefined.theUndefined)?0:1;
                        } else if (y == ESUndefined.theUndefined) {
                            return -1;
                        }
                        try {
                            if (comparator != ESUndefined.theUndefined) {
                                ESValue comparisonResult = comparator.callFunction(ESUndefined.theUndefined, new ESValue[] { x, y });
                                return comparisonResult.toInt32();
                            }
                            return x.toString().compareTo(y.toString());
                        } catch ( EcmaScriptException e ) {
                            throw new PackagedException(e, null);
                        }
                    }
                });
            } catch ( PackagedException p ) {
                throw (EcmaScriptException)p.getPackage();
            }
            return thisObject;
        }
    }
    
    private static final class ArrayPrototypeSlice extends AbstractArrayPrototypeFunction {
        private static final long serialVersionUID = 8979182986148299638L;

        private ArrayPrototypeSlice( ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        protected ESValue callFunction(ESObject thisObject, ESValue[] arguments) throws EcmaScriptException {
            ESObject array = createArray();
            long length = getLength(thisObject);
            double relativeStart = getArgAsInteger(arguments,0);
            long start = limitIndex(length, relativeStart);
            ESValue endValue = getArg(arguments,1);
            long end;
            if (endValue == ESUndefined.theUndefined) {
                end = length;
            } else {
                double relativeEnd = endValue.toInteger();
                end = limitIndex(length, relativeEnd);
            }
            long n = 0L;
            for( long k=start; k<end; k++) {
                ESValue v = thisObject.getPropertyIfAvailable(k);
                if (v != null) {
                    array.putProperty(n, v);
                }
                n++;
            }
            return array;
        }
    }
    private static class ArrayPrototypeShift extends AbstractArrayPrototypeFunction {
        private static final long serialVersionUID = 5041355975337134239L;

        private ArrayPrototypeShift(ESObject functionPrototype, Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        protected ESValue callFunction(ESObject thisObject, ESValue[] arguments) throws EcmaScriptException {
            long length = getLength(thisObject);
            ESValue result;
            if (length == 0) {
                result = ESUndefined.theUndefined;
            } else {
                result = thisObject.getProperty(0L);
                length --;
                ESValue lastValue = result;
                for (long k=1; k<=length; k++) {
                    ESValue value = thisObject.getPropertyIfAvailable(k);
                    if (value == null) {
                        if (lastValue != null) {
                            thisObject.deleteProperty(k-1);
                        }
                    } else {
                        thisObject.putProperty(k-1, value);
                    }
                    lastValue = value;
                }
                thisObject.deleteProperty(length);
            }
            setLength(thisObject,length);
            return result;
        }
    }
    private static class ArrayPrototypeReverse extends AbstractArrayPrototypeFunction {
        private static final long serialVersionUID = 557643967369109715L;

        private ArrayPrototypeReverse(ESObject functionPrototype,
                Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        protected ESValue callFunction(ESObject thisObject, ESValue[] arguments) throws EcmaScriptException {
            ESObjectListWrapper wrapper = new ESObjectListWrapper(thisObject);
            try {
                Collections.reverse(wrapper);
            } catch( PackagedException packagedException ) {
                throw (EcmaScriptException)packagedException.getPackage();
            }
            return thisObject;
        }
    }
    private static class ArrayPrototypePush extends
            AbstractArrayPrototypeFunction {
        private static final long serialVersionUID = 5041355975337134239L;

        private ArrayPrototypePush(ESObject functionPrototype,
                Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        protected ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            long length = getLength(thisObject);
            for (ESValue esValue : arguments) {
                thisObject.putProperty(length++, esValue);
            }
            return ESNumber.valueOf(length);
        }
    }
    static abstract class AbstractArrayPrototypeFunction extends BuiltinFunctionObject {

        private static final long serialVersionUID = 5419061284666339120L;

        protected AbstractArrayPrototypeFunction(ESObject functionPrototype,
                Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }
        
        @Override
        public ESValue callFunction(ESValue thisValue, ESValue[] arguments) throws EcmaScriptException {
            ESObject thisObject = thisValue.toESObject(getEvaluator());
            return callFunction(thisObject,arguments);
        }
        
        protected abstract ESValue callFunction(ESObject thisObject, ESValue[] arguments) throws EcmaScriptException;

        protected void setLength(ESObject thisObject, long index) throws EcmaScriptException {
            thisObject.putProperty(StandardProperty.LENGTHstring, ESNumber.valueOf(index), StandardProperty.LENGTHhash);
        }

        protected ESObject createArray() throws EcmaScriptException {
            return getEvaluator().createArray();
        }

        protected long limitIndex(long length, double relativeStart) {
            long start;
            if (relativeStart < 0) {
                start = (long)Math.max(length+relativeStart, 0d);
            } else {
                start = (long) Math.min(relativeStart, length);
            }
            return start;
        }

        public static long getLength(ESObject thisObject) throws EcmaScriptException {
            long length = thisObject.getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash).toUInt32();
            return length;
        }
    }
    private static final class ArrayPrototypePop extends AbstractArrayPrototypeFunction {
        private static final long serialVersionUID = 1996538016909813790L;

        private ArrayPrototypePop(ESObject functionPrototype,
                Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        public ESValue callFunction(ESObject thisObject, ESValue[] arguments) throws EcmaScriptException {
            long length = getLength(thisObject);
            long index;
            ESValue value;
            if (length == 0) {
                index = 0;
                value = ESUndefined.theUndefined;
            } else {
                index = length - 1;
                value = thisObject.getProperty(index);
                thisObject.deleteProperty(index);
            }
            setLength(thisObject, index);
            return value;
        }
    }

    private static final class ArrayPrototypeConcat extends
            AbstractArrayPrototypeFunction {
        private static final long serialVersionUID = 8819892791499592087L;

        private ArrayPrototypeConcat(ESObject functionPrototype,
                Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        public ESValue callFunction(ESObject thisObject, ESValue[] arguments) throws EcmaScriptException {
            ESObject result = createArray();
            long n=0;
            for (int i=-1; i<arguments.length; i++) {
                ESValue v = (i==-1)?thisObject:arguments[i];
                if (v instanceof SparseArrayPrototype) {
                    ESObject element = (ESObject)v;
                    long length = getLength(element);
                    for (long k=0; k<length; k++) {
                        ESValue subElement = element.getPropertyIfAvailable(k);
                        if (subElement != null) {
                            result.putProperty(n, subElement);
                        }
                        n++;
                    }
                } else {
                    result.putProperty(n++, v);
                }
            }
            return result;
        }
    }

    private static final class ArrayPrototypeToString extends
            AbstractArrayPrototypeFunction {
        private static final long serialVersionUID = -4163809301044076356L;

        private ArrayPrototypeToString(ESObject functionPrototype,
                Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        public ESValue callFunction(ESObject thisObject, ESValue[] arguments) throws EcmaScriptException {
            ESValue func = thisObject.getProperty(JOINstring, JOINhash);
            if (!(func instanceof FunctionPrototype)) {
                func = getEvaluator().getObjectPrototype().getProperty(StandardProperty.TOSTRINGstring, StandardProperty.TOSTRINGhash);
            }
            return func.callFunction(thisObject, ESValue.EMPTY_ARRAY);
        }
    }

    private static class ArrayPrototypeJoin extends AbstractArrayPrototypeFunction {
        private static final long serialVersionUID = -1601012834773820841L;

        private ArrayPrototypeJoin(ESObject functionPrototype,
                Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        public ESValue callFunction(ESObject thisObject, ESValue[] arguments) throws EcmaScriptException {
            long length = getLength(thisObject);
            String separator = getSeparator(arguments);
            if (length == 0) {
                return ESString.valueOf("");
            }
            StringBuilder sb = new StringBuilder();
            long index = 0L;
            sb.append(getPropertyAsString(thisObject, index));
            for( index=1; index<length; index++) {
                sb.append(separator);
                sb.append(getPropertyAsString(thisObject, index));
            }
            return new ESString(sb.toString());
        }

        private String getSeparator(ESValue[] arguments) {
            ESValue arg = getArg(arguments,0);
            String separator;
            if (arg.getTypeOf() == EStypeUndefined) {
                separator = ",";
            } else {
                separator = arg.toString();
            }
            return separator;
        }

        private String getPropertyAsString(ESObject object, long index)
                throws EcmaScriptException {
            String propertyName = Long.toString(index);
            ESValue property = object.getProperty(propertyName, propertyName.hashCode());
            String propertyAsString;
            if (property.getTypeOf() == EStypeUndefined || property.getTypeOf() == EStypeNull) {
                propertyAsString = "";
            } else {
                propertyAsString = property.toString();
            }
            return propertyAsString;
        }
    }

    private static final class ArrayConstructorIsArray extends
            BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        private ArrayConstructorIsArray(ESObject functionPrototype,
                Evaluator evaluator, String functionName, int length) throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESValue arg = getArg(arguments, 0);
            return ESBoolean.valueOf(arg.isArray());
        }
    }

    private static final long serialVersionUID = -4121117341521118647L;

    protected SparseArrayConstructor(ESObject prototype, Evaluator evaluator) throws EcmaScriptException {
        super(prototype, evaluator, StandardProperty.ARRAYstring,1);
    }
    
    @Override
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        return doConstruct(arguments);
    }

    @Override
    public ESObject doConstruct(ESValue[] arguments) throws EcmaScriptException {
        Evaluator evaluator = getEvaluator();
        SparseArrayPrototype sparseArrayPrototype = new SparseArrayPrototype(getEvaluator().getArrayPrototype(),evaluator);
        switch (arguments.length) {
        case 0:
            break;
        case 1:
            if (arguments[0].isNumberValue()) {
                if (isInteger(arguments[0])) {
                    sparseArrayPrototype.putProperty(StandardProperty.LENGTHstring,arguments[0],StandardProperty.LENGTHhash);
                    break;
                }
                throw new RangeError("Array length "+arguments[0]+" out of range");
            }
        default:
            long i = 0;
            for (ESValue argument : arguments) {
                sparseArrayPrototype.putProperty(i++, argument);
            }
        }
        return sparseArrayPrototype;
    }
    

    private boolean isInteger(ESValue esValue) throws EcmaScriptException {
        return esValue.isUInt32();
    }

    public static ESObject makeArrayObject(Evaluator evaluator,
            ObjectPrototype objectPrototype, FunctionPrototype functionPrototype) throws EcmaScriptException {
        SparseArrayPrototype arrayPrototype = new SparseArrayPrototype(objectPrototype, evaluator);
        evaluator.setArrayPrototype(arrayPrototype);

        SparseArrayConstructor sparseArrayConstructor = new SparseArrayConstructor(functionPrototype, evaluator);
        sparseArrayConstructor.putHiddenProperty("isArray", new ArrayConstructorIsArray(functionPrototype, evaluator, "isArray", 1));
        
        arrayPrototype.putProperty(StandardProperty.CONSTRUCTORstring, WRITEABLE|CONFIGURABLE, sparseArrayConstructor);
        arrayPrototype.putHiddenProperty(JOINstring,new ArrayPrototypeJoin(functionPrototype, evaluator, JOINstring, 1));
        arrayPrototype.putHiddenProperty(StandardProperty.TOSTRINGstring,new ArrayPrototypeToString(functionPrototype, evaluator, StandardProperty.TOSTRINGstring, 0));
        arrayPrototype.putHiddenProperty("concat", new ArrayPrototypeConcat(functionPrototype, evaluator, "concat", 1));
        arrayPrototype.putHiddenProperty("pop", new ArrayPrototypePop(functionPrototype, evaluator, "pop", 0));
        arrayPrototype.putHiddenProperty("push", new ArrayPrototypePush(functionPrototype, evaluator, "push", 1));
        arrayPrototype.putHiddenProperty("reverse", new ArrayPrototypeReverse(functionPrototype, evaluator, "reverse", 0));
        arrayPrototype.putHiddenProperty("shift", new ArrayPrototypeShift(functionPrototype, evaluator, "shift", 0));
        arrayPrototype.putHiddenProperty("slice", new ArrayPrototypeSlice(functionPrototype, evaluator, "slice", 2));
        arrayPrototype.putHiddenProperty("sort", new ArrayPrototypeSort(functionPrototype, evaluator, "sort", 1));
        arrayPrototype.putHiddenProperty("splice", new ArrayPrototypeSplice(functionPrototype, evaluator, "splice", 2));
        arrayPrototype.putHiddenProperty("unshift", new ArrayPrototypeUnshift(functionPrototype, evaluator, "unshift", 1));
        arrayPrototype.putHiddenProperty("indexOf", new ArrayPrototypeIndexOf(functionPrototype, evaluator, "indexOf", 1));
        arrayPrototype.putHiddenProperty("lastIndexOf", new ArrayPrototypeLastIndexOf(functionPrototype, evaluator, "lastIndexOf", 1));
        arrayPrototype.putHiddenProperty("every", new ArrayPrototypeEvery(functionPrototype, evaluator, "every", 1));
        arrayPrototype.putHiddenProperty("some", new ArrayPrototypeSome(functionPrototype, evaluator, "some", 1));
        arrayPrototype.putHiddenProperty("forEach", new ArrayPrototypeForEach(functionPrototype, evaluator, "forEach", 1));
        arrayPrototype.putHiddenProperty("map", new ArrayPrototypeMap(functionPrototype, evaluator, "map", 1));
        arrayPrototype.putHiddenProperty("filter", new ArrayPrototypeFilter(functionPrototype, evaluator, "filter", 1));
        arrayPrototype.putHiddenProperty("reduce", new ArrayPrototypeReduce(functionPrototype, evaluator, "reduce", 1));
        arrayPrototype.putHiddenProperty("reduceRight", new ArrayPrototypeReduceRight(functionPrototype, evaluator, "reduceRight", 1));
        
        sparseArrayConstructor.putProperty(StandardProperty.PROTOTYPEstring, 0, arrayPrototype);
        return sparseArrayConstructor;
    }

}
