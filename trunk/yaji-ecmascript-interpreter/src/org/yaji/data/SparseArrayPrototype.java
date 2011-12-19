package org.yaji.data;

import java.io.IOException;

import org.yaji.json.JsonState;
import org.yaji.util.ArrayUtil;
import org.yaji.util.UInt32BitSet;

import FESI.Data.ESNull;
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESValue;
import FESI.Data.StandardProperty;
import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.RangeError;
import FESI.Interpreter.Evaluator;

public class SparseArrayPrototype extends ESObject {

    private static final long serialVersionUID = 7416031370358821189L;
    private UInt32BitSet sparseValues = null;

    protected SparseArrayPrototype(ESObject prototype, Evaluator evaluator) throws EcmaScriptException {
        super(prototype, evaluator);
        putProperty(StandardProperty.LENGTHstring, WRITEABLE|CONFIGURABLE, ESNumber.valueOf(0));
    }

    @Override
    public String getESClassName() {
        return StandardProperty.ARRAYstring;
    }
    
    @Override
    public boolean isArray() {
        return true;
    }
    
    @Override
    public void putProperty(long index, ESValue propertyValue)
            throws EcmaScriptException {
        putProperty(index, Long.toString(index), propertyValue);
    }

    private void putProperty(long index, String indexString,
            ESValue propertyValue) throws EcmaScriptException {
        super.putProperty(indexString, propertyValue, indexString.hashCode());
        updateLength(index);
    }

    private void updateLength(long index) throws EcmaScriptException {
        long length = getLength();
        if (index >= length) {
            if (index > length) {
                if (sparseValues == null) {
                    sparseValues = new UInt32BitSet();
                    sparseValues.set(0L,length);
                }
                sparseValues.set(index);
            }
            super.putProperty(StandardProperty.LENGTHstring, ESNumber.valueOf(index+1), StandardProperty.LENGTHhash);
        }
    }

    private long getLength() throws EcmaScriptException {
        return ArrayUtil.getArrayLength(this);
    }

    private long validateLength(ESValue lengthProperty)
            throws EcmaScriptException, RangeError {
        long length = lengthProperty.toUInt32();
        if (lengthProperty.longValue() != length) {
            throw new RangeError("Array: Maximum length is "+0xFFFFFFFFL);
        }
        return length;
    }
    
    @Override
    public ESValue defineProperty(String propertyName, ESObject desc)
            throws EcmaScriptException {
        ESValue result = super.defineProperty(propertyName, desc);
        if (isAllDigits(propertyName)) {
            long longResult = Long.parseLong(propertyName);
            if (longResult < 0xFFFFFFFFL && longResult >= 0) {
                updateLength(longResult);
            }
        }
        return result;
    }
    
    @Override
    public void putProperty(String propertyName, ESValue propertyValue, int propertyHash)
            throws EcmaScriptException {
        if (isAllDigits(propertyName)) {
            long longResult = Long.parseLong(propertyName);
            if (longResult < 0xFFFFFFFFL && longResult >= 0) {
                putProperty(longResult,propertyName,propertyValue);
            } else {
                super.putProperty(propertyName, propertyValue, propertyHash);
            }
        } else {
            if (StandardProperty.LENGTHhash == propertyHash && StandardProperty.LENGTHstring.equals(propertyName)) {
                long requestedLength = validateLength(propertyValue);
                long length = getLength();
                while (requestedLength < length) {
                    length = decrement(length);
                    if (length >= requestedLength && (sparseValues == null || sparseValues.get(length))) {
                        if (!deleteProperty(length)) {
                            break;
                        }
                    }
                }
                if (requestedLength < length) {
                    propertyValue = ESNumber.valueOf(length);
                }
            }
            super.putProperty(propertyName, propertyValue, propertyHash);
        }
    }
    
    private long decrement(long length) {
        if (sparseValues == null) {
            return length - 1L;
        }
        return sparseValues.lastSetBit(length-1L);
    }

    @Override
    public boolean deleteProperty(long index) throws EcmaScriptException {
        if (sparseValues != null) {
            sparseValues.clear(index);
        }
        String propertyName = Long.toString(index);
        return super.deleteProperty(propertyName,propertyName.hashCode());
    }
    
    @Override
    public boolean deleteProperty(String propertyName, int hash)
            throws EcmaScriptException {
        if (isAllDigits(propertyName)) {
            long longResult = Long.parseLong(propertyName);
            if (longResult < 0xFFFFFFFFL && longResult >= 0) {
                if (sparseValues != null) {
                    sparseValues.clear(longResult);
                }
            }
        }
        return super.deleteProperty(propertyName, hash);
    }
    
    @Override
    public void toJson(Appendable appendable, JsonState state, String parentPropertyName) throws IOException, EcmaScriptException {
        state.pushCyclicCheck(this);
        state.indent.push();
        appendable.append('[');
        String separator = state.indent.start();
        
        long length = getLength();
        boolean output = false;
        for (long i=0; i<length; i++) {
            ESValue value = getPropertyIfAvailable(i);
            if (value != null) {
                appendable.append(separator);
                value = state.callReplacerFunction(this, ESString.valueOf((int)i), value );
                if (!value.canJson()) {
                    value = ESNull.theNull;
                }
                value.toJson(appendable, state, "");
                separator = state.indent.separator();
                output = true;
            }
        }
        if (output) {
            appendable.append(state.indent.end());
        }
        appendable.append(']');
        state.indent.pop();
        state.popCyclicCheck();
    }
    
    @Override
    public boolean canJson() {
        return true;
    }
    

}
