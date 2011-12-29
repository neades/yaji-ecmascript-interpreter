package FESI.Data;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;

public class BoundFunctionPrototype extends FunctionPrototype {
    private static final long serialVersionUID = -564850200874893167L;
    private final ESObject target;
    private final ESValue boundThis;
    private final ESValue[] boundArgs;

    public BoundFunctionPrototype(ESObject functionPrototype,
            Evaluator evaluator, int length, ESObject target, ESValue boundThis,
            ESValue[] boundArgs) throws EcmaScriptException {
        super(functionPrototype, evaluator, length);
        this.target = target;
        this.boundThis = boundThis;
        this.boundArgs = boundArgs;
        putProperty(StandardProperty.CALLERstring, 0, ESUndefined.theUndefined );
        putProperty(StandardProperty.ARGUMENTSstring, 0, ESUndefined.theUndefined );
    }

    @Override
    public void putProperty(String propertyName, ESValue propertyValue, int hash) throws EcmaScriptException {
        if ((hash == StandardProperty.CALLERhash && propertyName.equals(StandardProperty.CALLERstring))
            || (hash == StandardProperty.ARGUMENTShash && propertyName.equals(StandardProperty.ARGUMENTSstring)) ) {
            throw new TypeError("Bound function does not allow setting of "+propertyName+" property");
        }
        super.putProperty(propertyName, propertyValue, hash);
    }

    @Override
    public ESValue getPropertyIfAvailable(String propertyName, int hash) throws EcmaScriptException {
        if ((hash == StandardProperty.CALLERhash && propertyName.equals(StandardProperty.CALLERstring))
                || (hash == StandardProperty.ARGUMENTShash && propertyName.equals(StandardProperty.ARGUMENTSstring)) ) {
            throw new TypeError("Bound function does not allow getting of "+propertyName+" property");
        }
        return super.getPropertyIfAvailable(propertyName, hash);
    }
    
    @Override
    public ESValue callFunction(ESValue thisObject, ESValue[] extraArguments)
            throws EcmaScriptException {
        return target.callFunction(boundThis, mergeArguments(extraArguments));
    }

    private ESValue[] mergeArguments(ESValue[] extraArguments) {
        ESValue[] arguments = new ESValue[boundArgs.length+extraArguments.length];
        System.arraycopy(boundArgs, 0, arguments, 0, boundArgs.length);
        System.arraycopy(extraArguments, 0, arguments, boundArgs.length, extraArguments.length);
        return arguments;
    }
    
    @Override
    public ESObject doConstruct(ESValue[] arguments) throws EcmaScriptException {
        return target.doConstruct(mergeArguments(arguments));
    }
    
    @Override
    public boolean hasInstance(ESValue v1) throws EcmaScriptException {
        return target.hasInstance(v1);
    }
}
