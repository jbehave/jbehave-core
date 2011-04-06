package org.jbehave.core.failures;

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
        super(format("Method {0}, annotated with {1}, failed", method, asList(method.getAnnotations())), cause);
    }

    public BeforeOrAfterFailed(Throwable cause) {
        super(cause);
    }
}
