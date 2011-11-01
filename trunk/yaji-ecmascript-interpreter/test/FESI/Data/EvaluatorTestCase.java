package FESI.Data;

import java.util.ArrayList;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

public class EvaluatorTestCase {

    protected Evaluator evaluator;
    protected BuiltinFunctionObject arrayObject;
    protected BuiltinFunctionObject objectObject;
    protected BuiltinFunctionObject dateObject;
    protected GlobalObject globalObject;

    protected void setUp() throws Exception {
        evaluator = new Evaluator();
        globalObject = evaluator.getGlobalObject();

        objectObject = (BuiltinFunctionObject) globalObject.getProperty("Object","Object".hashCode());
        arrayObject = (BuiltinFunctionObject) globalObject.getProperty("Array","Array".hashCode());
        dateObject = (BuiltinFunctionObject) globalObject.getProperty("Date");
    }

    protected ESObject createFunction(String... params) throws EcmaScriptException {
        ESObject functionObject = (ESObject) evaluator.getGlobalObject().getProperty("Function", "Function".hashCode());
        ArrayList<ESValue> paramArray = new ArrayList<ESValue>();
        for (String string : params) {
            paramArray.add(new ESString(string));
        }
        return functionObject.doConstruct(paramArray.toArray(new ESValue[paramArray.size()]));
    }

}
