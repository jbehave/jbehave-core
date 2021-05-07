package org.jbehave.core.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.junit.jupiter.api.Test;

class AnnotationFinderBehaviour {

    @Test
    void shouldFindIfAnnotationIsPresent() {
        AnnotationFinder annotated = new AnnotationFinder(Annotated.class);
        assertThat(annotated.isAnnotationPresent(MyAnnotationWithoutMembers.class), is(true));        
    }

    @Test
    void shouldFindIfAnnotationValueIsPresent() {
        AnnotationFinder annotated = new AnnotationFinder(Annotated.class);
        assertThat(annotated.isAnnotationValuePresent(MyAnnotationWithoutMembers.class, "flag"), is(false));        
        assertThat(annotated.isAnnotationValuePresent(MyAnnotationWithMembers.class, "flag"), is(true));        
        AnnotationFinder notAnnotated = new AnnotationFinder(NotAnnotated.class);
        assertThat(notAnnotated.isAnnotationValuePresent(MyAnnotationWithoutMembers.class, "flag"), is(false));        
    }

    @Test
    void shouldFailIfAnnotationIsNotFound() {
        AnnotationFinder notAnnotated = new AnnotationFinder(NotAnnotated.class);
        assertThrows(AnnotationRequired.class,
                () -> notAnnotated.getAnnotatedValue(MyAnnotationWithMembers.class, boolean.class, "flag"));
    }

    @Test
    void shouldInheritValues() {
        AnnotationFinder inheritingAnnotated = new AnnotationFinder(InheritingAnnotated.class);
        List<String> annotatedValues = inheritingAnnotated.getAnnotatedValues(MyAnnotationWithMembers.class, String.class, "values");
        assertThat(annotatedValues.size(), equalTo(3));
        assertThat(annotatedValues, hasItem("1"));        
        assertThat(annotatedValues, hasItem("2"));        
        assertThat(annotatedValues, hasItem("3"));        
    }

    @Test
    void shouldInheritValuesWithoutDuplicates() {
        AnnotationFinder inheritingAnnotated = new AnnotationFinder(InheritingAnnotatedWithDuplicates.class);
        List<String> annotatedValues = inheritingAnnotated.getAnnotatedValues(MyAnnotationWithMembers.class, String.class, "values");
        assertThat(annotatedValues.size(), equalTo(3));
        assertThat(annotatedValues, hasItem("1"));        
        assertThat(annotatedValues, hasItem("2"));        
        assertThat(annotatedValues, hasItem("3"));      
    }

    @Test
    void shouldNotInheritValuesIfInheritFlagIsFalse() {
        AnnotationFinder notInheritingAnnotated = new AnnotationFinder(NotInheritingAnnotated.class);
        List<String> annotatedValues = notInheritingAnnotated.getAnnotatedValues(MyAnnotationWithMembers.class, String.class, "values");
        assertThat(annotatedValues.size(), equalTo(2));
        assertThat(annotatedValues, hasItem("2"));        
        assertThat(annotatedValues, hasItem("3"));      
    }

    @MyAnnotationWithoutMembers()
    @MyAnnotationWithMembers(flag = true, values = {"1"})
    static class Annotated {

    }
    
    @MyAnnotationWithMembers(flag = false, values = {"2", "3"})
    static class InheritingAnnotated extends Annotated{

    }

    @MyAnnotationWithMembers(flag = false, values = {"1", "2", "3"})
    static class InheritingAnnotatedWithDuplicates extends Annotated{

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
