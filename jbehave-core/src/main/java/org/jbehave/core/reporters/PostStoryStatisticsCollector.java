package org.jbehave.core.reporters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

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
    private final List<String> events = asList("notAllowed", "pending", "scenariosNotAllowed",
            "givenStoryScenariosNotAllowed", "steps", "stepsSuccessful", "stepsIgnorable", "stepsPending",
            "stepsNotPerformed", "stepsFailed", "currentScenarioSteps", "currentScenarioStepsPending", "scenarios",
            "scenariosSuccessful", "scenariosPending", "scenariosFailed", "givenStories", "givenStoryScenarios",
            "givenStoryScenariosSuccessful", "givenStoryScenariosPending", "givenStoryScenariosFailed", "examples");

    private Throwable cause;
    private OutcomesTable outcomesFailed;
    private int givenStories;
    private long storyStartTime;
    private boolean currentScenarioNotAllowed;

    public PostStoryStatisticsCollector(OutputStream output) {
        this.output = output;
    }

    public void successful(String step) {
        add("steps");
        add("stepsSuccessful");
        add("currentScenarioSteps");
    }

    public void ignorable(String step) {
        add("steps");
        add("stepsIgnorable");
        add("currentScenarioSteps");
    }

    public void pending(String step) {
        add("steps");
        add("stepsPending");
        add("currentScenarioSteps");
        add("currentScenarioStepsPending");
    }

    public void notPerformed(String step) {
        add("steps");
        add("stepsNotPerformed");
        add("currentScenarioSteps");
    }

    public void failed(String step, Throwable cause) {
        this.cause = cause;
        add("steps");
        add("stepsFailed");
        add("currentScenarioSteps");
    }

    public void failedOutcomes(String step, OutcomesTable table) {
        this.outcomesFailed = table;
        add("steps");
        add("stepsFailed");
        add("currentScenarioSteps");
    }

    public void beforeStory(Story story, boolean givenStory) {
        if (givenStory) {
            this.givenStories++;
        }

        if (!givenStory) {
            resetData();
            storyStartTime = System.currentTimeMillis();
        }
    }

    public void narrative(final Narrative narrative) {
    }

    public void storyNotAllowed(Story story, String filter) {
        resetData();
        add("notAllowed");
        writeData();
    }

    public void afterStory(boolean givenStory) {
        if (givenStory) {
            this.givenStories--;
        } else {
            if (has("scenariosPending") || has("givenStoryScenariosPending")) {
                add("pending");
            }
            int duration = (int)(System.currentTimeMillis() - storyStartTime);
            data.put("duration", duration);
            writeData();
        }
    }

    public void givenStories(GivenStories givenStories) {
        add("givenStories");
    }

    public void givenStories(List<String> storyPaths) {
        add("givenStories");
    }

    public void beforeScenario(String title) {
        cause = null;
        outcomesFailed = null;
        currentScenarioNotAllowed = false;
        reset("currentScenarioSteps");
        reset("currentScenarioStepsPending");
    }

    public void scenarioNotAllowed(Scenario scenario, String filter) {
        if (givenStories > 0) {
            add("givenStoryScenariosNotAllowed");
        } else {
            add("scenariosNotAllowed");
            currentScenarioNotAllowed = true;
        }
    }

    public void scenarioMeta(Meta meta) {
    }

    public void afterScenario() {
        if (givenStories > 0) {
            countScenarios("givenStoryScenarios");
        } else {
            countScenarios("scenarios");
        }
        if (has("currentScenarioStepsPending") || (!has("currentScenarioSteps") && !currentScenarioNotAllowed)) {
            if (givenStories > 0) {
                add("givenStoryScenariosPending");
            } else {
                add("scenariosPending");
            }
        }
    }

    private void countScenarios(String namespace) {
        add(namespace);
        if (!currentScenarioNotAllowed){
	        if (cause != null || outcomesFailed != null) {
	            add(namespace + "Failed");
	        } else {
	            add(namespace + "Successful");
	        }
        }
    }

    public void beforeExamples(List<String> steps, ExamplesTable table) {
    }

    public void example(Map<String, String> tableRow) {
        add("examples");
    }

    public void afterExamples() {
    }

    public void dryRun() {
    }

    public void pendingMethods(List<String> methods) {
    }

    public void restarted(String step, Throwable cause) {
    }

    public void cancelled() {
    }

    private void add(String event) {
        Integer count = data.get(event);
        if (count == null) {
            count = 0;
        }
        count++;
        data.put(event, count);
    }

    private boolean has(String event) {
        Integer count = data.get(event);
        if (count == null) {
            count = 0;
        }
        return count > 0;
    }

    private void writeData() {
        Properties p = new Properties();
        for (String event : data.keySet()) {
            if (!event.startsWith("current")) {
                p.setProperty(event, data.get(event).toString());
            }
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
            reset(event);
        }
    }

    private void reset(String event) {
        data.put(event, 0);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(output).append(data).toString();
    }

}
