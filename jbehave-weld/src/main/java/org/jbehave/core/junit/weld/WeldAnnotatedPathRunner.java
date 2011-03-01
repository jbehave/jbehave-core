/**
 * 
 */
package org.jbehave.core.junit.weld;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.weld.WeldAnnotationBuilder;
import org.jbehave.core.configuration.weld.WeldBootstrap;
import org.jbehave.core.junit.AnnotatedPathRunner;
import org.junit.runners.model.InitializationError;

/**
 * AnnotatedPathRunner that uses {@link WeldAnnotationBuilder}.
 * 
 * @author aaronwalker
 *
 */
public class WeldAnnotatedPathRunner extends AnnotatedPathRunner {
    
    private WeldBootstrap container;

    public WeldAnnotatedPathRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        
        container = new WeldBootstrap();
        container.initialize();
    }

    public AnnotationBuilder annotationBuilder() {
        return container.findAnnotationBuilder(testClass());
    }
}
