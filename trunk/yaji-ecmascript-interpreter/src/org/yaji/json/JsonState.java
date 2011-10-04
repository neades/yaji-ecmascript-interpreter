package org.yaji.json;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import FESI.Data.ArrayPrototype;
import FESI.Data.ESUndefined;
import FESI.Data.ESValue;
import FESI.Data.FunctionPrototype;
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
            if (obj instanceof InstanceMatch) {
                InstanceMatch instanceMatch = (InstanceMatch) obj;
                return instanceMatch.object == object;
            }
            return false;
        }
    }

    public Map<String,Integer> allowedList;
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

    private Map<String, Integer> createAllowedSet(ESValue replacerFunction)
            throws EcmaScriptException {
        HashMap<String, Integer> set = new HashMap<String,Integer>();
        if (replacerFunction instanceof ArrayPrototype) {
            ArrayPrototype filters = (ArrayPrototype) replacerFunction;
            for(int i=0; i<filters.size(); i++) {
                ESValue property = filters.getProperty(i);
                if (property.isStringValue() || property.isNumberValue()) {
                    set.put(property.toString(),Integer.valueOf(i));
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

    public int getAllowedIndex(String key) {
        Integer index = allowedList.get(key);
        return index == null ? -1 : index.intValue();
    }

    public int allowedSize() {
        return allowedList.size();
    }
}
