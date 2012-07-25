package org.jbehave.examples.trader.spring;

import org.jbehave.core.steps.spring.SpringApplicationContextFactory;
import org.springframework.context.ApplicationContext;

/**
 * Using Spring's annotation configuration
 */
public class TraderStoriesUsingSpringWithAnnotationConfiguration extends TraderStoriesUsingSpring {

    protected ApplicationContext createContext() {
        return new SpringApplicationContextFactory("org.jbehave.examples.trader.spring.SpringAnnotationConfiguration")
                .createApplicationContext();
    }


}
