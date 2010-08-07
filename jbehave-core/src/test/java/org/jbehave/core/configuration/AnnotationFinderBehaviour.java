package org.jbehave.core.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

import org.junit.Test;

public class AnnotationFinderBehaviour {

    @Test
    public void shouldFindIfAnnotationIsPresent(){
        AnnotationFinder annotated = new AnnotationFinder(Annotated.class);
        assertThat(annotated.isAnnotationPresent(MyAnnotationWithoutMembers.class), is(true));        
    }

    @Test
    public void shouldFindIfAnnotationValueIsPresent(){
        AnnotationFinder annotated = new AnnotationFinder(Annotated.class);
        assertThat(annotated.isAnnotationValuePresent(MyAnnotationWithoutMembers.class, "flag"), is(false));        
        assertThat(annotated.isAnnotationValuePresent(MyAnnotationWithMembers.class, "flag"), is(true));        
        AnnotationFinder notAnnotated = new AnnotationFinder(NotAnnotated.class);
        assertThat(notAnnotated.isAnnotationValuePresent(MyAnnotationWithoutMembers.class, "flag"), is(false));        
    }

    @Test(expected=AnnotationRequired.class)
    public void shouldFailIfAnnotationIsNotFound(){
        AnnotationFinder notAnnotated = new AnnotationFinder(NotAnnotated.class);
        assertThat(notAnnotated.getAnnotatedValue(MyAnnotationWithMembers.class, boolean.class, "flag"), equalTo(false));        
    }

    @Test
    public void shouldInheritValuesIfInheritFlagNotPresent(){
        AnnotationFinder inheritingAnnotated = new AnnotationFinder(InheritingAnnotated.class);
        assertThat(inheritingAnnotated.getAnnotatedValues(MyAnnotationWithMembers.class, String.class, "values"), equalTo(Arrays.asList("2", "3", "1")));        
    }

    @Test
    public void shouldNotInheritValuesIfInheritFlagIsFalse(){
        AnnotationFinder inheritingAnnotated = new AnnotationFinder(InheritingAnnotated.class);
        assertThat(inheritingAnnotated.getAnnotatedValues(MyAnnotationWithMembers.class, String.class, "values"), equalTo(Arrays.asList("2", "3", "1")));        
    }

    @MyAnnotationWithoutMembers()
    @MyAnnotationWithMembers(flag = true, values = {"1"})
    static class Annotated {

    }
    
    @MyAnnotationWithMembers(flag = false, values = {"2", "3"})
    static class InheritingAnnotated extends Annotated{

    }

    @MyAnnotationWithMembers(flag = false, values = {"2", "3"}, inheritValues = false)
    static class NotInheritingAnnotated extends Annotated{

    }

    static class NotAnnotated {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE })
    @Inherited
    public @interface MyAnnotationWithMembers {
        boolean flag();
        String[] values() default {};
        boolean inheritValues() default true;
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE })
    @Inherited
    public @interface MyAnnotationWithoutMembers {
        
    }
}
