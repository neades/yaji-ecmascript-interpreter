package FESI.Data;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

public class ErrorObject extends BuiltinFunctionObject {
    private static final long serialVersionUID = 866651721781282211L;

    private ErrorObject(ESObject functionPrototype, Evaluator evaluator) throws EcmaScriptException {
        super(functionPrototype, evaluator, "Error", 1);
        ErrorPrototype errorPrototype = new ErrorPrototype(evaluator);
        errorPrototype.putProperty("constructor", this, "constructor".hashCode());
        errorPrototype.putProperty("name", ESString.valueOf("Error"), "name".hashCode());
        errorPrototype.putProperty("message", ESString.valueOf(""), "message".hashCode());
        putHiddenProperty("prototype",errorPrototype);
    }

    @Override
    public String getESClassName() {
        return "Error";
    }
    
    @Override
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        return doConstruct(thisObject.toESObject(getEvaluator()),arguments);
    }
    
    @Override
    public ESObject doConstruct(ESObject thisObject, final ESValue[] arguments)
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
        return new ErrorObject(functionPrototype,evaluator);
    }

    public ErrorPrototype getPrototypeProperty() throws EcmaScriptException {
        return (ErrorPrototype) getProperty("prototype","prototype".hashCode());
    }


}
