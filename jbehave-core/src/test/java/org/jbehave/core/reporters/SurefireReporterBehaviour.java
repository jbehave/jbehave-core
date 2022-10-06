package org.jbehave.core.reporters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jbehave.core.embedder.MatchingStepMonitor.StepMatch;
import org.jbehave.core.embedder.PerformableTree.NormalPerformableScenario;
import org.jbehave.core.embedder.PerformableTree.PerformableRoot;
import org.jbehave.core.embedder.PerformableTree.PerformableScenario;
import org.jbehave.core.embedder.PerformableTree.PerformableSteps;
import org.jbehave.core.embedder.PerformableTree.PerformableStory;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.Description;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.StepPattern;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepType;
import org.junit.jupiter.api.Test;


class SurefireReporterBehaviour {

    @Test
    void shouldProduceXmlReportWithSimpleNamingStrategy() throws Exception {

        // Given
        String reportName = "surefire-simple";
        SurefireReporter.Options options = new SurefireReporter.Options().useReportName(reportName).doIncludeProperties(
                false);
        SurefireReporter reporter = new SurefireReporter(this.getClass(), options);

        // When
        PerformableRoot root = performableRoot();
        File outputDirectory = new File("target");
        reporter.generate(root, outputDirectory);
        

        // Then
        String outputPath = reportName + ".xml";
        assertThat(output(outputDirectory, outputPath), equalTo(resource(outputPath)));
    }

    @Test
    void shouldProduceXmlReportWithBreadcrumbNamingStrategy() throws Exception {

        // Given
        String reportName = "surefire-breadcrumb";
        SurefireReporter.Options options = new SurefireReporter.Options().useReportName(reportName).doIncludeProperties(
                false).withNamingStrategy(new SurefireReporter.BreadcrumbNamingStrategy());
        SurefireReporter reporter = new SurefireReporter(this.getClass(), options);

        // When
        PerformableRoot root = performableRoot();
        File outputDirectory = new File("target");
        reporter.generate(root, outputDirectory);


        // Then
        String outputPath = reportName + ".xml";
        assertThat(output(outputDirectory, outputPath), equalTo(resource(outputPath)));
    }

    private String resource(String name) throws IOException {
        return IOUtils.toString(getClass().getResource(name), StandardCharsets.UTF_8).replaceAll("(?:\\n|\\r)", "");
    }

    private String output(File outputDirectory, String name) throws IOException {
        return IOUtils.toString(new FileReader(new File(outputDirectory, "view/" + name))).replaceAll("(?:\\n|\\r)",
                "");
    }

    private PerformableRoot performableRoot() {
        PerformableRoot root = new PerformableRoot();
        Story story = new Story("/path/to/an_interesting.story", new Description("An interesting story"),
                new Meta(Arrays.asList("+theme testing", "+author Mauro")),
                new Narrative("renovate my house", "customer", "get a loan"), new ArrayList<Scenario>());
        PerformableStory performableStory = new PerformableStory(story, new LocalizedKeywords(), false);
        root.add(performableStory);
        Scenario scenario = new Scenario("title", Arrays.asList(""));
        PerformableScenario performableScenario = new PerformableScenario(scenario, story.getPath());
        performableStory.add(performableScenario);
        List<StepMatch> stepMatches = new ArrayList<>();
        stepMatches.add(new StepMatch(new StepPattern(StepType.GIVEN, "(def)", "[abc]")));
        NormalPerformableScenario normalScenario = new NormalPerformableScenario(story);
        normalScenario.addSteps(new PerformableSteps(null, stepMatches));
        performableScenario.useNormalScenario(normalScenario);
        return root;
    }   


}
