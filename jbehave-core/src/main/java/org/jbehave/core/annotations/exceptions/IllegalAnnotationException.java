
package org.jbehave.core.annotations.exceptions;

import java.lang.annotation.Annotation;

@SuppressWarnings("serial")
public class IllegalAnnotationException extends RuntimeException {

	public IllegalAnnotationException(Class<? extends Annotation> annotation, String reason) {
		super("The annotation " + annotation + " cannot be used in this test: " + reason);
	}
}
