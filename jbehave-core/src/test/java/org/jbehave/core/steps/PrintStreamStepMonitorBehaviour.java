package org.jbehave.core.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.model.StepPattern;
import org.junit.Test;

public class PrintStreamStepMonitorBehaviour {

    private final OutputStream out = new ByteArrayOutputStream();
    private final StepMonitor monitor = new PrintStreamStepMonitor(new PrintStream(out));

    @Test
    public void shouldReportStepMatchesType() {
        // When
        monitor.stepMatchesType("When another step", "Given my step", false, StepType.GIVEN, null, null);

        // Then
        assertIsOutputEqualTo("Step 'When another step' (with previous step 'Given my step') does not match type "
                + "'GIVEN' for method 'null' with annotations '[]' in steps instance 'null'");
    }

    @Test
    public void shouldReportStepMatchesPattern() throws NoSuchMethodException {
        MySteps steps = new MySteps();
        Method method = MySteps.class.getMethod("thenFoo", String.class);
        // When
        StepPattern stepPattern = new StepPattern(StepType.THEN, "Then foo named $name", "Then foo named name");
        monitor.stepMatchesPattern("Then foo named name", true, stepPattern, method, steps);

        // Then
        assertIsOutputEqualTo("Step 'Then foo named name' matches pattern '" + stepPattern
                + "' for method 'public void org.jbehave.core.steps.PrintStreamStepMonitorBehaviour$MySteps.thenFoo(java.lang.String)' "
                + "with annotations '[@org.jbehave.core.annotations.Then(priority=0, value=foo named $name)]' in steps instance '"
                + steps + "'");
    }

    @Test
    public void shouldReportConvertedValueOfType() {
        // When
        monitor.convertedValueOfType("1", int.class, 1, ParameterConverters.NumberConverter.class);

        // Then
        assertIsOutputEqualTo("Converted value '1' of type 'int' to '1' with converter "
                + "'class org.jbehave.core.steps.ParameterConverters$NumberConverter'");
    }

    @Test
    public void shouldReportBeforePerformingStep() {
        // When
        monitor.beforePerforming("a step", false, null);

        // Then
        assertIsOutputEqualTo("Performing step 'a step'");
    }

    @Test
    public void shouldReportNothingAfterBeforePerformingStep() {
        // When
        monitor.afterPerforming("a step", false, null);

        // Then
        assertThat(out.toString(), equalTo(""));
    }

    @Test
    public void shouldReportUsingAnnotatedNameForParameter() {
        // When
        monitor.usingAnnotatedNameForParameter("name", 0);

        // Then
        assertIsOutputEqualTo("Using annotated name 'name' for parameter position 0");
    }

    @Test
    public void shouldReportUsingParameterNameForParameter() {
        // When
        monitor.usingParameterNameForParameter("name", 0);

        // Then
        assertIsOutputEqualTo("Using parameter name 'name' for parameter position 0");
    }

    @Test
    public void shouldReportUsingTableAnnotatedNameForParameter() {
        // When
        monitor.usingTableAnnotatedNameForParameter("name", 0);

        // Then
        assertIsOutputEqualTo("Using table annotated name 'name' for parameter position 0");
    }

    @Test
    public void shouldReportUsingTableParameterNameForParameter() {
        // When
        monitor.usingTableParameterNameForParameter("name", 0);

        // Then
        assertIsOutputEqualTo("Using table parameter name 'name' for parameter position 0");
    }

    @Test
    public void shouldReportUsingNaturalOrderForParameter() {
        // When
        monitor.usingNaturalOrderForParameter(0);

        // Then
        assertIsOutputEqualTo("Using natural order for parameter position 0");
    }

    @Test
    public void shouldReportFoundParameter() {
        // When
        monitor.foundParameter("parameter", 0);

        // Then
        assertIsOutputEqualTo("Found parameter 'parameter' for position 0");
    }

    @Test
    public void shouldReportUsingStepsContextParameter() {
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
