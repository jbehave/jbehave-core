package org.jbehave.core.junit;

import org.jbehave.core.configuration.AnnotationBuilder;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * A JUnit {@link Runner} that uses the {@link AnnotationBuilder} to 
 * create an embeddable test instance.  
 */
public class AnnotatedEmbedderRunner extends BlockJUnit4ClassRunner {

    public AnnotatedEmbedderRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    public Object createTest() {
        return annotationBuilder().embeddableInstance();
    }

    public AnnotationBuilder annotationBuilder() {
        return new AnnotationBuilder(testClass());
    }

    protected Class<?> testClass() {
        return getTestClass().getJavaClass();
    }

}
