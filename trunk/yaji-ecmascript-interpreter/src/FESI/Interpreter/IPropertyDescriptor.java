package FESI.Interpreter;

import FESI.Data.ESValue;

public interface IPropertyDescriptor {

    public boolean isConfigurable();

    public boolean isWritable();

    public boolean isEnumerable();

    public ESValue getValue();

    public String getName();

}
