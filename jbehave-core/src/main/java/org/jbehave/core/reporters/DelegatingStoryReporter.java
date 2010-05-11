package org.jbehave.core.reporters;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.Story;

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

    public void afterScenario() {
        for (StoryReporter reporter : delegates) {
            reporter.afterScenario();
        }
    }

    public void afterStory(boolean embeddedStory) {
        for (StoryReporter reporter : delegates) {
            reporter.afterStory(embeddedStory);
        }
    }

    public void beforeScenario(String title) {
        for (StoryReporter reporter : delegates) {
            reporter.beforeScenario(title);
        }
    }

    public void beforeStory(Story story, boolean embeddedStory) {
        for (StoryReporter reporter : delegates) {
            reporter.beforeStory(story, embeddedStory);
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

    public void failed(String step, Throwable e) {
        for (StoryReporter reporter : delegates) {
            reporter.failed(step, e);
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

    public Collection<StoryReporter> getDelegates() {
        return delegates;
    }

    @Override
    public String toString() {
    	return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
