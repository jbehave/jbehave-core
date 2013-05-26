package org.jbehave.examples.core.spring;

import org.jbehave.core.steps.spring.SpringApplicationContextFactory;
import org.springframework.context.ApplicationContext;

/**
 * Using Spring's annotation configuration
 */
public class CoreStoriesUsingSpringWithAnnotationConfiguration extends CoreStoriesUsingSpring {

    protected ApplicationContext createContext() {
        return new SpringApplicationContextFactory("org.jbehave.examples.core.spring.SpringAnnotationConfiguration")
                .createApplicationContext();
    }


}
