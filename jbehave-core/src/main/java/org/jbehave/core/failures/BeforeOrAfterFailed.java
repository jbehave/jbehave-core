package org.jbehave.core.failures;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import static java.text.MessageFormat.format;

/**
 * Thrown when methods, annotated with before or after annotations (story or scenario),
 * fail.
 */
@SuppressWarnings("serial")
public class BeforeOrAfterFailed extends RuntimeException {

    public BeforeOrAfterFailed(Method method, Throwable cause) {
        super(format("Method {0} (annotated with {1} in class {2}) failed: {3}", method.getName(), toAnnotationNames(method.getAnnotations()), method.getDeclaringClass().getName(), cause), cause);
    }

    private static String toAnnotationNames(Annotation[] annotations) {
        List<String> names = new ArrayList<String>();
        for (Annotation annotation : annotations) {
            names.add("@"+annotation.annotationType().getSimpleName());
        }
        return StringUtils.join(names, ",");
    }

    public BeforeOrAfterFailed(Throwable cause) {
        super(cause);
    }
}
