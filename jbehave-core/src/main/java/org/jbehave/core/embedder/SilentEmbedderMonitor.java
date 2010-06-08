package org.jbehave.core.embedder;

/**
 *  Monitor that reports nothing
 */
public class SilentEmbedderMonitor extends PrintStreamEmbedderMonitor {

	protected void print(String message) {
	}

	protected void printStackTrace(Throwable e) {
	}

}
