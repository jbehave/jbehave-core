package org.jbehave.core.reporters;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Reporter which collects other {@link StoryReporter}s and delegates all
 * invocations to the collected reporters.
 * 
 * @author Mirko FriedenHagen
 */
public class DelegatingStoryReporter implements StoryReporter {

    private final Collection<StoryReporter> delegates;

    /**
     * Creates DelegatingStoryReporter with a given collections of delegates
     * 
     * @param delegates the ScenarioReporters to delegate to
     */
    public DelegatingStoryReporter(Collection<StoryReporter> delegates) {
        this.delegates = delegates;
    }

    /**
     * Creates DelegatingStoryReporter with a given varargs of delegates
     * 
     * @param delegates the StoryReporters to delegate to
     */
    public DelegatingStoryReporter(StoryReporter... delegates) {
        this(asList(delegates));
    }

    @Override
    public void afterScenario() {
        for (StoryReporter reporter : delegates) {
            reporter.afterScenario();
        }
    }

    @Override
    public void afterStory(boolean givenStory) {
        for (StoryReporter reporter : delegates) {
            reporter.afterStory(givenStory);
        }
    }

    @Override
    public void beforeScenario(Scenario scenario) {
        for (StoryReporter reporter : delegates) {
            reporter.beforeScenario(scenario);
        }
    }

    @Override
    public void beforeScenario(String scenarioTitle) {
        for (StoryReporter reporter : delegates) {
            reporter.beforeScenario(scenarioTitle);
        }
    }

    @Override
    public void scenarioMeta(Meta meta) {
        for (StoryReporter reporter : delegates) {
            reporter.scenarioMeta(meta);
        }
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        for (StoryReporter reporter : delegates) {
            reporter.beforeStory(story, givenStory);
        }
    }

    @Override
    public void narrative(final Narrative narrative) {
        for (StoryReporter reporter : delegates) {
            reporter.narrative(narrative);
        }
    }

    @Override
    public void lifecyle(Lifecycle lifecycle) {
        for (StoryReporter reporter : delegates) {
            reporter.lifecyle(lifecycle);
        }
    }

    @Override
    public void beforeExamples(List<String> steps, ExamplesTable table) {
        for (StoryReporter reporter : delegates) {
            reporter.beforeExamples(steps, table);
        }
    }

    @Override
    public void example(Map<String, String> tableRow) {
        for (StoryReporter reporter : delegates) {
            reporter.example(tableRow);
        }
    }

    @Override
    public void example(Map<String, String> tableRow, int exampleIndex) {
        for (StoryReporter reporter : delegates) {
            reporter.example(tableRow, exampleIndex);
        }
    }

    @Override
    public void afterExamples() {
        for (StoryReporter reporter : delegates) {
            reporter.afterExamples();
        }
    }

    @Override
    public void failed(String step, Throwable cause) {
        for (StoryReporter reporter : delegates) {
            reporter.failed(step, cause);
        }
    }

    @Override
    public void failedOutcomes(String step, OutcomesTable table) {
        for (StoryReporter reporter : delegates) {
            reporter.failedOutcomes(step, table);
        }
    }

    @Override
    public void beforeGivenStories() {
        for (StoryReporter reporter : delegates) {
            reporter.beforeGivenStories();
        }
    }

    @Override
    public void givenStories(GivenStories givenStories) {
        for (StoryReporter reporter : delegates) {
            reporter.givenStories(givenStories);
        }
    }

    @Override
    public void givenStories(List<String> storyPaths) {
        for (StoryReporter reporter : delegates) {
            reporter.givenStories(storyPaths);
        }
    }

    @Override
    public void afterGivenStories() {
        for (StoryReporter reporter : delegates) {
            reporter.afterGivenStories();
        }
    }

    @Override
    public void beforeStep(String step) {
        for (StoryReporter reporter : delegates) {
            reporter.beforeStep(step);
        }
    }

    @Override
    public void ignorable(String step) {
        for (StoryReporter reporter : delegates) {
            reporter.ignorable(step);
        }
    }

    @Override
    public void comment(String step) {
        for (StoryReporter reporter : delegates) {
            reporter.comment(step);
        }
    }

    @Override
    public void notPerformed(String step) {
        for (StoryReporter reporter : delegates) {
            reporter.notPerformed(step);
        }
    }

    @Override
    public void pending(String step) {
        for (StoryReporter reporter : delegates) {
            reporter.pending(step);
        }
    }

    @Override
    public void successful(String step) {
        for (StoryReporter reporter : delegates) {
            reporter.successful(step);
        }
    }

    @Override
    public void scenarioNotAllowed(Scenario scenario, String filter) {
        for (StoryReporter reporter : delegates) {
            reporter.scenarioNotAllowed(scenario, filter);
        }
    }

    @Override
    public void storyNotAllowed(Story story, String filter) {
        for (StoryReporter reporter : delegates) {
            reporter.storyNotAllowed(story, filter);
        }
    }

    @Override
    public void dryRun() {
        for (StoryReporter reporter : delegates) {
            reporter.dryRun();
        }
    }
    
    @Override
    public void pendingMethods(List<String> methods) {
        for (StoryReporter reporter : delegates) {
            reporter.pendingMethods(methods);
        }
    }

    @Override
    public void restarted(String step, Throwable cause) {
        for (StoryReporter reporter : delegates) {
            reporter.restarted(step, cause);
        }
    }
    
    @Override
    public void restartedStory(Story story, Throwable cause) {
        for (StoryReporter reporter : delegates) {
            reporter.restartedStory(story, cause);
        }
    }

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration) {
        for (StoryReporter reporter : delegates) {
            reporter.storyCancelled(story, storyDuration);
        }
    }

    public Collection<StoryReporter> getDelegates() {
        return delegates;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
