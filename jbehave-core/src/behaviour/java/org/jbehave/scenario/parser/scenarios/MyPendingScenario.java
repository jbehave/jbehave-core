package org.jbehave.scenario.parser.scenarios;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.jbehave.scenario.PropertyBasedConfiguration;
import org.jbehave.scenario.JUnitScenario;
import org.jbehave.scenario.reporters.PrintStreamScenarioReporter;

public class MyPendingScenario extends JUnitScenario {

    public MyPendingScenario() {
        // Making sure this doesn't output to the build while it's running
        super(new PropertyBasedConfiguration() {
            @Override
            public PrintStreamScenarioReporter forReportingScenarios() {
                return new PrintStreamScenarioReporter(new PrintStream(new ByteArrayOutputStream()));
            }
        });
    }
}