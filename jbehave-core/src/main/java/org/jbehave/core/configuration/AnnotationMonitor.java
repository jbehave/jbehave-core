package org.jbehave.core.configuration;

public interface AnnotationMonitor {
	void processingFailed(Object pAnnotatedRunner, Throwable e);
}
