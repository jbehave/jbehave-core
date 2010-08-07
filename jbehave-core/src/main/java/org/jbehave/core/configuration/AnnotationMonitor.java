package org.jbehave.core.configuration;

import java.lang.annotation.Annotation;

public interface AnnotationMonitor {

    void elementCreationFailed(Class<?> elementClass, Exception cause);

    void annotationNotFound(Class<? extends Annotation> annotation, Object annotatedInstance);

}
