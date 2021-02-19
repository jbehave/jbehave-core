package org.jbehave.core.annotations.groovy;

import groovy.lang.GroovyClassLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jbehave.core.configuration.groovy.BytecodeGroovyClassLoader;
import org.jbehave.core.configuration.groovy.GroovyResourceFinder;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface UsingGroovy {

    Class<? extends GroovyClassLoader> classLoader() default BytecodeGroovyClassLoader.class;

    Class<? extends GroovyResourceFinder> resourceFinder() default GroovyResourceFinder.class;

}
