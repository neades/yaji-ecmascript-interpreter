package FESI.Data;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

public class NativeErrorObject extends BuiltinFunctionObject {
    private static final long serialVersionUID = 866651721781282211L;
    public static final String REFERENCE_ERROR = "ReferenceError";
    public static final String URI_ERROR = "URIError";
    public static final String TYPE_ERROR = "TypeError";
    public static final String SYNTAX_ERROR = "SyntaxError";
    public static final String EVAL_ERROR = "EvalError";
    public static final String RANGE_ERROR = "RangeError";

    private NativeErrorObject(ESObject functionPrototype, Evaluator evaluator, String constructorName, ErrorPrototype errorPrototype) throws EcmaScriptException {
        super(functionPrototype, evaluator, constructorName, 1);
        ESObject nativeErrorPrototype = new ESObject(errorPrototype,evaluator){
            private static final long serialVersionUID = 2254948851727918335L;
        };
        nativeErrorPrototype.putProperty("constructor", this, "constructor".hashCode());
        nativeErrorPrototype.putProperty("name", ESString.valueOf(constructorName), "name".hashCode());
        nativeErrorPrototype.putProperty("message", ESString.valueOf(""), "message".hashCode());
        putHiddenProperty("prototype",nativeErrorPrototype);
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
    public static NativeErrorObject make(String name,Evaluator evaluator,
            ErrorPrototype errorPrototype, FunctionPrototype functionPrototype) throws EcmaScriptException {
        return new NativeErrorObject(functionPrototype,evaluator,name,errorPrototype);
    }

    public ESObject getPrototypeProperty() throws EcmaScriptException {
        return (ESObject) getProperty("prototype","prototype".hashCode());
    }

}
