package org.jbehave.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
@Documented
public @interface UsingSteps {

    Class<?>[] instances() default {};

    String[] packages() default {};

    String matchingNames() default ".*";
    
    String notMatchingNames() default "";
    
    boolean inheritInstances() default true;
    
}
