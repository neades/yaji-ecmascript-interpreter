package FESI.Interpreter;

import FESI.Data.ESValue;
import FESI.Exceptions.EcmaScriptException;

public interface IDescriptor {

    ESValue getPropertyIfAvailable(String propertyName,int hashCode) throws EcmaScriptException;

}
