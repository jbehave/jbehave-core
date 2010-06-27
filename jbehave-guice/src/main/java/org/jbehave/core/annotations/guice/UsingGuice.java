package org.jbehave.core.annotations.guice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jbehave.core.annotations.WithEmbedderControls;
import org.jbehave.core.embedder.Embedder;

import com.google.inject.Module;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited

public @interface UsingGuice {
	
	Class<? extends Module>[] modules() default {};
	Class<?> embedder() default Embedder.class;
	WithEmbedderControls embedderControls() default @WithEmbedderControls;
}
