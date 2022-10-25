package org.jbehave.core.reporters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCreator;
import org.jbehave.core.steps.StepCreator.PendingStep;
import org.jbehave.core.steps.Timing;

/**
 * Filters out the reports from all stories that pass, The delegate receives
 * output only for failing or pending stories.
 */
public class SilentSuccessFilter extends NullStoryReporter {

    private final StoryReporter delegate;
    private State runState = State.SILENT;
    private State beforeStoryState = State.SILENT;
    private State afterStoryState = State.SILENT;
    private State scenarioState = State.SILENT;
    private List<Todo> scenarioTodos = new ArrayList<>();
    private boolean givenStory;

    public SilentSuccessFilter(StoryReporter delegate) {
        this.delegate = delegate;
    }

    @Override
    public void dryRun() {
        runState = createState(delegate::dryRun);
        runState.report();
    }
    
    @Override
    public void pendingMethods(final List<String> methods) {
    }

    @Override
    public void beforeStory(final Story story, final boolean givenStory) {
        this.givenStory = givenStory;
        beforeStoryState = createState(() -> {
            delegate.beforeStory(story, givenStory);
            beforeStoryState = State.SILENT;
        });
    }

    @Override
    public void narrative(final Narrative narrative) {
        beforeStoryState = createState(() -> delegate.narrative(narrative));
        beforeStoryState.report();
    }

    @Override
    public void lifecycle(final Lifecycle lifecycle) {
        beforeStoryState = createState(() -> delegate.lifecycle(lifecycle));
        beforeStoryState.report();
    }

    @Override
    public void storyExcluded(final Story story, final String filter) {
        beforeStoryState = createState(() -> delegate.storyExcluded(story, filter));
        beforeStoryState.report();
    }

    @Override
    public void afterStory(boolean givenStory) {
        afterStoryState.report();
    }
    
    @Override
    public void ignorable(final String step) {
        addScenarioTodo(() -> delegate.ignorable(step));
    }

    @Override
    public void comment(final String step) {
        addScenarioTodo(() -> delegate.comment(step));
    }

    @Override
    public void failed(final String step, final Throwable cause) {
        addScenarioTodo(() -> delegate.failed(step, cause));
        setStateToNoisy();
    }

    @Override
    public void failedOutcomes(final String step, final OutcomesTable table) {
        addScenarioTodo(() -> delegate.failedOutcomes(step, table));
        setStateToNoisy();
    }

    @Override
    public void notPerformed(final String step) {
        addScenarioTodo(() -> delegate.notPerformed(step));
        setStateToNoisy();
    }

    @Override
    public void pending(PendingStep step) {
        addScenarioTodo(() -> delegate.pending(step));
        setStateToNoisy();
    }

    @Override
    public void pending(final String step) {
        pending((PendingStep) StepCreator.createPendingStep(step, null));
    }

    @Override
    public void successful(final String step) {
        addScenarioTodo(() -> delegate.successful(step));
    }

    @Override
    public void afterScenario(Timing timing) {
        addScenarioTodo(() -> delegate.afterScenario(timing));
        scenarioState.report();
    }

    @Override
    public void beforeScenario(final Scenario scenario) {
        scenarioTodos = new ArrayList<>();
        addScenarioTodo(() -> delegate.beforeScenario(scenario));
    }

    @Override
    public void scenarioExcluded(final Scenario scenario, final String filter) {
        scenarioState = createState(() -> delegate.scenarioExcluded(scenario, filter));
        scenarioState.report();
    }

    @Override
    public void givenStories(final GivenStories givenStories) {
        addScenarioTodo(() -> delegate.givenStories(givenStories));
    }

    @Override
    public void givenStories(final List<String> storyPaths) {
        addScenarioTodo(() -> delegate.givenStories(storyPaths));
    }

    @Override
    public void beforeExamples(final List<String> steps, final ExamplesTable table) {
        addScenarioTodo(() -> delegate.beforeExamples(steps, table));
    }

    @Override
    public void example(final Map<String, String> tableRow, final int exampleIndex) {
        addScenarioTodo(() -> delegate.example(tableRow, exampleIndex));
    }

    @Override
    public void afterExamples() {
        addScenarioTodo(() -> delegate.afterExamples());
    }

    private void addScenarioTodo(Runnable todoAction) {
        scenarioTodos.add(new Todo() {
            @Override
            public void doNow() {
                todoAction.run();
            }
        });
    }

    private State createState(Runnable stateReporter) {
        return new State() {
            @Override
            public void report() {
                stateReporter.run();
            }
        };
    }

    private static interface Todo {
        void doNow();
    }

    private interface State {
        State SILENT = new State() {
            @Override
            public void report() {
            }
        };

        void report();
    }

    private void setStateToNoisy() {
        scenarioState = new State() {
            @Override
            public void report() {
                beforeStoryState.report();
                for (Todo todo : scenarioTodos) {
                    todo.doNow();
                }
                afterStoryState = new State() {
                    @Override
                    public void report() {
                        delegate.afterStory(givenStory);
                        afterStoryState = State.SILENT;
                    }
                };
                scenarioState = State.SILENT;
            }
        };
    }
}
