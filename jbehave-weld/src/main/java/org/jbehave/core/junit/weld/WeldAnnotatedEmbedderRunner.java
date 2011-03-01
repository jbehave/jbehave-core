package org.jbehave.core.junit.weld;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.weld.WeldAnnotationBuilder;
import org.jbehave.core.configuration.weld.WeldBootstrap;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.runners.model.InitializationError;

/**
 * AnnotatedEmbedderRunner that uses {@link WeldAnnotationBuilder}.
 * 
 * @author Aaron Walker
 */
public class WeldAnnotatedEmbedderRunner extends AnnotatedEmbedderRunner {
    private WeldAnnotationBuilder annotationBuilder;
    private WeldBootstrap container;

    public WeldAnnotatedEmbedderRunner(Class<?> testClass) throws InitializationError {
        super(testClass);

        container = new WeldBootstrap();
        container.initialize();

        this.annotationBuilder = container.findAnnotationBuilder(testClass());
    }

    public AnnotationBuilder annotationBuilder() {
        return annotationBuilder;
    }
}
