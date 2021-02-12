package org.jbehave.core.annotations.needle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.needle4j.injection.InjectionProvider;

/**
 * Annotation to mark InjectionProviders in the JBehave steps. <br/>
 * Should be placed on fields of type {@link InjectionProvider} or an array of those.
 * 
 * @author Jan Galinski, Holisticon AG (jan.galinski@holisticon.de)
 * @author Simon Zambrovski, Holisticon AG (simon.zambrovski@holisticon.de)
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedleInjectionProvider {
    // Nothing here
}
