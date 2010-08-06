package org.jbehave.core.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import java.util.List;

import org.hamcrest.Matchers;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.i18n.LocalizedKeywords;
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
    public void shouldInheritStepsInstances() {
        AnnotationBuilder builderAnnotated = new AnnotationBuilder(AnnotatedInheriting.class);
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(), MyOtherOtherSteps.class, MySteps.class,
                MyOtherSteps.class);
    }

    @Test
    public void shouldNotInheritStepsInstances() {
        AnnotationBuilder builderAnnotated = new AnnotationBuilder(AnnotatedNotInheriting.class);
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(), MyOtherOtherSteps.class);
    }

    private void assertThatStepsInstancesAre(List<CandidateSteps> candidateSteps, Class<?>... stepsClasses) {
        assertThat(candidateSteps.size(), equalTo(stepsClasses.length));
        for (int i = 0; i < stepsClasses.length; i++) {
            assertThat(((Steps) candidateSteps.get(i)).instance(), instanceOf(stepsClasses[i]));
        }
    }

    @Test
    public void shouldCreateEmbeddableInstanceFromInjectableEmbedder() {
        AnnotationBuilder builderInjectable = new AnnotationBuilder(AnnotedInjectable.class);
        Object instance = builderInjectable.embeddableInstance();
        assertThat(instance, Matchers.instanceOf(InjectableEmbedder.class));
        Embedder embedder = ((InjectableEmbedder) instance).injectedEmbedder();
        assertThat(embedder.configuration().keywords(), instanceOf(MyKeywords.class));
        assertThatStepsInstancesAre(embedder.candidateSteps(), MySteps.class);
    }

    @Test
    public void shouldCreateEmbeddableInstanceFromConfigurableEmbedder() {
        AnnotationBuilder builderConfigurable = new AnnotationBuilder(AnnotedConfigurable.class);
        Object instance = builderConfigurable.embeddableInstance();
        assertThat(instance, Matchers.instanceOf(ConfigurableEmbedder.class));
        Embedder embedder = ((ConfigurableEmbedder) instance).configuredEmbedder();
        assertThat(embedder.configuration().keywords(), instanceOf(MyKeywords.class));
        assertThatStepsInstancesAre(embedder.candidateSteps(), MySteps.class);
    }

    @UsingSteps(instances = { MySteps.class, MyOtherSteps.class })
    private static class Annotated {

    }

    @UsingSteps(instances = { MyOtherOtherSteps.class })
    private static class AnnotatedInheriting extends Annotated {

    }

    @UsingSteps(instances = { MyOtherOtherSteps.class }, inheritInstances = false)
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

    @Configure(keywords = MyKeywords.class)
    @UsingEmbedder()
    @UsingSteps(instances = {MySteps.class})
    static class AnnotedInjectable extends InjectableEmbedder {

        public void run() throws Throwable {
        }

    }
    
    @Configure(keywords = MyKeywords.class)
    @UsingEmbedder()
    @UsingSteps(instances = {MySteps.class})
    static class AnnotedConfigurable extends ConfigurableEmbedder {

        public void run() throws Throwable {
        }

    }

    static class MyKeywords extends LocalizedKeywords {
        
    }
   
}
