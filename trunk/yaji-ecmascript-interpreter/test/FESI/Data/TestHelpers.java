package FESI.Data;

import java.util.ArrayList;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

public class TestHelpers {

    static ESValue createFunction(Evaluator evaluator, String... params) throws EcmaScriptException {
        ESObject functionObject = (ESObject) evaluator.getGlobalObject().getProperty("Function", "Function".hashCode());
        ArrayList<ESValue> paramArray = new ArrayList<ESValue>();
        for (String string : params) {
            paramArray.add(new ESString(string));
        }
        ESObject function = functionObject.doConstruct(paramArray.toArray(new ESValue[paramArray.size()]));
        return function;
    }

}
