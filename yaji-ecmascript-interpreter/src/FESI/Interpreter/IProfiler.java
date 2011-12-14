/*
 * Created on Mar 3, 2004
 */
package FESI.Interpreter;

/**
 * @author roryg
 */
public interface IProfiler {
    public void startProfiling();

    public void endProfiling();
    
    public void writeHeader();
    public void writeFooter();
}
