package org.jbehave.core.errors;

import static java.text.MessageFormat.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Thrown when methods with before or after annotations (story or core) fail.
 */
@SuppressWarnings("serial")
public class BeforeOrAfterException extends RuntimeException {

	private static final String MESSAGE = "Method {0}.{1}, annotated with {2} failed.";

	public BeforeOrAfterException(
			Class<? extends Annotation> annotation, Method method, Throwable t) {
		super(format(MESSAGE, method.getClass().getSimpleName(),
				method.getName(), annotation.getSimpleName()), t);
	}
}
