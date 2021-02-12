package org.jbehave.core.junit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.jbehave.core.ConfigurableEmbedder;
import org.jbehave.core.junit.story.ExampleScenarioJUnitStories;
import org.jbehave.core.junit.story.ExampleScenarioJUnitStoriesLocalized;
import org.jbehave.core.junit.story.ExampleScenarioJUnitStory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JUnitReportingRunnerIntegrationBehaviour {

    @Mock
    private RunNotifier notifier;

    static Stream<Arguments> data() {
        return Stream.of(
                arguments(ExampleScenarioJUnitStories.class,
                        "Multiplication\u2024story",
                        "Scenario: 2 squared",
                        "Given a variable x with value 2"),
                arguments(ExampleScenarioJUnitStory.class,
                        "example_scenario_j_unit_story\u2024story",
                        "Scenario: 2 squared",
                        "Given a variable x with value 2"),
                arguments(ExampleScenarioJUnitStoriesLocalized.class,
                        "Multiplication_de\u2024story",
                        "Szenario: 2 Quadrat",
                        "Gegeben ist die Variable x mit dem Wert 2")
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void runUpExampleScenarioAndCheckNotifications(Class<? extends ConfigurableEmbedder> cls,
            String expectedFirstStoryName, String expectedFirstScenario, String expectedFirstStep)
            throws ReflectiveOperationException, InitializationError {
        JUnitReportingRunner runner = new JUnitReportingRunner(cls);
        runner.run(notifier);
        verifyAllChildDescriptionsFired(runner.getDescription(), true);
        assertThat(runner.getDescription().getDisplayName(), equalTo(cls.getName()));

        Description firstStory = runner.getDescription().getChildren().get(1);
        assertThat(firstStory.getDisplayName(), equalTo(expectedFirstStoryName));

        Description firstScenario = firstStory.getChildren().get(0);
        assertThat(firstScenario.getDisplayName(), equalTo(expectedFirstScenario));
        assertThat(firstScenario.getChildren().get(0).getDisplayName(), Matchers.startsWith(expectedFirstStep));
    }

    private void verifyAllChildDescriptionsFired(Description description, boolean onlyChildren) {
        if (!onlyChildren && considerStepForVerification(description)) {
            verify(notifier).fireTestStarted(description);
        }
        for (Description child : description.getChildren()) {
            verifyAllChildDescriptionsFired(child, false);
        }
        if (!onlyChildren && considerStepForVerification(description)) {
            verify(notifier).fireTestFinished(description);
        }
    }

    private boolean considerStepForVerification(Description description) {
        String displayName = description.getDisplayName();
        return Character.isDigit(displayName.charAt(displayName.length() - 1));
    }
}
