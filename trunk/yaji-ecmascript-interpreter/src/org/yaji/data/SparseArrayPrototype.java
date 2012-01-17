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
import FESI.Data.ObjectObject;
import FESI.Data.StandardProperty;
import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.RangeError;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;

public class SparseArrayPrototype extends ESObject {

    private static final long serialVersionUID = 7416031370358821189L;
    private UInt32BitSet sparseValues = null;

    protected SparseArrayPrototype(ESObject prototype, Evaluator evaluator) throws EcmaScriptException {
        super(prototype, evaluator);
        putProperty(StandardProperty.LENGTHstring, WRITEABLE, ESNumber.valueOf(0));
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
        updateLength(index, isStrictMode());
        super.putProperty(indexString, propertyValue, indexString.hashCode());
    }

    @Override
    public void putOwnProperty(long index, ESValue propertyValue)
            throws EcmaScriptException {
        updateLength(index, isStrictMode());
        super.putOwnProperty(index, propertyValue);
    }
    
    private void updateLength(long index, boolean shouldThrow) throws EcmaScriptException {
        long length = getLength();
        if (index >= length) {
            if (index > length) {
                if (sparseValues == null) {
                    sparseValues = new UInt32BitSet();
                    sparseValues.set(0L,length);
                }
                sparseValues.set(index);
            }
            super.putProperty(StandardProperty.LENGTHstring, ESNumber.valueOf(index+1), StandardProperty.LENGTHhash, shouldThrow);
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
        if (isAllDigits(propertyName)) {
            long longResult = Long.parseLong(propertyName);
            if (longResult < 0xFFFFFFFFL && longResult >= 0) {
                updateLength(longResult, true);
            }
        } else if (StandardProperty.LENGTHhash == propertyName.hashCode() && StandardProperty.LENGTHstring.equals(propertyName)) {
            ESValue propertyValue = desc.getPropertyIfAvailable(StandardProperty.VALUEstring, StandardProperty.VALUEhash);
            if (propertyValue != null) {
                long newLen = validateLength(propertyValue);
                if (newLen != propertyValue.doubleValue()) {
                    throw new RangeError("Array[[defineProperty]]: Length must be an integer value");
                }
                desc = copyDescriptor(desc);
                desc.putProperty(StandardProperty.VALUEstring, ESNumber.valueOf(newLen), StandardProperty.VALUEhash);
                long currentLength = getLength();
                if (newLen < currentLength) {
                    if (!canPut(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash)) {
                        throw new TypeError("Cannot change length of array because the length is read only");
                    }
                    ESValue newPropertyValue = trimArray(propertyValue);
                    if (newPropertyValue != propertyValue) {
                        desc.putProperty(StandardProperty.VALUEstring, newPropertyValue, StandardProperty.VALUEhash);
                        super.defineProperty(propertyName, desc);
                        throw new TypeError("Cannot change length of array because element is not configurable");
                    }
                }
            }
        }

        return super.defineProperty(propertyName, desc);
    }
    
    private ESObject copyDescriptor(ESObject desc) throws EcmaScriptException {
        ESObject copy = ObjectObject.createObject(getEvaluator());
        copyPropertyIfSet(desc, copy, StandardProperty.CONFIGURABLEstring, StandardProperty.CONFIGURABLEhash);
        copyPropertyIfSet(desc, copy, StandardProperty.ENUMERABLEstring, StandardProperty.ENUMERABLEhash);
        copyPropertyIfSet(desc, copy, StandardProperty.VALUEstring, StandardProperty.VALUEhash);
        copyPropertyIfSet(desc, copy, StandardProperty.WRITABLEstring, StandardProperty.WRITABLEhash);
        copyPropertyIfSet(desc, copy, StandardProperty.GETstring, StandardProperty.GEThash);
        copyPropertyIfSet(desc, copy, StandardProperty.SETstring, StandardProperty.SEThash);
        return copy;
    }

    protected void copyPropertyIfSet(ESObject desc, ESObject copy, String propertyName, int propertyNameHashCode)
            throws EcmaScriptException {
        ESValue value = desc.getPropertyIfAvailable(propertyName, propertyNameHashCode);
        if (value != null) {
            copy.putProperty(propertyName, value, propertyNameHashCode);
        }
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
                propertyValue = trimArray(propertyValue);
            }
            super.putProperty(propertyName, propertyValue, propertyHash);
        }
    }

    protected ESValue trimArray(ESValue propertyValue)
            throws EcmaScriptException, RangeError {
        long requestedLength = validateLength(propertyValue);
        long length = getLength();
        while (requestedLength < length) {
            length = decrement(length);
            if (length >= requestedLength && (sparseValues == null || sparseValues.get(length))) {
                if (!deleteProperty(length)) {
                    length ++;
                    break;
                }
            }
        }
        if (requestedLength < length) {
            propertyValue = ESNumber.valueOf(length);
        }
        return propertyValue;
    }
    
    private long decrement(long length) {
        if (sparseValues == null) {
            return length - 1L;
        }
        return sparseValues.lastSetBit(length-1L);
    }

    @Override
    public boolean deleteProperty(long index) throws EcmaScriptException {
        String propertyName = Long.toString(index);
        boolean result = super.deleteProperty(propertyName,propertyName.hashCode());
        if (result) {
            if (sparseValues != null) {
                sparseValues.clear(index);
            }
        }
        return result;
    }
    
    @Override
    public boolean deleteProperty(String propertyName, int hash)
            throws EcmaScriptException {
        boolean result = super.deleteProperty(propertyName, hash);
        if (result && isAllDigits(propertyName)) {
            long longResult = Long.parseLong(propertyName);
            if (longResult < 0xFFFFFFFFL && longResult >= 0) {
                if (sparseValues != null) {
                    sparseValues.clear(longResult);
                }
            }
        }
        return result;
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
