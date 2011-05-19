/*
 * Created on 28-Jan-2004
 */
package FESI.Interpreter;

/**
 * @author roryg
 */
public interface IProcedureProfiler extends IProfiler {

    public Object[] startProcedure(String className, String functionName);

    public void endProcedure(String className, String functionName,
            Object[] start, long endTime);

    public void addElement(String message);

    public void endProfiling();

}
