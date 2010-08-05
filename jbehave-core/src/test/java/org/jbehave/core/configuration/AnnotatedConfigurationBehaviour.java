package org.jbehave.core.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import java.util.List;

import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Steps;
import org.junit.Test;

public class AnnotatedConfigurationBehaviour {

    @Test
    public void shouldCreateCandidateStepsFromAnnotation(){
       	AnnotationBuilder builderAnnotated = new AnnotationBuilder(Annotated.class);
		Configuration configuration = builderAnnotated.buildConfiguration();
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(configuration), MySteps.class, MyOtherSteps.class);
    }
    
    @Test
    public void shouldCreateEmptyCandidateStepsListIfAnnotationOrAnnotatedValuesNotPresent(){
       	AnnotationBuilder builderNotAnnotated = new AnnotationBuilder(NotAnnotated.class);
		Configuration configuration = builderNotAnnotated.buildConfiguration();
    	assertThatStepsInstancesAre(builderNotAnnotated.buildCandidateSteps(configuration));
       	AnnotationBuilder builderAnnotatedWithoutSteps = new AnnotationBuilder(AnnotatedWithoutSteps.class);
        assertThatStepsInstancesAre(builderAnnotatedWithoutSteps.buildCandidateSteps(configuration));
    }

    private void assertThatStepsInstancesAre(List<CandidateSteps> candidateSteps, Class<?>... stepsClasses){
        assertThat(candidateSteps.size(), equalTo(stepsClasses.length));
        for (int i = 0; i < stepsClasses.length; i++ ){
            assertThat(((Steps)candidateSteps.get(i)).instance(), instanceOf(stepsClasses[i]));
        }        
    }

    @UsingSteps(instances={MySteps.class, MyOtherSteps.class})
    private static class Annotated {
        
    }
    
    @UsingSteps()
    private static class AnnotatedWithoutSteps {
        
    }

    private static class NotAnnotated {
        
    }
    
    static class MySteps {
        
    }
    
    static class MyOtherSteps{
        
    }
}
