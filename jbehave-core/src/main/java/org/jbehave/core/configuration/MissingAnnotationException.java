package org.jbehave.core.configuration;

import java.lang.annotation.Annotation;

@SuppressWarnings("serial")
/**
 * Thrown when an annotation or one of property members is required and is not found.
 */
public class MissingAnnotationException extends RuntimeException {

	/**
	 * Used when an annotation is required and not found
	 * 
	 * @param annotatedClass the annotated Class 
	 * @param missingAnnotation the missing Annotation class
	 */
	public MissingAnnotationException(Class<?> annotatedClass,
			Class<? extends Annotation> missingAnnotation) {
		super(annotatedClass + " requires to be annotated by " + missingAnnotation + ".");
	}

	/**
	 * Used when a member is required and not found
	 * 
     * @param annotation the Annotation class
	 * @param missingMemberName the missing member name
	 */
	public MissingAnnotationException(Class<? extends Annotation> annotation, String missingMemberName) {
		super(annotation + " requires member named '" + missingMemberName + "'.");
	}

}