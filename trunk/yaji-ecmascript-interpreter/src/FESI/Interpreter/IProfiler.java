/*
 * Created on Mar 3, 2004
 */
package FESI.Interpreter;

import java.io.OutputStream;

/**
 * @author roryg
 */
public interface IProfiler {
    public void startProfiling();

    public void endProfiling();

    public void writeHeader(long thisProfileLog, OutputStream profileLog);

    public void writeFooter(OutputStream profileLog);
}
