package FESI.Data;

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

}
