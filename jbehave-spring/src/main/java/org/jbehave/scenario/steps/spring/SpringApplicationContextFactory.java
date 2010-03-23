package org.jbehave.scenario.steps.spring;

import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 *  Simple facade to create a {@GenericApplicationContext} using an
 *  injected {@link ClassLoader}. 
 */
public class SpringApplicationContextFactory {

    private GenericApplicationContext context;

    public SpringApplicationContextFactory(String... resourceLocations) {
        this(Thread.currentThread().getContextClassLoader(), resourceLocations);
    }

    public SpringApplicationContextFactory(ClassLoader classLoader, String... resourceLocations) {
        this(null, classLoader, resourceLocations);
    }

    public SpringApplicationContextFactory(ApplicationContext parent, String... resourceLocations) {
        this(parent, parent.getClassLoader(), resourceLocations);
    }

    public SpringApplicationContextFactory(ApplicationContext parent, ClassLoader classLoader, String... resourceLocations) {
        // set up the context class loader
        Thread.currentThread().setContextClassLoader(classLoader);

        // create application context
        context = new GenericApplicationContext(parent);
        ResourceLoader resourceLoader = new DefaultResourceLoader(classLoader);
        context.setResourceLoader(resourceLoader);
        BeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
        for (String resourceLocation : resourceLocations) {
            reader.loadBeanDefinitions(resourceLoader.getResource(resourceLocation));
        }
        context.refresh();
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return context;
    }

}
