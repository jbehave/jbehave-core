package org.jbehave.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

/**
 * Indicates that the step should be performed only if the specified condition is met. A <em>condition</em>
 * is a state that can be determined programmatically before the step is performed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Conditional {

    /**
     * Defines the class of condition that will be used for step matching.
     * 
     * @return the Predicate implementation class.
     */
    Class<? extends Predicate<Object>> condition();

    /**
     * Defines the value to match the condition against.
     * 
     * @return the String value, empty by default.
     */
    String value() default "";
}
