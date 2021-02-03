package org.jbehave.core.reporters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.failures.RestartingStoryFailure;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.steps.Timing;

import static java.util.Arrays.asList;

/**
 * <p>
 * Reporter that collects statistics and writes them as properties to output
 * stream after each story
 * </p>
 */
public class PostStoryStatisticsCollector extends NullStoryReporter {

    private final OutputStream output;
    private final Map<String, Integer> data = new HashMap<>();
    private final List<String> events = asList("notAllowed", "pending", "scenariosNotAllowed",
            "givenStoryScenariosNotAllowed", "steps", "stepsSuccessful", "stepsIgnorable", "comments", "stepsPending",
            "stepsNotPerformed", "stepsFailed", "currentScenarioSteps", "currentScenarioStepsPending", "scenarios",
            "scenariosSuccessful", "scenariosPending", "scenariosFailed", "givenStories", "givenStoryScenarios",
            "givenStoryScenariosSuccessful", "givenStoryScenariosPending", "givenStoryScenariosFailed", "examples");

    private Throwable cause;
    private OutcomesTable outcomesFailed;
    private int givenStories;
    private boolean currentScenarioNotAllowed;

    public PostStoryStatisticsCollector(OutputStream output) {
        this.output = output;
    }

    @Override
    public void successful(String step) {
        add("steps");
        add("stepsSuccessful");
        add("currentScenarioSteps");
    }

    @Override
    public void ignorable(String step) {
        add("steps");
        add("stepsIgnorable");
        add("currentScenarioSteps");
    }

    @Override
    public void comment(String step) {
        add("steps");
        add("comments");
        add("currentScenarioSteps");
    }

    @Override
    public void pending(String step) {
        add("steps");
        add("stepsPending");
        add("currentScenarioSteps");
        add("currentScenarioStepsPending");
    }

    @Override
    public void notPerformed(String step) {
        add("steps");
        add("stepsNotPerformed");
        add("currentScenarioSteps");
    }

    @Override
    public void failed(String step, Throwable cause) {
        this.cause = cause;

        if (cause != null && !(cause.getCause() instanceof RestartingStoryFailure)) {
            add("steps");
            add("stepsFailed");
            add("currentScenarioSteps");
        }
    }

    @Override
    public void failedOutcomes(String step, OutcomesTable table) {
        this.outcomesFailed = table;
        add("steps");
        add("stepsFailed");
        add("currentScenarioSteps");
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        if (givenStory) {
            this.givenStories++;
        }

        if (!givenStory) {
            resetData();
        }
    }

    @Override
    public void storyNotAllowed(Story story, String filter) {
        resetData();
        add("notAllowed");
        writeData();
    }

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration) {
        add("cancelled");
    }

    @Override
    public void afterStory(boolean givenStory) {
        boolean write = false;
        if (givenStory) {
            this.givenStories--;
            if ( has("stepsFailed") ){
                add("scenariosFailed");
                write = true;
            }
        } else {
            if (has("scenariosPending") || has("givenStoryScenariosPending")) {
                add("pending");
            }
            write = true;
        }
        if ( write ) {
            writeData();
        }
    }

    @Override
    public void givenStories(GivenStories givenStories) {
        add("givenStories");
    }

    @Override
    public void givenStories(List<String> storyPaths) {
        add("givenStories");
    }

    @Override
    public void beforeScenario(Scenario scenario) {
        cause = null;
        outcomesFailed = null;
        currentScenarioNotAllowed = false;
        reset("currentScenarioSteps");
        reset("currentScenarioStepsPending");
    }

    @Override
    public void scenarioNotAllowed(Scenario scenario, String filter) {
        if (givenStories > 0) {
            add("givenStoryScenariosNotAllowed");
        } else {
            add("scenariosNotAllowed");
            currentScenarioNotAllowed = true;
        }
    }

    @Override
    public void afterScenario(Timing timing) {
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

    @Override
    public void example(Map<String, String> tableRow, int exampleIndex) {
        add("examples");
    }

    @Override
    public void restartedStory(Story story, Throwable cause) {
        resetData();
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
            output.close();
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
