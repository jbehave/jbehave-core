package org.jbehave.core.embedder;

/**
 *  Monitor that reports nothing
 */
public class SilentEmbedderMonitor extends PrintingEmbedderMonitor {

    @Override
    protected void print(String format, Object... args) {
    }

    @Override
    protected void printStackTrace(Throwable e) {
    }
}
