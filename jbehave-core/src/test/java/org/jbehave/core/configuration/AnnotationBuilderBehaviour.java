package org.jbehave.core.configuration;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.List;

import org.hamcrest.Matchers;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingPaths;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.configuration.AnnotationBuilder.InstantiationFailed;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.ParameterConverters.FromStringParameterConverter;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.scan.GivenOnly;
import org.jbehave.core.steps.scan.GivenWhen;
import org.jbehave.core.steps.scan.GivenWhenThen;
import org.junit.jupiter.api.Test;

class AnnotationBuilderBehaviour {

    @Test
    void shouldReturnDependencies() {
        AnnotationBuilder annotated = new AnnotationBuilder(Annotated.class);
        assertThat(annotated.annotatedClass().getName(), equalTo(Annotated.class.getName()));
        assertThat(annotated.annotationFinder(), instanceOf(AnnotationFinder.class));
        assertThat(annotated.annotationMonitor(), instanceOf(PrintStreamAnnotationMonitor.class));
    }

    @Test
    void shouldBuildDefaultEmbedderIfAnnotationNotPresent() {
        AnnotationBuilder notAnnotated = new AnnotationBuilder(NotAnnotated.class);
        assertThat(notAnnotated.buildEmbedder(), is(notNullValue()));
    }

    @Test
    void shouldBuildEmbedderWithAnnotatedControls() {
        AnnotationBuilder annotated = new AnnotationBuilder(AnnotedEmbedderControls.class);
        EmbedderControls embedderControls = annotated.buildEmbedder().embedderControls();
        assertThat(embedderControls.batch(), is(true));
        assertThat(embedderControls.generateViewAfterStories(), is(true));
        assertThat(embedderControls.ignoreFailureInStories(), is(true));
        assertThat(embedderControls.ignoreFailureInView(), is(true));
        assertThat(embedderControls.skip(), is(true));
        assertThat(embedderControls.storyTimeouts(), equalTo("**/longs/*.story:60,**/shorts/*.story:10"));
        assertThat(embedderControls.failOnStoryTimeout(), is(true));
        assertThat(embedderControls.threads(), equalTo(2));
        assertThat(embedderControls.verboseFailures(), is(true));
        assertThat(embedderControls.verboseFiltering(), is(true));
    }

    @Test
    void shouldBuildWithCustomConfiguration() {
        AnnotationBuilder annotated = new AnnotationBuilder(AnnotatedCustomConfiguration.class);
        assertThat(annotated.buildConfiguration(), instanceOf(MyConfiguration.class));
    }

    @Test
    void shouldBuildCandidateSteps() {
        AnnotationBuilder annotated = new AnnotationBuilder(Annotated.class);
        assertThatStepsInstancesAre(annotated.buildCandidateSteps(), MySteps.class, MyOtherSteps.class);
    }

    @Test
    void shouldBuildCandidateStepsAsEmptyListIfAnnotationOrAnnotatedValuesNotPresent() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnnotationBuilder notAnnotated = new AnnotationBuilder(NotAnnotated.class, new PrintStreamAnnotationMonitor(
                new PrintStream(baos)));
        assertThatStepsInstancesAre(notAnnotated.buildCandidateSteps());
        AnnotationBuilder annotatedWithoutSteps = new AnnotationBuilder(AnnotatedWithoutSteps.class);
        assertThatStepsInstancesAre(annotatedWithoutSteps.buildCandidateSteps());
        assertThat(baos.toString().trim(), is(equalTo("Annotation " + UsingSteps.class + " not found in "
                + NotAnnotated.class)));
    }

    @Test
    void shouldInheritStepsInstances() {
        AnnotationBuilder annotated = new AnnotationBuilder(AnnotatedInheriting.class);
        assertThatStepsInstancesAre(annotated.buildCandidateSteps(), MyOtherOtherSteps.class, MySteps.class,
                MyOtherSteps.class);
    }

    @Test
    void shouldNotInheritStepsInstances() {
        AnnotationBuilder builderAnnotated = new AnnotationBuilder(AnnotatedNotInheriting.class);
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(), MyOtherOtherSteps.class);
    }

    @Test
    void shouldNotIgnoreFailingStepsInstances() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnnotationBuilder annotatedFailing = new AnnotationBuilder(AnnotatedFailing.class,
                new PrintStreamAnnotationMonitor(new PrintStream(baos)));
        try {
            assertThatStepsInstancesAre(annotatedFailing.buildCandidateSteps(), MySteps.class);
            throw new AssertionError("Exception was not thrown");
        } catch (RuntimeException e) {
            assertThat(baos.toString(), containsString("Element creation failed"));
            assertThat(baos.toString(), containsString("RuntimeException"));
        }
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
    void shouldCreateEmbeddableInstanceFromInjectableEmbedder() {
        AnnotationBuilder annotatedInjectable = new AnnotationBuilder(AnnotedInjectable.class);
        Object instance = annotatedInjectable.embeddableInstance();
        assertThat(instance, Matchers.instanceOf(InjectableEmbedder.class));
        Embedder embedder = ((InjectableEmbedder) instance).injectedEmbedder();
        assertThat(embedder.configuration().keywords(), instanceOf(MyKeywords.class));
        assertThat(embedder.metaFilters(), equalTo(asList("+embedder injectable")));
        assertThat(embedder.systemProperties().getProperty("one"), equalTo("One"));
        assertThat(embedder.systemProperties().getProperty("two"), equalTo("Two"));
        assertThatStepsInstancesAre(embedder.stepsFactory().createCandidateSteps(), MySteps.class);
    }

    @Test
    void shouldCreateEmbeddableInstanceFromConfigurableEmbedder() {
        AnnotationBuilder annotatedConfigurable = new AnnotationBuilder(AnnotedConfigurable.class);
        Object instance = annotatedConfigurable.embeddableInstance();
        assertThat(instance, Matchers.instanceOf(ConfigurableEmbedder.class));
        Embedder embedder = ((ConfigurableEmbedder) instance).configuredEmbedder();
        assertThat(embedder.configuration().keywords(), instanceOf(MyKeywords.class));
        assertThat(embedder.metaFilters(), equalTo(asList("+embedder configurable")));
        assertThatStepsInstancesAre(embedder.stepsFactory().createCandidateSteps(), MySteps.class);
    }

    @Test
    void shouldFindStoryPaths() {
        assertThat(new AnnotationBuilder(AnnotatedWithPaths.class).findPaths().size(), greaterThan(0));
        assertThat(new AnnotationBuilder(AnnotedConfigurable.class).findPaths().size(), equalTo(0));
    }

    @Test
    void shouldNotCreateEmbeddableInstanceForAnnotatedClassThatIsNotInstantiable() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnnotationBuilder annotatedPrivate = new AnnotationBuilder(AnnotatedPrivate.class,
                new PrintStreamAnnotationMonitor(new PrintStream(baos)));
        try {
            annotatedPrivate.embeddableInstance();
            throw new AssertionError("Exception was not thrown");
        } catch (InstantiationFailed e) {
            assertThat(baos.toString(), containsString("Element creation failed"));
            assertThat(baos.toString(), containsString("IllegalAccessException"));
        }

    }

    @Test
    void shouldBuildCandidateStepsFromPackages() {
        AnnotationBuilder annotatedWithPackages = new AnnotationBuilder(AnnotatedWithPackages.class);
        List<CandidateSteps> candidateSteps = annotatedWithPackages.buildCandidateSteps();
        assertThatStepsInstancesAre(candidateSteps, GivenOnly.class, GivenWhen.class, GivenWhenThen.class);
    }

    
    @Configure(parameterConverters = { MyParameterConverter.class })
    @UsingSteps(instances = { MySteps.class, MyOtherSteps.class })
    static class Annotated {

    }

    static class MyParameterConverter extends FromStringParameterConverter<String> {

        @Override
        public boolean canConvertTo(Type type) {
            return true;
        }

        @Override
        public String convertValue(String value, Type type) {
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
    
    @Configure
    @UsingSteps(packages = { "org.jbehave.core.steps.scan" })
    static class AnnotatedWithPackages {

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

    @Configure(using = MyConfiguration.class)
    static class AnnotatedCustomConfiguration extends InjectableEmbedder {

        @Override
        public void run() {
        }

    }

    static class MyConfiguration extends Configuration {

    }

    @UsingEmbedder(batch = true, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true, skip = true, verboseFailures = true, verboseFiltering = true, 
            storyTimeouts = "**/longs/*.story:60,**/shorts/*.story:10", failOnStoryTimeout = true, threads = 2)
    @UsingSteps(instances = { MySteps.class })
    static class AnnotedEmbedderControls extends InjectableEmbedder {

        @Override
        public void run() {
        }

    }

    @Configure(keywords = MyKeywords.class)
    @UsingEmbedder(metaFilters = "+embedder injectable", systemProperties = "one=One,two=Two")
    @UsingSteps(instances = { MySteps.class })
    static class AnnotedInjectable extends InjectableEmbedder {

        @Override
        public void run() {
        }

    }

    @Configure(keywords = MyKeywords.class)
    @UsingEmbedder(metaFilters = "+embedder configurable")
    @UsingSteps(instances = { MySteps.class })
    static class AnnotedConfigurable extends ConfigurableEmbedder {

        @Override
        public void run() {
        }

    }

    static class MyKeywords extends LocalizedKeywords {

    }

    @Configure()
    @UsingEmbedder()
    @UsingSteps(instances = { MySteps.class })
    private static class AnnotatedPrivate extends ConfigurableEmbedder {

        @Override
        public void run() {
        }

    }

    @UsingPaths(searchIn = "src/test/java", includes = { "**/stories/*story" })
    private static class AnnotatedWithPaths {

    }

}
