package org.jbehave.core.junit.weld;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.weld.WeldAnnotationBuilder;
import org.jbehave.core.configuration.weld.WeldBootstrap;
import org.jbehave.core.junit.AnnotatedPathRunner;
import org.junit.runners.model.InitializationError;

/**
 * AnnotatedPathRunner that uses {@link WeldAnnotationBuilder}.
 * 
 * @author Aaron Walker
 */
public class WeldAnnotatedPathRunner extends AnnotatedPathRunner {

    private WeldBootstrap container;

    public WeldAnnotatedPathRunner(Class<?> testClass) throws InitializationError {
        super(testClass);

    }

    public AnnotationBuilder annotationBuilder() {
        if (container == null) {
            container = new WeldBootstrap();
            container.initialize();
        }
        return container.findAnnotationBuilder(testClass());
    }
}
