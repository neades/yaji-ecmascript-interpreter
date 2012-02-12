package org.yaji.log;

/**
 * YAJI log interface
 */
public interface ILog {

    void asDebug(String m);

    void asDebug(String m, Throwable t);

    void asError(String m);

    void asError(String m, Throwable t);

    void asInfo(String m);

    void asInfo(String m, Throwable t);

    void asTrace(String m);

    void asTrace(String m, Throwable t);

    void asWarning(String m);

    void asWarning(String m, Throwable t);

    void print(String m);
    
    void println();
}
