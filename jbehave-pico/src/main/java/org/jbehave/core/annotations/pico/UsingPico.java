package org.jbehave.core.annotations.pico;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jbehave.core.configuration.pico.PicoModule;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.PicoContainer;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface UsingPico {

    Class<? extends PicoContainer> container() default DefaultPicoContainer.class;
    
	Class<? extends PicoModule>[] modules() default {};

}
