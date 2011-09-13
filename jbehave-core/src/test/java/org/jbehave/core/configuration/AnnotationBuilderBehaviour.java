package org.jbehave.core.configuration;

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
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.Steps;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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
    public void shouldBuildEmbedderWithAnnotatedControls() {
        AnnotationBuilder annotated = new AnnotationBuilder(AnnotedEmbedderControls.class);
        EmbedderControls embedderControls = annotated.buildEmbedder().embedderControls();
        assertThat(embedderControls.batch(), is(true));
        assertThat(embedderControls.generateViewAfterStories(), is(true));
        assertThat(embedderControls.ignoreFailureInStories(), is(true));
        assertThat(embedderControls.ignoreFailureInView(), is(true));
        assertThat(embedderControls.skip(), is(true));        
        assertThat(embedderControls.storyTimeoutInSecs(), equalTo(100L));
        assertThat(embedderControls.threads(), equalTo(2));
    }

    @Test
    public void shouldBuildWithCustomConfiguration() {
        AnnotationBuilder annotated = new AnnotationBuilder(AnnotatedCustomConfiguration.class);
        assertThat(annotated.buildConfiguration(), instanceOf(MyConfiguration.class));
    }

    @Test
    public void shouldBuildCandidateSteps() {
        AnnotationBuilder annotated = new AnnotationBuilder(Annotated.class);
        assertThatStepsInstancesAre(annotated.buildCandidateSteps(), MySteps.class, MyOtherSteps.class);
    }

    @Test
    public void shouldBuildCandidateStepsAsEmptyListIfAnnotationOrAnnotatedValuesNotPresent() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnnotationBuilder notAnnotated = new AnnotationBuilder(NotAnnotated.class, new PrintStreamAnnotationMonitor(new PrintStream(baos)));
        assertThatStepsInstancesAre(notAnnotated.buildCandidateSteps());
        AnnotationBuilder annotatedWithoutSteps = new AnnotationBuilder(AnnotatedWithoutSteps.class);
        assertThatStepsInstancesAre(annotatedWithoutSteps.buildCandidateSteps());
        assertThat(baos.toString().trim(), is(equalTo("Annotation " + UsingSteps.class + " not found in " + NotAnnotated.class)));
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

    @Test
    public void shouldNotIgnoreFailingStepsInstances() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnnotationBuilder annotatedFailing = new AnnotationBuilder(AnnotatedFailing.class, new PrintStreamAnnotationMonitor(new PrintStream(baos)));
        try {
            assertThatStepsInstancesAre(annotatedFailing.buildCandidateSteps(), MySteps.class);
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
    public void shouldCreateEmbeddableInstanceFromInjectableEmbedder() {
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
    public void shouldCreateEmbeddableInstanceFromInjectableEmbedderWithoutStepsFactory() {
        AnnotationBuilder annotatedInjectable = new AnnotationBuilder(AnnotedInjectableWithoutStepsFactory.class);
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
        assertThatStepsInstancesAre(embedder.stepsFactory().createCandidateSteps(), MySteps.class);
    }

    @Test
    public void shouldCreateEmbeddableInstanceFromConfigurableEmbedderWithoutStepsFactory() {
        AnnotationBuilder annotatedConfigurable = new AnnotationBuilder(AnnotedConfigurableWithoutStepsFactory.class);
        Object instance = annotatedConfigurable.embeddableInstance();
        assertThat(instance, Matchers.instanceOf(ConfigurableEmbedder.class));
        Embedder embedder = ((ConfigurableEmbedder) instance).configuredEmbedder();
        assertThat(embedder.configuration().keywords(), instanceOf(MyKeywords.class));
        assertThat(embedder.metaFilters(), equalTo(asList("+embedder configurable")));
        assertThatStepsInstancesAre(embedder.candidateSteps(), MySteps.class);
    }

    @Test
    public void shouldFindStoryPaths() {
        assertThat(new AnnotationBuilder(AnnotatedWithPaths.class).findPaths().size(), greaterThan(0));
        assertThat(new AnnotationBuilder(AnnotedConfigurable.class).findPaths().size(), equalTo(0));
    }

    @Test
    public void shouldNotCreateEmbeddableInstanceForAnnotatedClassThatIsNotInstantiable() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AnnotationBuilder annotatedPrivate = new AnnotationBuilder(AnnotatedPrivate.class, new PrintStreamAnnotationMonitor(new PrintStream(baos)));
        try {
            annotatedPrivate.embeddableInstance();
        } catch (InstantiationFailed e) {
            assertThat(baos.toString(), containsString("Element creation failed"));
            assertThat(baos.toString(), containsString("IllegalAccessException"));
        }

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

    @Configure(using = MyConfiguration.class)
    static class AnnotatedCustomConfiguration extends InjectableEmbedder {

        public void run() throws Throwable {
        }

    }

    static class MyConfiguration extends Configuration {
        
    }
    
    @UsingEmbedder(batch = true, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true, skip = true,
            storyTimeoutInSecs = 100, threads = 2)
    @UsingSteps(instances = { MySteps.class })
    static class AnnotedEmbedderControls extends InjectableEmbedder {

        public void run() throws Throwable {
        }

    }

    @Configure(keywords = MyKeywords.class)
    @UsingEmbedder(metaFilters = "+embedder injectable", systemProperties="one=One,two=Two")
    @UsingSteps(instances = { MySteps.class })
    static class AnnotedInjectable extends InjectableEmbedder {

        public void run() throws Throwable {
        }

    }

    @Configure(keywords = MyKeywords.class)
    @UsingEmbedder(metaFilters = "+embedder injectable", stepsFactory = false)
    @UsingSteps(instances = { MySteps.class })
    static class AnnotedInjectableWithoutStepsFactory extends InjectableEmbedder {

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

    @Configure(keywords = MyKeywords.class)
    @UsingEmbedder(metaFilters = "+embedder configurable", stepsFactory = false)
    @UsingSteps(instances = { MySteps.class })
    static class AnnotedConfigurableWithoutStepsFactory extends ConfigurableEmbedder {

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

    @UsingPaths(searchIn="src/test/java", includes={"**/stories/*story"})
    private static class AnnotatedWithPaths {

    }
    
}
