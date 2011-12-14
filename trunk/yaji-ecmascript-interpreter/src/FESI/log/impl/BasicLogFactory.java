package FESI.log.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import FESI.log.ILog;
import FESI.log.ILogFactory;

/**
 * A very basic ILogFactory implementation for storing and returning ILog
 * instances
 */
public class BasicLogFactory implements ILogFactory {
    private Map<String, ILog> logs;

    public BasicLogFactory() {
        logs = new ConcurrentHashMap<String, ILog>();
    }

    public ILog getLog(String name) {
        ILog log = logs.get(name);
        if (log == null) {
            log = new BasicLog(name);
            logs.put(name, log);
        }
        return log;
    }
    
    public void setLog(String name, ILog log) {
        logs.put(name, log);
    }
}
