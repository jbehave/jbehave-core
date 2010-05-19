package org.jbehave.core.runner;

/**
 *  Monitor that reports nothing
 */
public class SilentRunnerMonitor extends PrintStreamRunnerMonitor {

	protected void print(String message) {
	}

	protected void printStackTrace(Throwable e) {
	}

}
