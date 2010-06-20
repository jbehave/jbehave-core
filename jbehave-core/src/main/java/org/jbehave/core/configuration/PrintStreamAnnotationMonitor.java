package org.jbehave.core.configuration;

import java.io.PrintStream;

public class PrintStreamAnnotationMonitor implements AnnotationMonitor {

	private PrintStream output = System.out;
	public PrintStreamAnnotationMonitor() {
		super();
	}
    public PrintStreamAnnotationMonitor(PrintStream output) {
        this.output = output;
    }

	public void processingFailed(Object pAnnotatedRunner, Throwable e) {
		output.println("Failed to process annotations in " + pAnnotatedRunner.getClass() + ". /n");
		e.printStackTrace(output);
		
	}
}
