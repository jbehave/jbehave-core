package org.jbehave.core.failures;

import static java.text.MessageFormat.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Thrown when methods with before or after annotations (story or scenario) fail.
 */
@SuppressWarnings("serial")
public class BeforeOrAfterFailed extends RuntimeException {

	private static final String MESSAGE = "Method {0}.{1}, annotated with {2} failed.";

	public BeforeOrAfterFailed(
			Class<? extends Annotation> annotation, Method method, Throwable cause) {
		super(format(MESSAGE, method.getClass().getSimpleName(),
				method.getName(), annotation.getSimpleName()), cause);
	}
}
