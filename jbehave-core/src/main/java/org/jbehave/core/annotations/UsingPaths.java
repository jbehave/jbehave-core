package org.jbehave.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jbehave.core.io.StoryFinder;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface UsingPaths {

    String searchIn();
	String[] includes() default {"**/*.story"};
    String[] excludes() default {};
    Class<? extends StoryFinder> storyFinder() default StoryFinder.class;

}
