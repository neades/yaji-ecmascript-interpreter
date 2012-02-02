package FESI.Data;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;

public class ErrorObject extends BuiltinFunctionObject {
    private static class ErrorPrototypeToString extends BuiltinFunctionObject {
        private static final long serialVersionUID = 5282799604824991358L;

        private ErrorPrototypeToString(ESObject functionPrototype,
                Evaluator evaluator, String functionName, int length)
                throws EcmaScriptException {
            super(functionPrototype, evaluator, functionName, length);
        }

        @Override
        public ESValue callFunction(ESValue thisValue, ESValue[] arguments) throws EcmaScriptException {
            if (!thisValue.isObjectCoercible()) {
                throw new TypeError("Error.prototype.toString cannot be applied to a non-object");
            }
            ESObject thisObject = thisValue.toESObject(getEvaluator());
            String name = getValue(thisObject, "name", "Error");
            
            String msg = getValue(thisObject, "message", "");
            
            if (name.length() == 0) {
                return new ESString(msg);
            }
            if (msg.length() == 0) {
                return new ESString(name);
            }
            return new ESString(name + ": "+msg);
        }

        public String getValue(ESObject thisObject, String propertyName,
                String defaultString) throws EcmaScriptException {
            ESValue value = thisObject.getProperty(propertyName, propertyName.hashCode());
            String name;
            if (value.getTypeOf() != EStypeUndefined) {
                name = value.callToString();
            } else {
                name = defaultString;
            }
            return name;
        }
    }

    private static final long serialVersionUID = 866651721781282211L;

    private ErrorObject(ESObject functionPrototype, Evaluator evaluator, ObjectPrototype objectPrototype) throws EcmaScriptException {
        super(functionPrototype, evaluator, "Error", 1);
        ErrorPrototype errorPrototype = new ErrorPrototype(objectPrototype,evaluator);
        errorPrototype.putProperty("constructor", this, "constructor".hashCode());
        errorPrototype.putProperty("name", ESString.valueOf("Error"), "name".hashCode());
        errorPrototype.putProperty("message", ESString.valueOf(""), "message".hashCode());
        errorPrototype.putHiddenProperty(StandardProperty.TOSTRINGstring, new ErrorPrototypeToString(functionPrototype, evaluator, StandardProperty.TOSTRINGstring, 0));
        putProperty(StandardProperty.PROTOTYPEstring, 0, errorPrototype);
    }

    @Override
    public String getESClassName() {
        return "Error";
    }
    
    @Override
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        return doConstruct(arguments);
    }
    
    @Override
    public ESObject doConstruct(final ESValue[] arguments)
            throws EcmaScriptException {
        ESObject error = new ESObject(getPrototypeProperty(),getEvaluator()) {
            private static final long serialVersionUID = -5191230380035994792L;
        {
            if (arguments.length > 0 && arguments[0].getTypeOf() != EStypeUndefined) {
                putProperty("message",arguments[0],"message".hashCode());
            }
        }};
        return error;
    }
    
    public static ErrorObject make(Evaluator evaluator,
            ObjectPrototype objectPrototype, FunctionPrototype functionPrototype) throws EcmaScriptException {
        return new ErrorObject(functionPrototype,evaluator,objectPrototype);
    }

    public ErrorPrototype getPrototypeProperty() throws EcmaScriptException {
        return (ErrorPrototype) getProperty(StandardProperty.PROTOTYPEstring,StandardProperty.PROTOTYPEhash);
    }


}
