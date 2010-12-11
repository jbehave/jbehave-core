package org.jbehave.core.reporters;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.util.Arrays.asList;

/**
 * <p>
 * Reporter that collects statistics and writes them as properties to output
 * stream after each story
 * </p>
 */
public class PostStoryStatisticsCollector implements StoryReporter {

    private final OutputStream output;
    private final Map<String, Integer> data = new HashMap<String, Integer>();
    private final List<String> events = asList("notAllowed", "scenariosNotAllowed", "steps", "stepsSuccessful",
            "stepsIgnorable", "stepsPending", "stepsNotPerformed", "stepsFailed", "scenarios", "scenariosSuccessful",
            "scenariosFailed", "givenStories", "examples");

    private Throwable cause;
    private OutcomesTable outcomesFailed;

    public PostStoryStatisticsCollector(OutputStream output) {
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

    public void failedOutcomes(String step, OutcomesTable table) {
        this.outcomesFailed = table;
        count("steps");
        count("stepsFailed");
    }

    public void beforeStory(Story story, boolean givenStory) {
        if (givenStory) {
            return;
        }
        resetData();
    }

    public void storyNotAllowed(Story story, String filter) {
        resetData();
        count("notAllowed");
        writeData();
    }

    public void afterStory(boolean givenStory) {
        if (givenStory) {
            return;
        }
        writeData();
    }

    public void givenStories(GivenStories givenStories) {
        count("givenStories");
    }

    public void givenStories(List<String> storyPaths) {
        count("givenStories");
    }

    public void beforeScenario(String title, boolean givenStory) {
        cause = null;
        outcomesFailed = null;
    }

    public void scenarioNotAllowed(Scenario scenario, String filter, boolean givenStory) {
        count("scenariosNotAllowed");
    }

    public void scenarioMeta(Meta meta, boolean givenStory) {
    }

    public void afterScenario(boolean givenStory) {
        if (givenStory) {
            return;
        }

        count("scenarios");
        if (cause != null || outcomesFailed != null) {
            count("scenariosFailed");
        } else {
            count("scenariosSuccessful");
        }
    }

    public void beforeExamples(List<String> steps, ExamplesTable table) {
    }

    public void example(Map<String, String> tableRow) {
        count("examples");
    }

    public void afterExamples() {
    }

    public void dryRun() {
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
            e.printStackTrace();
        }
    }

    private void resetData() {
        data.clear();
        for (String event : events) {
            data.put(event, 0);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(output).append(data).toString();
    }

}
