package org.yaji.data;

import FESI.Data.ESValue;
import FESI.Data.ValueDescription;
import FESI.Exceptions.EcmaScriptException;


public class ESAccessorValue extends ESValue {

    private static final long serialVersionUID = -8967617730129520928L;
    
    protected ESValue setter;
    protected ESValue getter;

    @Override
    public String toDetailString() {
        return "";
    }

    @Override
    public Object toJavaObject() {
        return null;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public ESValue toESPrimitive() throws EcmaScriptException {
        throw new EcmaScriptException("Accessor should never be converted to ESPrimitive");
    }

    @Override
    public ESValue toESPrimitive(int preferedType) throws EcmaScriptException {
        throw new EcmaScriptException("Accessor should never be converted to ESPrimitive");
    }

    @Override
    public int getTypeOf() {
        return EStypeUndefined;
    }

    @Override
    public String getTypeofString() {
        return "Accessor";
    }

    @Override
    public boolean isComposite() {
        return false;
    }

    @Override
    public ValueDescription getDescription(String name) {
        return null;
    }
    
    @Override
    public void setGetAccessorDescriptor(ESValue get) throws EcmaScriptException {
        this.getter = get;
    }
    
    @Override
    public void setSetAccessorDescriptor(ESValue set) throws EcmaScriptException {
        this.setter = set;
    }
    
    @Override
    public ESValue getSetAccessorDescriptor() throws EcmaScriptException {
        return setter;
    }
    
    @Override
    public ESValue getGetAccessorDescriptor() throws EcmaScriptException {
        return getter;
    }
    
    @Override
    public boolean isAccessorDescriptor() {
        return true;
    }
    
    @Override
    public boolean hasGetAccessorDescriptor() {
        return getter != null && getter.isCallable();
    }
    
    @Override
    public boolean hasSetAccessorDescriptor() {
        return setter != null && setter.isCallable();
    }
}
