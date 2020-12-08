package org.jbehave.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface AfterStory {

    boolean uponGivenStory() default false;

    /**
     * Lifecycle hooks with the higher order will be executed last
     *
     * @return order value
     */
    int order() default 0;
}
