package org.jbehave.core.configuration;

import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

    @Test
    public void shouldFailIfAnnotationIsNotFound(){
        try {
            AnnotationFinder notAnnotated = new AnnotationFinder(NotAnnotated.class);
            assertThat(notAnnotated.getAnnotatedValue(MyAnnotationWithMembers.class, boolean.class, "flag"), equalTo(false));
        } catch (Exception e) {
            assertThat(e, is(instanceOf(AnnotationRequired.class)));
        }
    }

    @Test
    public void shouldInheritValues(){
        AnnotationFinder inheritingAnnotated = new AnnotationFinder(InheritingAnnotated.class);
        List<String> annotatedValues = inheritingAnnotated.getAnnotatedValues(MyAnnotationWithMembers.class, String.class, "values");
        assertThat(annotatedValues.size(), equalTo(3));
        assertThat(annotatedValues, hasItem("1"));        
        assertThat(annotatedValues, hasItem("2"));        
        assertThat(annotatedValues, hasItem("3"));        
    }

    @Test
    public void shouldInheritValuesWithoutDuplicates(){
        AnnotationFinder inheritingAnnotated = new AnnotationFinder(InheritingAnnotatedWithDuplicates.class);
        List<String> annotatedValues = inheritingAnnotated.getAnnotatedValues(MyAnnotationWithMembers.class, String.class, "values");
        assertThat(annotatedValues.size(), equalTo(3));
        assertThat(annotatedValues, hasItem("1"));        
        assertThat(annotatedValues, hasItem("2"));        
        assertThat(annotatedValues, hasItem("3"));      
    }

    @Test
    public void shouldNotInheritValuesIfInheritFlagIsFalse(){
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
