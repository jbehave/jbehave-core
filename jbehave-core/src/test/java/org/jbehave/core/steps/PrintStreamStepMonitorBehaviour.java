package org.jbehave.core.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.model.StepPattern;
import org.junit.jupiter.api.Test;

class PrintStreamStepMonitorBehaviour {

    private final OutputStream out = new ByteArrayOutputStream();
    private final StepMonitor monitor = new PrintStreamStepMonitor(new PrintStream(out));

    @Test
    void shouldReportStepMatchesType() {
        // When
        monitor.stepMatchesType("When another step", "Given my step", false, StepType.GIVEN, null, null);

        // Then
        assertIsOutputEqualTo("Step 'When another step' (with previous step 'Given my step') does not match type "
                + "'GIVEN' for method 'null' with annotations '[]' in steps instance 'null'");
    }

    @Test
    void shouldReportStepMatchesPattern() throws NoSuchMethodException {
        MySteps steps = new MySteps();
        Method method = MySteps.class.getMethod("thenFoo", String.class);
        // When
        StepPattern stepPattern = new StepPattern(StepType.THEN, "Then foo named $name", "Then foo named name");
        monitor.stepMatchesPattern("Then foo named name", true, stepPattern, method, steps);

        // Then
        assertIsOutputEqualTo("Step 'Then foo named name' matches pattern '" + stepPattern
                + "' for method 'public void org.jbehave.core.steps.PrintStreamStepMonitorBehaviour$MySteps.thenFoo(java.lang.String)' "
                + "with annotations '[@org.jbehave.core.annotations.Then(priority=0, value=\"foo named $name\")]' in steps instance '"
                + steps + "'");
    }

    @Test
    void shouldReportConvertedValueOfTypeWithConverters() {
        // When
        Queue<Class<?>> convertersQueue = new LinkedList<>(Arrays.asList(ParameterConverters.NumberConverter.class,
                ParameterConverters.ExamplesTableConverter.class, ParameterConverters.EnumListConverter.class));
        monitor.convertedValueOfType("1", int.class, 1, convertersQueue);

        // Then
        assertIsOutputEqualTo("Converted value '1' of type 'int' to '1' with converters "
                + "'org.jbehave.core.steps.ParameterConverters$NumberConverter "
                + "-> org.jbehave.core.steps.ParameterConverters$ExamplesTableConverter "
                + "-> org.jbehave.core.steps.ParameterConverters$EnumListConverter'");
    }

    @Test
    void shouldReportBeforePerformingStep() {
        // When
        monitor.beforePerforming("a step", false, null);

        // Then
        assertIsOutputEqualTo("Performing step 'a step'");
    }

    @Test
    void shouldReportNothingAfterBeforePerformingStep() {
        // When
        monitor.afterPerforming("a step", false, null);

        // Then
        assertThat(out.toString(), equalTo(""));
    }

    @Test
    void shouldReportUsingAnnotatedNameForParameter() {
        // When
        monitor.usingAnnotatedNameForParameter("name", 0);

        // Then
        assertIsOutputEqualTo("Using annotated name 'name' for parameter position 0");
    }

    @Test
    void shouldReportUsingParameterNameForParameter() {
        // When
        monitor.usingParameterNameForParameter("name", 0);

        // Then
        assertIsOutputEqualTo("Using parameter name 'name' for parameter position 0");
    }

    @Test
    void shouldReportUsingTableAnnotatedNameForParameter() {
        // When
        monitor.usingTableAnnotatedNameForParameter("name", 0);

        // Then
        assertIsOutputEqualTo("Using table annotated name 'name' for parameter position 0");
    }

    @Test
    void shouldReportUsingTableParameterNameForParameter() {
        // When
        monitor.usingTableParameterNameForParameter("name", 0);

        // Then
        assertIsOutputEqualTo("Using table parameter name 'name' for parameter position 0");
    }

    @Test
    void shouldReportUsingNaturalOrderForParameter() {
        // When
        monitor.usingNaturalOrderForParameter(0);

        // Then
        assertIsOutputEqualTo("Using natural order for parameter position 0");
    }

    @Test
    void shouldReportFoundParameter() {
        // When
        monitor.foundParameter("parameter", 0);

        // Then
        assertIsOutputEqualTo("Found parameter 'parameter' for position 0");
    }

    @Test
    void shouldReportUsingStepsContextParameter() {
        // When
        monitor.usingStepsContextParameter("fromContext");

        // Then
        assertIsOutputEqualTo("Found parameter 'fromContext' from Steps Context");
    }

    private void assertIsOutputEqualTo(String message) {
        assertThat(out.toString(), equalTo(message + System.lineSeparator()));
    }

    static class MySteps  {

        @Then("foo named $name")
        public void thenFoo(String name) {
        }
    }
}
