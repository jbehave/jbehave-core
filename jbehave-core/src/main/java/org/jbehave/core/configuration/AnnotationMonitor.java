package org.jbehave.core.configuration;

public interface AnnotationMonitor {

    void annotatedElementInvalid(Class<?> elementClass, Exception cause);

}
