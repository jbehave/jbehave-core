package org.jbehave.core.configuration;

public interface AnnotationMonitor {

    void elementCreationFailed(Class<?> elementClass, Exception cause);

}
