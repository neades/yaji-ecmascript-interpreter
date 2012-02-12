package org.yaji.log;

import org.yaji.log.impl.LogBinder;

/**
 * A utility class used for log access throughout YAJI.
 * 
 * It is implementation agnostic, wrapping an ILogFactory instance which must be
 * bound at compile time.
 */
public final class Logs {
    public static ILog getLog(String s) {
        ILogFactory factory = getLogFactory();
        return factory.getLog(s);
    }

    public static ILog getLog(Class<?> c) {
        return getLog(c.getName());
    }

    private static ILogFactory getLogFactory() {
        return LogBinder.INSTANCE.getLogFactory();
    }
}
