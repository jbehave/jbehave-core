package org.jbehave.core.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import org.jbehave.core.annotations.WithSteps;
import org.jbehave.core.steps.CandidateSteps;
import org.junit.Test;

public class AnnotatedConfigurationBehaviour {

    @Test
    public void shouldConfigureFromAnnotations(){
        AnnotationBuilder builder = new AnnotationBuilder();
        List<CandidateSteps> candidateSteps = builder.buildCandidateSteps(new Annotated());
        assertThat(candidateSteps.size(), equalTo(2));
    }
    
    @WithSteps(instances={MySteps.class, MyOtherSteps.class})
    private static class Annotated {
        
    }
    
    static class MySteps {
        
    }
    
    static class MyOtherSteps{
        
    }
}
