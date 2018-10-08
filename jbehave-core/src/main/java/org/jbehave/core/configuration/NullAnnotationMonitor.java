package org.jbehave.core.configuration;

import java.lang.annotation.Annotation;

/**
 * <a href="http://en.wikipedia.org/wiki/Null_Object_pattern">Null Object
 * Pattern</a> implementation of {@link AnnotationMonitor}. Can be extended to
 * override only the methods of interest.
 */
public class NullAnnotationMonitor implements AnnotationMonitor {

    @Override
    public void elementCreationFailed(Class<?> elementClass, Exception cause) {
    }

    @Override
    public void annotationNotFound(Class<? extends Annotation> annotation, Object annotatedInstance) {
    }

}
