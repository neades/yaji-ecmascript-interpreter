/*
 * Created on Mar 3, 2004
 */
package FESI.Data;

import FESI.Interpreter.Evaluator;

/**
 * @author roryg
 */
public interface IObjectProfiler {

    public void write(Evaluator evaluator, long currentTime, ESObject object, Object context, String state);

    public void markHeap();

}
