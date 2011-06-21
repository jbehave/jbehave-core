package org.jbehave.core.reporters;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.jbehave.core.reporters.ANSIColor.MAGENTA;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_START;
import static org.junit.Assert.assertThat;

public class ColoredConsoleOutputBehaviour {
    private ColoredConsoleOutput output;

    @Before
    public void setUp() throws Exception {
        output = new ColoredConsoleOutput();
    }

    @Test
    public void shouldNotOutputAnyColorForAnUnknownOutputType() throws Exception {
        final String formattedOutput = output.format("unknown", "unknown");

        assertThat(formattedOutput, is("unknown"));
    }

    @Test
    public void shouldOutputMessageInGreenForSuccessfulOutput() throws Exception {
        output.overwritePattern("successful", "{0}");

        final String outputForSuccess = output.format("successful", "", "success");

        assertThat(outputForSuccess, is("\033[32msuccess\033[0m"));
    }

    @Test
    public void shouldOutputMessageInYellowForPendingOutputs() throws Exception {
        output.overwritePattern("pending", "{0}");
        output.overwritePattern("pendingMethod", "{0}");
        output.overwritePattern("notPerformed", "{0}");

        final String outputForPending = output.format("pending", "", "pending");
        final String outputForPendingMethod = output.format("pendingMethod", "", "pending method");
        final String outputForNotPerformed = output.format("notPerformed", "", "not performed");

        assertThat(outputForPending, is("\033[33mpending\033[0m"));
        assertThat(outputForPendingMethod, is("\033[33mpending method\033[0m"));
        assertThat(outputForNotPerformed, is("\033[33mnot performed\033[0m"));
    }

    @Test
    public void shouldOutputMessageInCyanForIgnorableOutput() throws Exception {
        output.overwritePattern("ignorable", "{0}");

        final String outputForSuccess = output.format("ignorable", "", "ignore");

        assertThat(outputForSuccess, is("\033[36mignore\033[0m"));
    }

    @Test
    public void shouldOutputMessageInRedForFailedOutput() throws Exception {
        output.overwritePattern("failed", "{0}");

        final String outputForSuccess = output.format("failed", "", "failure");

        assertThat(outputForSuccess, is("\033[31mfailure\033[0m"));
    }

    @Test
    public void shouldBoldifyParamsThatAreDemarcatedWithStartEndValues() throws Exception {
        output.assignColorToEvent("params", MAGENTA);
        output.overwritePattern("params", "{0} and {1}");

        final String outputWithValuesDemarcated = output.format("params", "", value("one"), value("two"));

        assertThat(outputWithValuesDemarcated, is("\033[35m\033[1;35mone\033[0;35m and \033[1;35mtwo\033[0;35m\033[0m"));
    }

    private String value(String str) {
        return PARAMETER_VALUE_START + str + PARAMETER_VALUE_END;
    }
}
