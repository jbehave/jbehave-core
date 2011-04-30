package org.jbehave.core.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class PrintStreamStepMonitorBehaviour {

    @Test
    public void shouldReportStepEventsToMonitor() {
        // Given
        OutputStream out = new ByteArrayOutputStream();
        StepMonitor monitor = new PrintStreamStepMonitor(new PrintStream(out));

        // When
        monitor.performing("a step", false);
        monitor.foundParameter("parameter", 0);

        // Then
        assertThat(out.toString(), containsString("Performing step 'a step'"));
        assertThat(out.toString(), containsString("Found parameter 'parameter' for position 0"));
    }

  
}
