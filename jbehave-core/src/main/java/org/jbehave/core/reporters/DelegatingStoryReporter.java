package org.jbehave.core.reporters;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

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

    public void afterScenario(boolean givenStory) {
        for (StoryReporter reporter : delegates) {
            reporter.afterScenario(givenStory);
        }
    }

    public void afterStory(boolean givenStory) {
        for (StoryReporter reporter : delegates) {
            reporter.afterStory(givenStory);
        }
    }

    public void beforeScenario(String scenarioTitle, boolean givenStory) {
        for (StoryReporter reporter : delegates) {
            reporter.beforeScenario(scenarioTitle, givenStory);
        }
    }

    public void scenarioMeta(Meta meta, boolean givenStory) {
        for (StoryReporter reporter : delegates) {
            reporter.scenarioMeta(meta, givenStory);
        }
    }

    public void beforeStory(Story story, boolean givenStory) {
        for (StoryReporter reporter : delegates) {
            reporter.beforeStory(story, givenStory);
        }
    }

    public void beforeExamples(List<String> steps, ExamplesTable table) {
        for (StoryReporter reporter : delegates) {
            reporter.beforeExamples(steps, table);
        }
    }

    public void example(Map<String, String> tableRow) {
        for (StoryReporter reporter : delegates) {
            reporter.example(tableRow);
        }
    }

    public void afterExamples() {
        for (StoryReporter reporter : delegates) {
            reporter.afterExamples();
        }
    }

    public void failed(String step, Throwable cause) {
        for (StoryReporter reporter : delegates) {
            reporter.failed(step, cause);
        }
    }

    public void failedOutcomes(String step, OutcomesTable table) {
        for (StoryReporter reporter : delegates) {
            reporter.failedOutcomes(step, table);
        }
    }

    public void givenStories(GivenStories givenStories) {
        for (StoryReporter reporter : delegates) {
            reporter.givenStories(givenStories);
        }
    }

    public void givenStories(List<String> storyPaths) {
        for (StoryReporter reporter : delegates) {
            reporter.givenStories(storyPaths);
        }
    }

    public void ignorable(String step) {
        for (StoryReporter reporter : delegates) {
            reporter.ignorable(step);
        }
    }
    
    public void notPerformed(String step) {
        for (StoryReporter reporter : delegates) {
            reporter.notPerformed(step);
        }
    }

    public void pending(String step) {
        for (StoryReporter reporter : delegates) {
            reporter.pending(step);
        }
    }

    public void successful(String step) {
        for (StoryReporter reporter : delegates) {
            reporter.successful(step);
        }
    }

	public void dryRun() {
        for (StoryReporter reporter : delegates) {
            reporter.dryRun();
        }
	}
	
    public void scenarioNotAllowed(Scenario scenario, String filter, boolean givenStory) {
        for (StoryReporter reporter : delegates) {
            reporter.scenarioNotAllowed(scenario, filter, givenStory);
        }
    }

    public void storyNotAllowed(Story story, String filter) {
        for (StoryReporter reporter : delegates) {
            reporter.storyNotAllowed(story, filter);
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
