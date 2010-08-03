package org.jbehave.core.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import java.util.List;

import org.jbehave.core.annotations.UsingInheritance;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Steps;
import org.junit.Test;

public class AnnotationBuilderBehaviour {

    @Test
    public void shouldCreateCandidateStepsFromAnnotation() {
        AnnotationBuilder builderAnnotated = new AnnotationBuilder(Annotated.class);
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(), MySteps.class, MyOtherSteps.class);
    }

    @Test
    public void shouldCreateEmptyCandidateStepsListIfAnnotationOrAnnotatedValuesNotPresent() {
        AnnotationBuilder builderNotAnnotated = new AnnotationBuilder(NotAnnotated.class);
        assertThatStepsInstancesAre(builderNotAnnotated.buildCandidateSteps());
        AnnotationBuilder builderAnnotatedWithoutSteps = new AnnotationBuilder(AnnotatedWithoutSteps.class);
        assertThatStepsInstancesAre(builderAnnotatedWithoutSteps.buildCandidateSteps());
    }

    @Test
    public void shouldInheritInstances() {
        AnnotationBuilder builderAnnotated = new AnnotationBuilder(AnnotatedInheriting.class);
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(), MyOtherOtherSteps.class, MySteps.class,
                MyOtherSteps.class);
    }

    @Test
    public void shouldNotInheritInstances() {
        AnnotationBuilder builderAnnotated = new AnnotationBuilder(AnnotatedNotInheriting.class);
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(), MyOtherOtherSteps.class);
    }

    private void assertThatStepsInstancesAre(List<CandidateSteps> candidateSteps, Class<?>... stepsClasses) {
        assertThat(candidateSteps.size(), equalTo(stepsClasses.length));
        for (int i = 0; i < stepsClasses.length; i++) {
            assertThat(((Steps) candidateSteps.get(i)).instance(), instanceOf(stepsClasses[i]));
        }
    }

    @UsingSteps(instances = { MySteps.class, MyOtherSteps.class })
    private static class Annotated {

    }

    @UsingSteps(instances = { MyOtherOtherSteps.class })
    private static class AnnotatedInheriting extends Annotated {

    }

    @UsingSteps(instances = { MyOtherOtherSteps.class })
    @UsingInheritance(ofValues = false)
    private static class AnnotatedNotInheriting extends Annotated {

    }

    @UsingSteps()
    private static class AnnotatedWithoutSteps {

    }

    private static class NotAnnotated {

    }

    static class MySteps {

    }

    static class MyOtherSteps {

    }

    static class MyOtherOtherSteps {

    }

}
