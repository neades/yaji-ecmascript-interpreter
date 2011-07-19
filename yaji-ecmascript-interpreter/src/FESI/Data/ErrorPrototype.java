package FESI.Data;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

public class ErrorPrototype extends ESObject {
    private static final long serialVersionUID = 5169400913129240083L;
    
    public ErrorPrototype(Evaluator evaluator) throws EcmaScriptException {
        super(evaluator.getObjectPrototype(),evaluator);
        
        putHiddenProperty("toString", new BuiltinFunctionObject(evaluator.getFunctionPrototype(),evaluator,"toString",1) {
            private static final long serialVersionUID = 7306694754871874677L;
            @Override
            public ESValue callFunction(ESValue thisValue, ESValue[] arguments)
                    throws EcmaScriptException {
                ESObject thisObject = thisValue.toESObject(getEvaluator());
                ESValue esmessage = thisObject.getProperty("message","message".hashCode());
                if (esmessage.getTypeOf() == EStypeUndefined) {
                    return esmessage;
                }
                ESValue esname = thisObject.getProperty("name","name".hashCode());
                String name = (esname.getTypeOf() == EStypeUndefined)
                                ? "Error"
                                : esname.toString();
                return new ESString(name + ": " + esmessage.toString());
            }
        });
    }

    
}
