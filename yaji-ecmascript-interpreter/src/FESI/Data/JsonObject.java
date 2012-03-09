package FESI.Data;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;

import org.yaji.json.Json;
import org.yaji.json.JsonUtil;
import org.yaji.json.ParseException;
import org.yaji.json.TokenMgrError;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.SyntaxError;
import FESI.Interpreter.Evaluator;

public class JsonObject extends ESObject {
    protected JsonObject(ESObject prototype, Evaluator evaluator, ESObject functionPrototype) throws EcmaScriptException {
        super(prototype, evaluator);
        
        putHiddenProperty("parse", new BuiltinFunctionObject(functionPrototype,evaluator,"parse",2) {
            private static final long serialVersionUID = 1L;
            @Override
            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                ESValue jsonText = arguments.length>0?arguments[0]:ESUndefined.theUndefined;
                ESValue reviver = arguments.length>1?arguments[1]:ESUndefined.theUndefined;
                Json json = new Json( new StringReader(jsonText.toString()));
                json.setEvaluator(getEvaluator());
                try {
                    return revive(json.Parse(),reviver);
                } catch (ParseException e) {
                    throw new SyntaxError(e.getMessage());
                } catch (TokenMgrError e) {
                    throw new SyntaxError(e.getMessage());
                }
            }
        });
        putHiddenProperty("stringify", new BuiltinFunctionObject(functionPrototype,evaluator,"stringify",3) {
            private static final long serialVersionUID = 1L;
            @Override
            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                ESValue obj2json = arguments.length>0?arguments[0]:ESUndefined.theUndefined;
                ESValue replacerFunction = arguments.length>1?arguments[1]:ESUndefined.theUndefined;
                ESValue indent = arguments.length>2?arguments[2]:ESUndefined.theUndefined;
                try {
                    String result = JsonUtil.stringify(getEvaluator(),obj2json,replacerFunction, indent);
                    if (result == null) {
                        return ESUndefined.theUndefined;
                    }
                    return new ESString(result);
                } catch (IOException e) {
                    throw new EcmaScriptException(e.getMessage(), "Error");
                }
            }
        });
    }

    protected ESValue revive(ESValue unfiltered, ESValue reviver) throws EcmaScriptException {
        if (!(reviver instanceof FunctionPrototype)) {
            return unfiltered;
        }
        ObjectPrototype root = ObjectObject.createObject(getEvaluator());
        root.putProperty("", unfiltered, "".hashCode());
        return stringWalker.walk(root,"",reviver);
    }

    private static Walker<String> stringWalker = new Walker<String>() {
        @Override
        protected ESValue get(ESObject holder, String name) throws EcmaScriptException {
            return holder.getProperty(name);
        }
        @Override
        protected ESValue toESValue(String name) {
            return new ESString(name);
        }
    };
    
    private static Walker<Long> integerWalker = new Walker<Long>() {
        @Override
        protected ESValue get(ESObject holder, Long idx) throws EcmaScriptException {
            return holder.getProperty(idx.longValue());
        }
        @Override
        protected ESValue toESValue(Long idx) {
            return new ESString(idx.toString());
        }
    };
    
    private static abstract class Walker<T> {
        public ESValue walk(ESObject holder, T name, ESValue reviver) throws EcmaScriptException {
            ESValue val = get(holder, name);
            if (!val.isPrimitive()) {
                ESObject object = (ESObject) val;
                if (object.isArray()) {
                    walkArray(object, reviver);
                } else {
                    walkObject(object, reviver);
                }
            }
            return reviver.callFunction(holder, new ESValue[] { toESValue(name), val });
        }

        protected abstract ESValue get(ESObject holder, T name) throws EcmaScriptException;
        protected abstract ESValue toESValue(T name);

        private void walkArray(ESObject array, ESValue reviver)
                throws EcmaScriptException {
            long length = array.getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash).toUInt32();
            for( long i=0; i<length; i++) {
                ESValue newElement = integerWalker.walk(array, Long.valueOf(i), reviver);
                array.putProperty(i, newElement);
            }
        }

        private void walkObject(ESObject object, ESValue reviver)
                throws EcmaScriptException {
            Enumeration<String> allProperties = object.getAllProperties();
            while (allProperties.hasMoreElements()) {
                String key = allProperties.nextElement();
                ESValue newElement = stringWalker.walk(object,key,reviver);
                if (newElement.getTypeOf() == EStypeUndefined) {
                    object.deleteProperty(key, key.hashCode());
                } else {
                    object.putProperty(key, newElement);
                }
            }
        }

    }
    
    private static final long serialVersionUID = 468753851884316472L;
    
    @Override
    public String getESClassName() {
        return "JSON";
    }

    public static ESValue makeJsonObject(Evaluator evaluator,
            ObjectPrototype objectPrototype, FunctionPrototype functionPrototype) throws EcmaScriptException {
        return new JsonObject(objectPrototype,evaluator,functionPrototype);
    }

}
