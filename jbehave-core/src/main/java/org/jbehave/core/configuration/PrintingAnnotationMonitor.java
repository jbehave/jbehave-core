package org.jbehave.core.configuration;

import java.lang.annotation.Annotation;

public abstract class PrintingAnnotationMonitor implements AnnotationMonitor {

    @Override
    public void elementCreationFailed(Class<?> elementClass, Exception cause) {
        print("Element creation failed: %s", elementClass);
        printStackTrace(cause);
    }

    @Override
    public void annotationNotFound(Class<? extends Annotation> annotation, Object annotatedInstance) {
        print("Annotation %s not found in %s", annotation, annotatedInstance);
    }

    protected abstract void print(String format, Object... args);

    protected abstract void printStackTrace(Throwable e);
}
