package org.jbehave.core.configuration.groovy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import java.util.List;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.groovy.UsingGroovy;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Steps;
import org.junit.jupiter.api.Test;

class GroovyAnnotationBuilderBehaviour {

    @Test
    void shouldBuildConfigurationFromAnnotations() {
        GroovyAnnotationBuilder builder = new GroovyAnnotationBuilder(AnnotatedUsingGroovy.class);
        assertThatConfigurationIs(builder.buildConfiguration(), new MostUsefulConfiguration());
    }

    @Test
    void shouldBuildDefaultConfigurationIfAnnotationOrAnnotatedValuesNotPresent() {
        GroovyAnnotationBuilder builderNotAnnotated = new GroovyAnnotationBuilder(NotAnnotated.class);
        assertThatConfigurationIs(builderNotAnnotated.buildConfiguration(), new MostUsefulConfiguration());
    }

    private void assertThatConfigurationIs(Configuration builtConfiguration, Configuration defaultConfiguration) {
        assertThat(builtConfiguration.failureStrategy(), instanceOf(defaultConfiguration.failureStrategy().getClass()));
        assertThat(builtConfiguration.storyLoader(), instanceOf(defaultConfiguration.storyLoader().getClass()));
        assertThat(builtConfiguration.stepPatternParser(), instanceOf(defaultConfiguration.stepPatternParser()
                .getClass()));
        assertThat(builtConfiguration.storyReporterBuilder().formats(), equalTo(defaultConfiguration
                .storyReporterBuilder().formats()));
        assertThat(builtConfiguration.storyReporterBuilder().outputDirectory(), equalTo(defaultConfiguration
                .storyReporterBuilder().outputDirectory()));
        assertThat(builtConfiguration.storyReporterBuilder().viewResources(), equalTo(defaultConfiguration
                .storyReporterBuilder().viewResources()));
        assertThat(builtConfiguration.storyReporterBuilder().reportFailureTrace(), equalTo(defaultConfiguration
                .storyReporterBuilder().reportFailureTrace()));
    }

    @Test
    void shouldBuildCandidateStepsFromAnnotationsUsingGroovy() {
        GroovyAnnotationBuilder builderAnnotated = new GroovyAnnotationBuilder(AnnotatedUsingGroovy.class);
        Configuration configuration = builderAnnotated.buildConfiguration();
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(configuration), "FooSteps");
    }

    private void assertThatStepsInstancesAre(List<CandidateSteps> candidateSteps, String... stepsNames) {
        assertThat(candidateSteps.size(), equalTo(stepsNames.length));
        for (int i = 0; i < stepsNames.length; i++) {
            assertThat(((Steps) candidateSteps.get(i)).instance().getClass().getSimpleName(), equalTo(stepsNames[i]));
        }
    }

    @UsingEmbedder()
    @Configure()
    @UsingGroovy(resourceFinder = TestGroovyResourceFinder.class)
    private static class AnnotatedUsingGroovy {

    }
    
    public static class TestGroovyResourceFinder extends GroovyResourceFinder {
        public TestGroovyResourceFinder(){
            super(CodeLocations.codeLocationFromPath("src/test/java"), "**/configuration/groovy/*.groovy", "");
        }
    }

    private static class NotAnnotated {

    }

}
