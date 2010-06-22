package org.jbehave.core.steps.spring;

import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * Factory for Spring {@ApplicationContext} using the
 * specified resources
 */
public class SpringApplicationContextFactory {

    private final ApplicationContext parent;
    private final ClassLoader classLoader;
    private final String[] resourceLocations;

    public SpringApplicationContextFactory(String... resourceLocations) {
        this(Thread.currentThread().getContextClassLoader(), resourceLocations);
    }

    public SpringApplicationContextFactory(ClassLoader classLoader, String... resourceLocations) {
        this(null, classLoader, resourceLocations);
    }

    public SpringApplicationContextFactory(ApplicationContext parent, String... resourceLocations) {
        this(parent, parent.getClassLoader(), resourceLocations);
    }

    public SpringApplicationContextFactory(ApplicationContext parent, ClassLoader classLoader,
            String... resourceLocations) {
        this.parent = parent;
        this.classLoader = classLoader;
        this.resourceLocations = resourceLocations;
    }

    public ConfigurableApplicationContext createApplicationContext() {
        // create application context
        GenericApplicationContext context = new GenericApplicationContext(parent);
        ResourceLoader resourceLoader = new DefaultResourceLoader(classLoader);
        context.setResourceLoader(resourceLoader);
        BeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
        for (String resourceLocation : resourceLocations) {
            reader.loadBeanDefinitions(resourceLoader.getResource(resourceLocation));
        }
        context.refresh();
        return context;
    }

}
