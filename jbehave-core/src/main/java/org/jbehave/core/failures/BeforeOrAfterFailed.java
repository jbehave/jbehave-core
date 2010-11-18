package org.jbehave.core.failures;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static java.text.MessageFormat.format;
import static java.util.Arrays.asList;

/**
 * Thrown when methods with before or after annotations (story or scenario)
 * fail.
 */
@SuppressWarnings("serial")
public class BeforeOrAfterFailed extends RuntimeException {

	public BeforeOrAfterFailed(Method method, Throwable cause) {
		super(format("Method {0}, annotated with {1}, failed", method,
				asList(getAnnotations(method))), cause);
	}

    private static Annotation[] getAnnotations(Method method) {
        Annotation[] annotations = method.getAnnotations();
        if (annotations == null) {
            annotations = new Annotation[0];
        }
        return annotations;
    }

    public BeforeOrAfterFailed(Throwable cause) {
        super(cause);
    }
}
