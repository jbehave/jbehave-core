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
 * Factory for Spring {@link ApplicationContext} using the specified resources.
 * The resources can be expressed as:
 * <ol>
 * <li>Annotated class names</li>
 * <li>XML location paths</li>
 * </ol>
 * The context will be an instance of {@link AnnotationConfigApplicationContext}, 
 * if the resources are annotated class names, or
 * {@link GenericApplicationContext} otherwise.
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

    /**
     * Creates a configurable application context from the resources provided.
     * The context will be an instance of
     * {@link AnnotationConfigApplicationContext}, if the resources are
     * annotated class names, or {@link GenericApplicationContext} otherwise.
     * 
     * @return A ConfigurableApplicationContext
     */
    public ConfigurableApplicationContext createApplicationContext() {
        try {
            // first try to create annotation config application context
            Class<?>[] annotatedClasses = new Class<?>[resources.length];
            for (int i = 0; i < resources.length; i++) {
                annotatedClasses[i] = this.classLoader.loadClass(resources[i]);
            }
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(annotatedClasses);
            context.setParent(parent);
            context.setClassLoader(classLoader);
            return context;
        } catch (ClassNotFoundException e) {
            // create generic application context
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
