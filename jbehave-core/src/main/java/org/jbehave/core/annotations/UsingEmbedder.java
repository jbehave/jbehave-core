package org.jbehave.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jbehave.core.embedder.Embedder;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface UsingEmbedder {

    Class<?> embedder() default Embedder.class;
    boolean batch()  default false;
	boolean skip()  default false;
	boolean generateViewAfterStories()  default true;
	boolean ignoreFailureInStories()  default false;
	boolean ignoreFailureInView()  default false;
    long storyTimeoutInSecs() default 300;
	int threads() default 1;
	boolean stepsFactory() default true;
    String[] metaFilters() default {};
    String systemProperties() default "";

}
