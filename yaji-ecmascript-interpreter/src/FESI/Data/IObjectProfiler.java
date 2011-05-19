/*
 * Created on Mar 3, 2004
 */
package FESI.Data;

import FESI.Interpreter.Evaluator;

/**
 * @author roryg
 */
public interface IObjectProfiler {

    /**
     * Write a profiling line to output.
     * 
     * @param evaluator
     * @param currentTime
     * @param object
     * @param context
     * @param state
     */
    public void write(Evaluator evaluator, long currentTime, ESObject object,
            Object context, String state);

    /**
     * Put in a marker
     */
    public void markHeap();

}
