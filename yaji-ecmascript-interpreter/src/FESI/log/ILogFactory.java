package FESI.log;

/**
 * Instances of ILogFactory should create new or return existing ILog instances
 */
public interface ILogFactory {
    /**
     * Get a log by name
     * 
     * @param s the name of the log to return
     */
    public ILog getLog(String s);
}
