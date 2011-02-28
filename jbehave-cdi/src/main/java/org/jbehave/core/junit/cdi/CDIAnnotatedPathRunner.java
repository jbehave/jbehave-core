/**
 * 
 */
package org.jbehave.core.junit.cdi;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.cdi.CDIAnnotationBuilder;
import org.jbehave.core.configuration.cdi.CDIBootstrap;
import org.jbehave.core.junit.AnnotatedPathRunner;
import org.junit.runners.model.InitializationError;

/**
 * AnnotatedPathRunner that uses {@link CDIAnnotationBuilder}.
 * 
 * @author aaronwalker
 *
 */
public class CDIAnnotatedPathRunner extends AnnotatedPathRunner {
    
    private CDIBootstrap container;

    public CDIAnnotatedPathRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        
        container = new CDIBootstrap();
        container.initialize();
    }

    public AnnotationBuilder annotationBuilder() {
        return container.findCDIAnnotationBuilder(testClass());
    }
}
