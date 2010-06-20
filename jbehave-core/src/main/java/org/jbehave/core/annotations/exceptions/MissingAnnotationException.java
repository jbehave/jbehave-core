package org.jbehave.core.annotations.exceptions;

import java.lang.annotation.Annotation;

@SuppressWarnings("serial")
/**
 * Used when an annotation is required and is not present or one of your property members.
 */
public class MissingAnnotationException extends RuntimeException {

	/**
	 * 
	 * @param pAnnotation - The annotation missed.
	 * @param pClass - The target class for the annotation.
	 */
	public MissingAnnotationException(Class<? extends Annotation> pAnnotation,
			Class<?> pClass) {
		super(pClass + " requires to be annotated by " + pAnnotation + ".");
	}

	/**
	 * Used when some member is required and it is not present.
	 * @param pAnnotation
	 * @param pMemberName
	 */
	public MissingAnnotationException(Class<? extends Annotation> pAnnotation, String pMemberName) {
		super(pAnnotation + " doesn't have the necessary member named '" + pMemberName + "'.");
	}

	/**
	 * User when there are two options and none of them was informed.
	 * @param pAnnotation1
	 * @param pAnnotation2
	 * @param pClass
	 */
	public MissingAnnotationException(Class<? extends Annotation> pAnnotation1,
			Class<? extends Annotation> pAnnotation2, Class<?> pClass) {
		super(pClass + " requires to be annotated by " + pAnnotation1 + " or "
				+ pAnnotation2 + ".");
	}

}