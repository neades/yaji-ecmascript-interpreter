package org.yaji.log.impl;

import org.yaji.log.ILogFactory;

/**
 * The binding of the YAJI ILogFactory interface to a host implementation needs
 * to occur here.
 */
public class LogBinder {
    public static final LogBinder INSTANCE = new LogBinder();

    private final ILogFactory logFactory;

    private LogBinder() {
        logFactory = new BasicLogFactory();
    }

    public ILogFactory getLogFactory() {
        return logFactory;
    }
}
