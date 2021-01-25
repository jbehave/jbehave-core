package org.jbehave.core.configuration.weld;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.STATS;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.annotations.weld.UsingWeld;
import org.jbehave.core.annotations.weld.WeldStep;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.configuration.weld.WeldBootstrap;
import org.jbehave.core.configuration.weld.ConfigurationProducer.CustomObject;
import org.jbehave.core.failures.SilentlyAbsorbingFailure;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.Steps;
import org.junit.jupiter.api.Test;


public class WeldAnnotationBuilderBehaviour {

    @Test
    public void shouldBuildConfigurationFromAnnotations() {
        AnnotationBuilder builder = createBuilder(AnnotatedUsingWeld.class);
        Configuration configuration = builder.buildConfiguration();

        assertThat(configuration.storyControls().dryRun(), is(true));
        assertThat(configuration.storyControls().skipScenariosAfterFailure(), is(true));
        assertThat(configuration.failureStrategy(), instanceOf(SilentlyAbsorbingFailure.class));
        assertThat(configuration.storyLoader(), instanceOf(LoadFromURL.class));
        assertThat(configuration.stepPatternParser(), instanceOf(RegexPrefixCapturingPatternParser.class));
        assertThat(((RegexPrefixCapturingPatternParser) configuration.stepPatternParser()).getPrefix(),
                equalTo("MyPrefix"));
        assertThatCustomObjectIsConverted(configuration.parameterConverters());
        assertThatDateIsConvertedWithFormat(configuration.parameterConverters(), new SimpleDateFormat("yyyy-MM-dd"));
        assertThat(configuration.storyReporterBuilder().formats(), hasItems(CONSOLE, HTML, TXT, XML, STATS));
        Keywords keywords = configuration.storyReporterBuilder().keywords();
        assertThat(keywords, instanceOf(LocalizedKeywords.class));
        assertThat(((LocalizedKeywords) keywords).getLocale(), equalTo(Locale.ITALIAN));
        assertThat(configuration.storyReporterBuilder().outputDirectory().getName(), equalTo("my-output-directory"));
        assertThat(configuration.storyReporterBuilder().viewResources().getProperty("index"),
                equalTo("my-reports-index.ftl"));
        assertThat(configuration.storyReporterBuilder().viewResources().getProperty("decorateNonHtml"), equalTo("true"));
        assertThat(configuration.storyReporterBuilder().reportFailureTrace(), is(true));
        
    }
    
    @Test
    public void shouldBuildConfigurationFromAnnotationsUsingConfigureAndConverters() {
        
        AnnotationBuilder builderAnnotated = createBuilder(AnnotatedUsingConfigureAndConverters.class);
        
        Configuration configuration = builderAnnotated.buildConfiguration();
        assertThatCustomObjectIsConverted(configuration.parameterConverters());
        assertThatDateIsConvertedWithFormat(configuration.parameterConverters(), new SimpleDateFormat("yyyy-MM-dd"));
    }
    
    @Test
    public void shouldBuildDefaultConfigurationIfAnnotationNotPresent() {

        AnnotationBuilder builderNotAnnotated = createBuilder(NotAnnotated.class);
        assertThatConfigurationIs(builderNotAnnotated.buildConfiguration(), new MostUsefulConfiguration());
    }
    
    @Test
    public void shouldBuildCandidateStepsFromAnnotationsUsingWeld() {
        AnnotationBuilder builderAnnotated = createBuilder(AnnotatedUsingWeld.class);
        Configuration configuration = builderAnnotated.buildConfiguration();
        
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(configuration), WeldStepBean.class);
    }
    
    @Test
    public void shouldBuildCandidateStepsFromAnnotationsUsingStepsAndWeldSteps() {
        AnnotationBuilder builderAnnotated = createBuilder(AnnotatedUsingWeldWithSteps.class);
        Configuration configuration = builderAnnotated.buildConfiguration();
        
        assertThatStepsInstancesAre(builderAnnotated.buildCandidateSteps(configuration),WeldStepBean.class, FooSteps.class);
    }
    
    @Test
    public void shouldBuildOnlyWeldStepsListIfAnnotationOrAnnotatedValuesNotPresent() {
        AnnotationBuilder builderNotAnnotated = createBuilder(NotAnnotated.class);
        Configuration configuration = builderNotAnnotated.buildConfiguration();
        
        assertThatStepsInstancesAre(builderNotAnnotated.buildCandidateSteps(configuration),WeldStepBean.class);
    }
    
    @Test
    public void shouldCreateOnlyOneContainerForMultipleBuildInvocations() {

        AnnotationBuilder builderAnnotated = createBuilder(AnnotatedUsingWeld.class);
        Configuration configuration = builderAnnotated.buildConfiguration();
        assertThat(builderAnnotated.buildConfiguration(), sameInstance(configuration));
    }
    
    private AnnotationBuilder createBuilder(Class<?> type) {
        WeldBootstrap bootstrap = new WeldBootstrap();
        bootstrap.initialize();
        AnnotationBuilder builder = bootstrap.findAnnotationBuilder(type);
        assertThat(builder, is(instanceOf(AnnotationBuilder.class)));
        return builder;
    }
    
    private void assertThatStepsInstancesAre(List<CandidateSteps> candidateSteps, Class<?>... stepsClasses) {
        assertThat(candidateSteps.size(), equalTo(stepsClasses.length));
        for (int i = 0; i < stepsClasses.length; i++) {
            assertThat(((Steps) candidateSteps.get(i)).instance(), instanceOf(stepsClasses[i]));
        }
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
    
    private void assertThatCustomObjectIsConverted(ParameterConverters parameterConverters) {
        assertThat(parameterConverters.convert("value", CustomObject.class).toString(),
                equalTo(new CustomObject("value").toString()));
    }

    private void assertThatDateIsConvertedWithFormat(ParameterConverters parameterConverters, DateFormat dateFormat) {
        String date = "2010-10-10";
        try {
            assertThat((Date) parameterConverters.convert(date, Date.class), equalTo(dateFormat.parse(date)));
        } catch (ParseException e) {
            throw new AssertionError();
        }
    }
    
    @Configure
    @UsingWeld
    public static class AnnotatedUsingWeld {

    }
    
    @Configure
    @UsingWeld
    @UsingSteps(instances={FooSteps.class})
    public static class AnnotatedUsingWeldWithSteps {

    }
    
    @Configure(parameterConverters = { ConfigurationProducer.MyDateConverter.class })
    @UsingWeld
    private static class AnnotatedUsingConfigureAndConverters {

    }
    
    private static class NotAnnotated {

    }
    
    @WeldStep
    public static class WeldStepBean {
        
        @Given("this is a step")
        public void simpleStep() {}
    }
    
    public static class FooSteps {
        
        @Given("this is another step")
        public void simpleStep2() {}
    }

}
