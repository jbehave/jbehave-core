package org.jbehave.core.annotations.groovy;

import org.jbehave.core.configuration.groovy.GroovyResourceFinder;
import org.jbehave.core.configuration.groovy.JBehaveGroovyClassLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
@Inherited
public @interface UsingGroovy {

    Class<? extends JBehaveGroovyClassLoader> classLoader() default JBehaveGroovyClassLoader.class;

    Class<? extends GroovyResourceFinder> resourceFinder() default GroovyResourceFinder.class;

}
