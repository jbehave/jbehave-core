package org.jbehave.scenario.reporters;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jbehave.scenario.definition.Blurb;
import org.jbehave.scenario.definition.ExamplesTable;
import org.jbehave.scenario.definition.StoryDefinition;

/**
 * Reporter which collects other {@link ScenarioReporter}s and delegates all
 * invocations to the collected reporters.
 * 
 * @author Mirko FriedenHagen
 */
public class DelegatingScenarioReporter implements ScenarioReporter {

    private final Collection<ScenarioReporter> delegates;

    /**
     * Creates DelegatingScenarioReporter with a given collections of delegates
     * 
     * @param delegates the ScenarioReporters to delegate to
     */
    public DelegatingScenarioReporter(Collection<ScenarioReporter> delegates) {
        this.delegates = delegates;
    }

    /**
     * Creates DelegatingScenarioReporter with a given varargs of delegates
     * 
     * @param delegates the ScenarioReporters to delegate to
     */
    public DelegatingScenarioReporter(ScenarioReporter... delegates) {
        this(asList(delegates));
    }

    public void afterScenario() {
        for (ScenarioReporter reporter : delegates) {
            reporter.afterScenario();
        }
    }

    public void afterStory(boolean embeddedStory) {
        for (ScenarioReporter reporter : delegates) {
            reporter.afterStory(embeddedStory);
        }
    }

    public void afterStory() {
        for (ScenarioReporter reporter : delegates) {
            reporter.afterStory();
        }
    }

    public void beforeScenario(String title) {
        for (ScenarioReporter reporter : delegates) {
            reporter.beforeScenario(title);
        }
    }

    public void beforeStory(StoryDefinition story, boolean embeddedStory) {
        for (ScenarioReporter reporter : delegates) {
            reporter.beforeStory(story, embeddedStory);
        }
    }

    public void beforeStory(Blurb blurb) {
        for (ScenarioReporter reporter : delegates) {
            reporter.beforeStory(blurb);
        }
    }

    public void beforeExamples(List<String> steps, ExamplesTable table) {
        for (ScenarioReporter reporter : delegates) {
            reporter.beforeExamples(steps, table);
        }
    }

    public void example(Map<String, String> tableRow) {
        for (ScenarioReporter reporter : delegates) {
            reporter.example(tableRow);
        }
    }

    public void afterExamples() {
        for (ScenarioReporter reporter : delegates) {
            reporter.afterExamples();
        }
    }

    public void examplesTable(ExamplesTable table) {
        beforeExamples(new ArrayList<String>(), table);
    }

    public void examplesTableRow(Map<String, String> tableRow) {
        example(tableRow);
    }

    public void failed(String step, Throwable e) {
        for (ScenarioReporter reporter : delegates) {
            reporter.failed(step, e);
        }
    }

    public void givenScenarios(List<String> givenScenarios) {
        for (ScenarioReporter reporter : delegates) {
            reporter.givenScenarios(givenScenarios);
        }
    }

    public void ignorable(String step) {
        for (ScenarioReporter reporter : delegates) {
            reporter.ignorable(step);
        }
    }
    
    public void notPerformed(String step) {
        for (ScenarioReporter reporter : delegates) {
            reporter.notPerformed(step);
        }
    }

    public void pending(String step) {
        for (ScenarioReporter reporter : delegates) {
            reporter.pending(step);
        }
    }

    public void successful(String step) {
        for (ScenarioReporter reporter : delegates) {
            reporter.successful(step);
        }
    }

    public Collection<ScenarioReporter> getDelegates() {
        return delegates;
    }

}
