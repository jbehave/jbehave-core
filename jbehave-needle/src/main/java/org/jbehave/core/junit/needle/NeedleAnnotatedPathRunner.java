package org.jbehave.core.junit.needle;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.needle.NeedleAnnotationBuilder;
import org.jbehave.core.junit.AnnotatedPathRunner;
import org.junit.runners.model.InitializationError;

/**
 * AnnotatedPathRunner that uses {@link NeedleAnnotationBuilder}.
 */
public class NeedleAnnotatedPathRunner extends AnnotatedPathRunner {

    public NeedleAnnotatedPathRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    public AnnotationBuilder annotationBuilder() {
        return new NeedleAnnotationBuilder(testClass());
    }

}
