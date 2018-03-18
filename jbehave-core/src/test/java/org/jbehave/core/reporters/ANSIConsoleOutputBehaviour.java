package org.jbehave.core.reporters;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.jbehave.core.reporters.ANSIConsoleOutput.SGRCode.MAGENTA;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_END;
import static org.jbehave.core.steps.StepCreator.PARAMETER_VALUE_START;

public class ANSIConsoleOutputBehaviour {
    private ANSIConsoleOutput output;

    @Before
    public void setUp() throws Exception {
        output = new ANSIConsoleOutput();
    }

    @Test
    public void shouldNotFormatAnUnknownEventType() throws Exception {
        assertThat(output.format("unknown", "unknown"), Matchers.is("unknown"));
    }

    @Test
    public void shouldFormatInGreenForSuccessfulOutput() throws Exception {
        output.overwritePattern("successful", "{0}");

        assertThat(output.format("successful", "", "success"), Matchers.is("\033[32msuccess\033[0m"));
    }

    @Test
    public void shouldFormatInYellowForPendingOutputs() throws Exception {
        output.overwritePattern("pending", "{0}");
        output.overwritePattern("pendingMethod", "{0}");

        assertThat(output.format("pending", "", "pending"), Matchers.is("\033[33mpending\033[0m"));
        assertThat(output.format("pendingMethod", "", "pending method"), Matchers.is("\033[33mpending method\033[0m"));
    }

    @Test
    public void shouldFormatInMagentaForNotPerformedOutputs() throws Exception {
        output.overwritePattern("notPerformed", "{0}");

        assertThat(output.format("notPerformed", "", "not performed"), Matchers.is("\033[35mnot performed\033[0m"));
    }

    @Test
    public void shouldFormatInBlueForIgnorableOutput() throws Exception {
        output.overwritePattern("ignorable", "{0}");

        assertThat(output.format("ignorable", "", "ignore"), Matchers.is("\033[34mignore\033[0m"));
    }

    @Test
    public void shouldFormatInRedForFailedOutput() throws Exception {
        output.overwritePattern("failed", "{0}");

        assertThat(output.format("failed", "", "failure"), Matchers.is("\033[31mfailure\033[0m"));
    }

    @Test
    public void shouldBoldifyParamsThatAreDemarcatedWithStartEndValues() throws Exception {
        output.assignCodeToEvent("params", MAGENTA);
        output.overwritePattern("params", "{0} and {1}");

        assertThat(output.format("params", "", value("one"), value("two")), Matchers.is("\033[35m\033[1;35mone\033[0;35m and \033[1;35mtwo\033[0;35m\033[0m"));
    }

    private String value(String str) {
        return PARAMETER_VALUE_START + str + PARAMETER_VALUE_END;
    }
}
