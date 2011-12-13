package org.yaji.json;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import FESI.Data.ESObject;
import FESI.Data.ESUndefined;
import FESI.Data.ESValue;
import FESI.Data.FunctionPrototype;
import FESI.Data.StandardProperty;
import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.TypeError;

public class JsonState {
    private static class InstanceMatch {

        private final ESValue object;

        public InstanceMatch(ESValue object) {
            this.object = object;
        }

        @Override
        public int hashCode() {
            return object.hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof InstanceMatch) {
                InstanceMatch instanceMatch = (InstanceMatch) obj;
                return instanceMatch.object == object;
            }
            return false;
        }
    }

    public Map<String,Long> allowedList;
    private ESValue replacerFunction;
    public JsonIndent indent;
    private Deque<InstanceMatch> stack = new ArrayDeque<InstanceMatch>();
    
    public JsonState(ESValue replacer, ESValue indentValue) {
        try {
            indent = JsonIndent.create(indentValue);
        } catch (EcmaScriptException e) {
            e.printStackTrace();
        }
        try {
            this.allowedList = createAllowedSet(replacer);
        } catch (EcmaScriptException e) {
            e.printStackTrace();
        }
        this.replacerFunction = getReplacerFunction(replacer);
    }

    private ESValue getReplacerFunction(ESValue replacer) {
        ESValue replacerFunction = ESUndefined.theUndefined;
        if (replacer instanceof FunctionPrototype) {
            replacerFunction = replacer;
        }
        return replacerFunction;
    }

    private Map<String, Long> createAllowedSet(ESValue replacerFunction)
            throws EcmaScriptException {
        HashMap<String, Long> set = new HashMap<String,Long>();
        if (replacerFunction.isArray()) {
            ESObject filters = (ESObject) replacerFunction;
            long length = filters.getProperty(StandardProperty.LENGTHstring, StandardProperty.LENGTHhash).toUInt32();
            for(long i=0; i<length; i++) {
                ESValue property = filters.getProperty(i);
                if (property.isStringValue() || property.isNumberValue()) {
                    set.put(property.toString(),Long.valueOf(i));
                }
            }
        }
        return set;
    }

    public ESValue callReplacerFunction(ESValue thisObject, ESValue key, ESValue value) throws EcmaScriptException {
        if (replacerFunction != ESUndefined.theUndefined) {
            return replacerFunction.callFunction(thisObject, new ESValue[] { key, value });
        }
        return value;
    }

    public void pushCyclicCheck(ESValue object) throws TypeError {
        InstanceMatch instanceMatch = new InstanceMatch(object);
        if (stack.contains(instanceMatch)) {
            throw new TypeError("Cannot convert object to String - Structure is cyclical");
        }
        stack.push(instanceMatch);
    }

    public void popCyclicCheck() {
        stack.pop();
    }

    public long getAllowedIndex(String key) {
        Long index = allowedList.get(key);
        return index == null ? -1 : index.longValue();
    }

    public int allowedSize() {
        return allowedList.size();
    }
}
