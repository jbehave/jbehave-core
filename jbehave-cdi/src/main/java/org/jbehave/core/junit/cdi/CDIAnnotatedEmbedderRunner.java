/**
 * 
 */
package org.jbehave.core.junit.cdi;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.cdi.CDIAnnotationBuilder;
import org.jbehave.core.configuration.cdi.CDIBootstrap;
import org.jbehave.core.junit.AnnotatedEmbedderRunner;
import org.junit.runners.model.InitializationError;

/**
 * AnnotatedEmbedderRunner that uses {@link CDIAnnotationBuilder}.
 * 
 * @author aaronwalker
 *
 */
public class CDIAnnotatedEmbedderRunner extends AnnotatedEmbedderRunner {
    private CDIAnnotationBuilder annotationBuilder;
    private CDIBootstrap container;

    public CDIAnnotatedEmbedderRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        
        container = new CDIBootstrap();
        container.initialize();
        
        this.annotationBuilder = container.findCDIAnnotationBuilder(testClass());
    }

    public AnnotationBuilder annotationBuilder() {
        return annotationBuilder;
    }
}
