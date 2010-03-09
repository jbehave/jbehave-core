package org.jbehave.scenario.reporters;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jbehave.scenario.definition.Blurb;
import org.jbehave.scenario.definition.ExamplesTable;
import org.jbehave.scenario.definition.StoryDefinition;

/**
 * <p>
 * Scenario reporter that collects statistics and stores them as properties to
 * output stream
 * </p>
 */
public class StatisticsScenarioReporter implements ScenarioReporter {

    private final OutputStream output;
    private final Map<String, Integer> data = new HashMap<String, Integer>();
    private final List<String> events = asList("steps", "stepsSuccessful", "stepsIgnorable", "stepsPending",
            "stepsNotPerformed", "stepsFailed", "scenarios", "scenariosFailed", "givenScenarios", "examples");

    private Throwable cause;

    public StatisticsScenarioReporter(OutputStream output) {
        this.output = output;
    }

    public void successful(String step) {
        count("steps");
        count("stepsSuccessful");
    }

    public void ignorable(String step) {
        count("steps");
        count("stepsIgnorable");
    }

    public void pending(String step) {
        count("steps");
        count("stepsPending");
    }

    public void notPerformed(String step) {
        count("steps");
        count("stepsNotPerformed");
    }

    public void failed(String step, Throwable cause) {
        this.cause = cause;
        count("steps");
        count("stepsFailed");
    }

    public void beforeStory(StoryDefinition story, boolean embeddedStory) {
        resetData();
    }

    public void beforeStory(Blurb blurb) {
        beforeStory(new StoryDefinition(blurb), false);
    }

    public void afterStory(boolean embeddedStory) {
        writeData();
    }

    public void afterStory() {
        afterStory(false);
    }

    public void givenScenarios(List<String> givenScenarios) {
        count("givenScenarios");
    }

    public void beforeScenario(String title) {
        cause = null;
    }

    public void afterScenario() {
        count("scenarios");
        if (cause != null) {
            count("scenariosFailed");
        }
    }

    public void beforeExamples(List<String> steps, ExamplesTable table) {
    }

    public void example(Map<String, String> tableRow) {
        count("examples");
    }

    public void afterExamples() {
    }

    public void examplesTable(ExamplesTable table) {
        beforeExamples(new ArrayList<String>(), table);
    }

    public void examplesTableRow(Map<String, String> tableRow) {
        example(tableRow);
    }

    private void count(String event) {
        Integer count = data.get(event);
        if (count == null) {
            count = 0;
        }
        count++;
        data.put(event, count);
    }

    private void writeData() {
        Properties p = new Properties();
        for (String event : data.keySet()) {
            p.setProperty(event, data.get(event).toString());
        }
        try {
            p.store(output, this.getClass().getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void resetData() {
        data.clear();
        for (String event : events) {
            data.put(event, 0);
        }
    }

}
