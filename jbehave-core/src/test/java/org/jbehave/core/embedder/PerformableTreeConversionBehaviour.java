package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.Scope;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.Step;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.StepMonitor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PerformableTreeConversionBehaviour {

    @SuppressWarnings("unchecked")
    @Test
    void shouldConvertParameters() {
        SharpParameterConverters sharpParameterConverters = new SharpParameterConverters();
        ParameterControls parameterControls = new ParameterControls();
        Configuration configuration = mock(Configuration.class);
        when(configuration.storyControls()).thenReturn(new StoryControls());

        StoryControls storyControls = mock(StoryControls.class);
        when(configuration.storyControls()).thenReturn(storyControls);
        when(storyControls.skipBeforeAndAfterScenarioStepsIfGivenStory()).thenReturn(false);
        when(configuration.parameterConverters()).thenReturn(sharpParameterConverters);
        when(configuration.parameterControls()).thenReturn(parameterControls);

        Keywords keywords = mock(Keywords.class);
        when(configuration.keywords()).thenReturn(keywords);

        StepMonitor stepMonitor = mock(StepMonitor.class);
        when(configuration.stepMonitor()).thenReturn(stepMonitor);
        StepCollector stepCollector = mock(StepCollector.class);
        when(configuration.stepCollector()).thenReturn(stepCollector);

        Map<String,String> storyExampleFirstRow = new HashMap<>();
        storyExampleFirstRow.put("var1","#A");
        storyExampleFirstRow.put("var2","#B");

        Map<String,String> storyExampleSecondRow = new HashMap<>();
        storyExampleSecondRow.put("var1","#C");
        storyExampleSecondRow.put("var2","#D");

        ExamplesTable storyExamplesTable = ExamplesTable.empty().withRows(
                asList(storyExampleFirstRow, storyExampleSecondRow));

        Lifecycle lifecycle = new Lifecycle(storyExamplesTable, new ArrayList<>(), new ArrayList<>());

        Map<Stage, List<Step>> lifecycleSteps = new EnumMap<>(Stage.class);
        lifecycleSteps.put(Stage.BEFORE, emptyList());
        lifecycleSteps.put(Stage.AFTER, emptyList());

        ArgumentCaptor<Map<String, String>> storyParametersCaptor = ArgumentCaptor.forClass(Map.class);
        when(stepCollector.collectLifecycleSteps(eq(emptyList()), eq(lifecycle), isEmptyMeta(), eq(Scope.STORY),
                storyParametersCaptor.capture(), any(MatchingStepMonitor.class))).thenReturn(lifecycleSteps);

        ArgumentCaptor<Map<String, String>> scenarioParametersCaptor = ArgumentCaptor.forClass(Map.class);
        when(stepCollector.collectLifecycleSteps(eq(emptyList()), eq(lifecycle), isEmptyMeta(), eq(Scope.SCENARIO),
                scenarioParametersCaptor.capture(), any(MatchingStepMonitor.class))).thenReturn(lifecycleSteps);

        Map<String,String> scenarioExample = new HashMap<>();
        scenarioExample.put("var1","#E");
        scenarioExample.put("var3","<var2>#F");

        Map<String,String> scenarioExampleSecond = new HashMap<>();
        scenarioExampleSecond.put("var1","#G<var2>");
        scenarioExampleSecond.put("var3","#H");

        ExamplesTable scenarioExamplesTable = ExamplesTable.empty().withRows(
                asList(scenarioExample, scenarioExampleSecond));

        String scenarioTitle = "scenario title";
        GivenStories givenStories = new GivenStories("");
        Scenario scenario = new Scenario(scenarioTitle, new Meta(), givenStories, scenarioExamplesTable, emptyList());

        Story story = new Story(null, null, new Meta(), mock(Narrative.class), givenStories, lifecycle,
                singletonList(scenario));

        PerformableTree performableTree = new PerformableTree();
        PerformableTree.RunContext context = spyRunContext(performableTree, configuration);
        performableTree.addStories(context, singletonList(story));
        List<PerformableTree.PerformableScenario> performableScenarios = performableTree.getRoot().getStories().get(0)
                .getScenarios();

        assertThatAreEqual(scenarioExample.size(), performableScenarios.size());
        assertThatAreEqual(scenarioTitle + " [1]", performableScenarios.get(0).getScenario().getTitle());
        List<PerformableTree.ExamplePerformableScenario> examplePerformableScenarios = performableScenarios.get(0)
                .getExamples();
        assertThatAreEqual(scenarioExample.size(), examplePerformableScenarios.size());
        assertThatAreEqual("eE", examplePerformableScenarios.get(0).getParameters().get("var1"));
        assertThatAreEqual("bB", examplePerformableScenarios.get(0).getParameters().get("var2"));
        assertThatAreEqual("bBb#fF", examplePerformableScenarios.get(0).getParameters().get("var3"));

        assertThatAreEqual("gbbGbB", examplePerformableScenarios.get(1).getParameters().get("var1"));
        assertThatAreEqual("bB", examplePerformableScenarios.get(1).getParameters().get("var2"));
        assertThatAreEqual("hH", examplePerformableScenarios.get(1).getParameters().get("var3"));

        assertThatAreEqual(scenarioTitle + " [2]", performableScenarios.get(1).getScenario().getTitle());
        examplePerformableScenarios = performableScenarios.get(1).getExamples();
        assertThatAreEqual(scenarioExample.size(), examplePerformableScenarios.size());
        assertThatAreEqual("eE", examplePerformableScenarios.get(0).getParameters().get("var1"));
        assertThatAreEqual("dD", examplePerformableScenarios.get(0).getParameters().get("var2"));
        assertThatAreEqual("dDd#fF", examplePerformableScenarios.get(0).getParameters().get("var3"));

        assertThatAreEqual("gddGdD", examplePerformableScenarios.get(1).getParameters().get("var1"));
        assertThatAreEqual("dD", examplePerformableScenarios.get(1).getParameters().get("var2"));
        assertThatAreEqual("hH", examplePerformableScenarios.get(1).getParameters().get("var3"));

        List<Map<String, String>> storyParameters = new ArrayList<>();
        storyParameters.add(new HashMap<>());
        assertEquals(storyParameters, storyParametersCaptor.getAllValues());

        List<Map<String, String>> scenarioParameters = new ArrayList<>();
        scenarioParameters.add(performableScenarios.get(0).getExamples().get(0).getParameters());
        scenarioParameters.add(performableScenarios.get(0).getExamples().get(1).getParameters());
        scenarioParameters.add(performableScenarios.get(1).getExamples().get(0).getParameters());
        scenarioParameters.add(performableScenarios.get(1).getExamples().get(1).getParameters());
        assertEquals(scenarioParameters, scenarioParametersCaptor.getAllValues());
    }

    private PerformableTree.RunContext spyRunContext(PerformableTree performableTree, Configuration configuration) {
        AllStepCandidates allStepCandidates = mock(AllStepCandidates.class);
        EmbedderMonitor embedderMonitor = mock(EmbedderMonitor.class);
        BatchFailures failures = mock(BatchFailures.class);
        return spy(performableTree.newRunContext(configuration, allStepCandidates, embedderMonitor, new MetaFilter(),
                failures));
    }

    private void assertThatAreEqual(Object expected, Object actual) {
        assertThat(actual, is(expected));
    }

    private Meta isEmptyMeta() {
        return argThat(meta -> meta.getPropertyNames().isEmpty());
    }

    private static class SharpParameterConverters extends ParameterConverters {

        @Override
        public Object convert(String value, Type type) {
            if (type == String.class) {
                return value.replace("#", value.substring(1).toLowerCase());
            }
            return null;
        }
    }
}
