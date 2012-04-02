package org.jbehave.core.steps.spring;

import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * Factory for Spring {@link ApplicationContext} using the specified resources
 */
public class SpringApplicationContextFactory {

    private final ApplicationContext parent;
    private final ClassLoader classLoader;
    private final String[] resources;

    public SpringApplicationContextFactory(String... resources) {

        this(SpringApplicationContextFactory.class.getClassLoader(), resources);
    }

    public SpringApplicationContextFactory(ClassLoader classLoader, String... resources) {

        this(null, classLoader, resources);
    }

    public SpringApplicationContextFactory(ApplicationContext parent, ClassLoader classLoader, String... resources) {

        this.parent = parent;
        this.classLoader = classLoader;
        this.resources = resources;
    }

    public ConfigurableApplicationContext createApplicationContext() {


        try {

            Class<?>[] classes = new Class<?>[resources.length];

            for (int i = 0; i < resources.length; i++) {
                classes[i] = this.classLoader.loadClass(resources[i]);
            }

            AnnotationConfigApplicationContext result = new AnnotationConfigApplicationContext(classes);
            result.setParent(parent);
            result.setClassLoader(classLoader);
            return result;
        } catch (ClassNotFoundException e) {
            // create application context
            GenericApplicationContext context = new GenericApplicationContext(parent);
            context.setClassLoader(classLoader);
            ResourceLoader resourceLoader = new DefaultResourceLoader(classLoader);
            context.setResourceLoader(resourceLoader);
            BeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
            for (String resource : resources) {
                reader.loadBeanDefinitions(resourceLoader.getResource(resource));
            }
            context.refresh();
            return context;
        }

    }

}
