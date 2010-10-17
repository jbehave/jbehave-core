package org.jbehave.core.configuration;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.lang.reflect.Type;
import java.util.List;

import org.hamcrest.Matchers;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.configuration.AnnotationBuilder.InstantiationFailed;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.junit.Test;

public class AnnotationBuilderBehaviour {

    @Test
    public void shouldReturnDependencies() {
        AnnotationBuilder annotated = new AnnotationBuilder(Annotated.class);
        assertThat(annotated.annotatedClass().getName(), equalTo(Annotated.class.getName()));
        assertThat(annotated.annotationFinder(), instanceOf(AnnotationFinder.class));
        assertThat(annotated.annotationMonitor(), instanceOf(PrintStreamAnnotationMonitor.class));
    }

    @Test
    public void shouldBuildDefaultEmbedderIfAnnotationNotPresent() {
        AnnotationBuilder notAnnotated = new AnnotationBuilder(NotAnnotated.class);
        assertThat(notAnnotated.buildEmbedder(), is(notNullValue()));
    }

    @Test
    public void shouldBuildCandidateSteps() {
        AnnotationBuilder annotated = new AnnotationBuilder(Annotated.class);
        assertThatStepsInstancesAre(annotated.buildCandidateSteps(), MySteps.class, MyOtherSteps.class);
    }

    @Test
    public void shouldBuildCandidateStepsAsEmptyListIfAnnotationOrAnnotatedValuesNotPresent() {
        AnnotationBuilder notAnnotated = new AnnotationBuilder(NotAnnotated.class);
        assertThatStepsInstancesAre(notAnnotated.buildCandidateSteps());
        AnnotationBuilder annotatedWithoutSteps = new AnnotationBuilder(AnnotatedWithoutSteps.class);
        assertThatStepsInstancesAre(annotatedWithoutSteps.buildCandidateSteps());
    }

    @Test
    public void shouldInheritStepsInstances() {
        AnnotationBuilder annotated = new AnnotationBuilder(AnnotatedInheriting.class);
        assertThatStepsInstancesAre(annotated.buildCandidateSteps(), MyOtherOtherSteps.class, MySteps.class,
                MyOtherSteps.class);
    }

    @Test
    public void shouldNotInheritStepsInstances() {
        AnnotationBuilder builderAnnotated = new AnnotationBuilder(AnnotatedNotInheriting.class);
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(), MyOtherOtherSteps.class);
    }

    @Test(expected = RuntimeException.class)
    public void shouldNotIgnoreFailingStepsInstances() {
        AnnotationBuilder annotatedFailing = new AnnotationBuilder(AnnotatedFailing.class);
        assertThatStepsInstancesAre(annotatedFailing.buildCandidateSteps(), MySteps.class);
    }

    private void assertThatStepsInstancesAre(List<CandidateSteps> candidateSteps, Class<?>... stepsClasses) {
        for (Class<?> stepsClass : stepsClasses) {
            boolean found = false;
            for (CandidateSteps steps : candidateSteps) {
                Object instance = ((Steps) steps).instance();
                if (instance.getClass() == stepsClass) {
                    found = true;
                }
            }
            assertThat(found, is(true));
        }
    }

    @Test
    public void shouldCreateEmbeddableInstanceFromInjectableEmbedder() {
        AnnotationBuilder annotatedInjectable = new AnnotationBuilder(AnnotedInjectable.class);
        Object instance = annotatedInjectable.embeddableInstance();
        assertThat(instance, Matchers.instanceOf(InjectableEmbedder.class));
        Embedder embedder = ((InjectableEmbedder) instance).injectedEmbedder();
        assertThat(embedder.configuration().keywords(), instanceOf(MyKeywords.class));
        assertThat(embedder.metaFilters(), equalTo(asList("+embedder injectable")));
        assertThatStepsInstancesAre(embedder.candidateSteps(), MySteps.class);
    }

    @Test
    public void shouldCreateEmbeddableInstanceFromConfigurableEmbedder() {
        AnnotationBuilder annotatedConfigurable = new AnnotationBuilder(AnnotedConfigurable.class);
        Object instance = annotatedConfigurable.embeddableInstance();
        assertThat(instance, Matchers.instanceOf(ConfigurableEmbedder.class));
        Embedder embedder = ((ConfigurableEmbedder) instance).configuredEmbedder();
        assertThat(embedder.configuration().keywords(), instanceOf(MyKeywords.class));
        assertThat(embedder.metaFilters(), equalTo(asList("+embedder configurable")));
        assertThatStepsInstancesAre(embedder.candidateSteps(), MySteps.class);
    }

    @Test(expected = InstantiationFailed.class)
    public void shouldNotCreateEmbeddableInstanceForAnnotatedClassThatIsNotInstantiable() {
        AnnotationBuilder annotatedPrivate = new AnnotationBuilder(AnnotatedPrivate.class);
        annotatedPrivate.embeddableInstance();
    }

    @Configure(parameterConverters = { MyParameterConverter.class })
    @UsingSteps(instances = { MySteps.class, MyOtherSteps.class })
    static class Annotated {

    }

    static class MyParameterConverter implements ParameterConverter {

        public boolean accept(Type type) {
            return true;
        }

        public Object convertValue(String value, Type type) {
            return value + "Converted";
        }

    }

    @UsingSteps(instances = { MyOtherOtherSteps.class })
    static class AnnotatedInheriting extends Annotated {

    }

    @UsingSteps(instances = { MyOtherOtherSteps.class }, inheritInstances = false)
    static class AnnotatedNotInheriting extends Annotated {

    }

    @UsingSteps(instances = { MySteps.class, MyFailingSteps.class })
    static class AnnotatedFailing {

    }

    @UsingSteps()
    static class AnnotatedWithoutSteps {

    }

    static class NotAnnotated {

    }

    static class MySteps {

    }

    static class MyOtherSteps {

    }

    static class MyOtherOtherSteps {

    }

    static class MyFailingSteps {
        public MyFailingSteps() {
            throw new RuntimeException();
        }
    }

    @Configure(keywords = MyKeywords.class)
    @UsingEmbedder(metaFilters = "+embedder injectable")
    @UsingSteps(instances = { MySteps.class })
    static class AnnotedInjectable extends InjectableEmbedder {

        public void run() throws Throwable {
        }

    }

    @Configure(keywords = MyKeywords.class)
    @UsingEmbedder(metaFilters = "+embedder configurable")
    @UsingSteps(instances = { MySteps.class })
    static class AnnotedConfigurable extends ConfigurableEmbedder {

        public void run() throws Throwable {
        }

    }

    static class MyKeywords extends LocalizedKeywords {

    }

    @Configure()
    @UsingEmbedder()
    @UsingSteps(instances = { MySteps.class })
    private static class AnnotatedPrivate extends ConfigurableEmbedder {

        public void run() throws Throwable {
        }

    }

}
