package org.jbehave.core.annotations;

import static org.jbehave.core.annotations.ContextOutcome.RetentionLevel.EXAMPLE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ContextOutcome {

    enum RetentionLevel {
        STORY, SCENARIO, EXAMPLE
    }

    String value();

    RetentionLevel retentionLevel() default EXAMPLE;
}