package org.jbehave.core.configuration.guice;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class GuiceJUnit4ClassRunner extends BlockJUnit4ClassRunner {

    public GuiceJUnit4ClassRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    public Object createTest() {
        Class<?> testClass = getTestClass().getJavaClass();
        try {
            return new GuiceAnnotationBuilder(testClass).instanceWithInjectedEmbedder();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
