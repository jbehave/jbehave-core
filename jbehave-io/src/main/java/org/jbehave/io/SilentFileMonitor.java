package org.jbehave.io;

public class SilentFileMonitor extends PrintStreamFileMonitor {

    @Override
    protected void print(String format, Object... args) {
        // print nothing
    }

    @Override
    protected void printStackTrace(Throwable e) {
        // print nothing
    }
}
