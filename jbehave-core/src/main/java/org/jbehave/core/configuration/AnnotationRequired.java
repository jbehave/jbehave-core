package org.jbehave.core.configuration;

import java.lang.annotation.Annotation;

/**
 * Thrown when an annotation is required and not found.
 */
@SuppressWarnings("serial")
public class AnnotationRequired extends RuntimeException {

    /**
     * Used when an annotation is required and not found
     * 
     * @param annotatedClass the annotated Class 
     * @param missingAnnotation the missing Annotation class
     */
    public AnnotationRequired(Class<?> annotatedClass,
            Class<? extends Annotation> missingAnnotation) {
        super(annotatedClass + " requires to be annotated by " + missingAnnotation + ".");
    }

}