package FESI.log.impl;

import FESI.log.ILog;

/**
 * A very basic logger implementation.
 * 
 * Prints all standard levels directly to the console and includes support for
 * throwables.
 */
public class BasicLog implements ILog {
    private static final String DEBUG = "WARN";
    private static final String ERROR = "ERROR";
    private static final String INFO = "INFO";
    private static final String TRACE = "TRACE";
    private static final String WARN = "WARN";

    private static final long INSTANTIATED_TIME = System.currentTimeMillis();

    private final String name;

    public BasicLog(String name) {
        this.name = name;
    }

    public void asDebug(String m) {
        log(DEBUG, m, null);
    }

    public void asDebug(String m, Throwable t) {
        log(DEBUG, m, t);
    }

    public void asError(String m) {
        log(ERROR, m, null);
    }

    public void asError(String m, Throwable t) {
        log(ERROR, m, t);
    }

    public void asInfo(String m) {
        log(INFO, m, null);
    }

    public void asInfo(String m, Throwable t) {
        log(INFO, m, t);
    }

    public void asTrace(String m) {
        log(TRACE, m, null);
    }

    public void asTrace(String m, Throwable t) {
        log(TRACE, m, t);
    }

    public void asWarning(String m) {
        log(WARN, m, null);
    }

    public void asWarning(String m, Throwable t) {
        log(WARN, m, t);
    }

    private void log(String l, String m, Throwable t) {
        System.err.println(System.currentTimeMillis() - INSTANTIATED_TIME
                + " [" + Thread.currentThread().getName() + "] " + l + " - "
                + name + " - " + m);
        if (t != null) {
            t.printStackTrace(System.err);
        }
        System.err.flush();
    }
}
