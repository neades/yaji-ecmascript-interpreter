package org.yaji.log.impl;

import org.yaji.log.AbstractLog;

/**
 * A very basic logger implementation.
 * 
 * Prints all standard levels directly to the console and includes support for
 * throwables.
 */
public class BasicLog extends AbstractLog {
    private static final long INSTANTIATED_TIME = System.currentTimeMillis();

    private final String name;

    public BasicLog(String name) {
        this.name = name;
    }

    public void print(String m) {
        System.out.print(m);
    }
    
    public void println() {
        System.out.println();
    }
    @Override
    protected void log(String l, String m, Throwable t) {
        System.err.println(System.currentTimeMillis() - INSTANTIATED_TIME
                + " [" + Thread.currentThread().getName() + "] " + l + " - "
                + name + " - " + m);
        if (t != null) {
            t.printStackTrace(System.err);
        }
        System.err.flush();
    }
}
