package FESI.log;

public abstract class AbstractLog implements ILog {

    protected abstract void log(String l, String m, Throwable t);

    private static final String DEBUG = "WARN";
    private static final String ERROR = "ERROR";
    private static final String INFO = "INFO";
    private static final String TRACE = "TRACE";
    private static final String WARN = "WARN";

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

}
