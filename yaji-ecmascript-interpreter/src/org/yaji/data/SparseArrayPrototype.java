package org.yaji.data;

import java.io.IOException;

import org.yaji.json.JsonState;

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
        long length = getLength();
        if (index >= length) {
            super.putProperty(StandardProperty.LENGTHstring, ESNumber.valueOf(index+1), StandardProperty.LENGTHhash);
        }
    }

    private long getLength() throws EcmaScriptException {
        ESValue lengthProperty = getProperty(StandardProperty.LENGTHstring, StandardProperty.LENGTHhash);
        long length = lengthProperty.toUInt32();
        return length;
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
                validateLength(propertyValue);
            }
            super.putProperty(propertyName, propertyValue, propertyHash);
        }
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
