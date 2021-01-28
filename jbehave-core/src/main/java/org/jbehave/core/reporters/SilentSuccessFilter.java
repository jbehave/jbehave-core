package org.jbehave.core.reporters;

import org.jbehave.core.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        runState = new State(){
            @Override
            public void report(){
                delegate.dryRun();
            }
        };
        runState.report();
    }
    
    @Override
    public void pendingMethods(final List<String> methods) {
        runState = new State(){
            @Override
            public void report(){
                delegate.pendingMethods(methods);
            }
        };
        runState.report();
    }

    @Override
    public void beforeStory(final Story story, final boolean givenStory) {
        this.givenStory = givenStory;
        beforeStoryState = new State() {
            @Override
            public void report() {
                delegate.beforeStory(story, givenStory);
                beforeStoryState = State.SILENT;
            }
        };
    }

    @Override
    public void narrative(final Narrative narrative) {
        beforeStoryState = new State() {
            @Override
            public void report() {
                delegate.narrative(narrative);
            }
        };
        beforeStoryState.report();
    }

    @Override
    public void lifecyle(final Lifecycle lifecycle) {
        beforeStoryState = new State() {
            @Override
            public void report() {
                delegate.lifecyle(lifecycle);
            }
        };
        beforeStoryState.report();
    }

    @Override
    public void storyNotAllowed(final Story story, final String filter) {
        beforeStoryState = new State() {
            @Override
            public void report() {
                delegate.storyNotAllowed(story, filter);
            }
        };
        beforeStoryState.report();
    }

    @Override
    public void afterStory(boolean givenStory) {
        afterStoryState.report();
    }
    
    @Override
    public void ignorable(final String step) {
        scenarioTodos.add(new Todo() {
            @Override
            public void doNow() {
                delegate.ignorable(step);
            }
        });
    }

    @Override
    public void comment(final String step) {
        scenarioTodos.add(new Todo() {
            @Override
            public void doNow() {
                delegate.comment(step);
            }
        });
    }

    @Override
    public void failed(final String step, final Throwable cause) {
        scenarioTodos.add(new Todo() {
            @Override
            public void doNow() {
                delegate.failed(step, cause);
            }
        });
        setStateToNoisy();
    }

    @Override
    public void failedOutcomes(final String step, final OutcomesTable table) {
        scenarioTodos.add(new Todo() {
            @Override
            public void doNow() {
                delegate.failedOutcomes(step, table);
            }
        });
        setStateToNoisy();
    }

    @Override
    public void notPerformed(final String step) {
        scenarioTodos.add(new Todo() {
            @Override
            public void doNow() {
                delegate.notPerformed(step);
            }
        });
        setStateToNoisy();
    }

    @Override
    public void pending(final String step) {
        scenarioTodos.add(new Todo() {
            @Override
            public void doNow() {
                delegate.pending(step);
            }
        });
        setStateToNoisy();
    }

    @Override
    public void successful(final String step) {
        scenarioTodos.add(new Todo() {
            @Override
            public void doNow() {
                delegate.successful(step);
            }
        });
    }

    @Override
    public void afterScenario() {
        scenarioTodos.add(new Todo() {
            @Override
            public void doNow() {
                delegate.afterScenario();
            }
        });
        scenarioState.report();
    }

    @Override
    public void beforeScenario(final Scenario scenario) {
        scenarioTodos = new ArrayList<>();
        scenarioTodos.add(new Todo() {
            @Override
            public void doNow() {
                delegate.beforeScenario(scenario);
            }
        });
    }

    @Override
    public void scenarioNotAllowed(final Scenario scenario, final String filter) {
        scenarioState = new State() {
            @Override
            public void report() {
                delegate.scenarioNotAllowed(scenario, filter);
            }
        };
        scenarioState.report();
    }

    @Override
    public void givenStories(final GivenStories givenStories) {
        scenarioTodos.add(new Todo() {
            @Override
            public void doNow() {
                delegate.givenStories(givenStories);
            }
        });
    }

    @Override
    public void givenStories(final List<String> storyPaths) {
        scenarioTodos.add(new Todo() {
            @Override
            public void doNow() {
                delegate.givenStories(storyPaths);
            }
        });
    }

    @Override
    public void beforeExamples(final List<String> steps, final ExamplesTable table) {
        scenarioTodos.add(new Todo() {
            @Override
            public void doNow() {
                delegate.beforeExamples(steps, table);
            }
        });
    }

    @Override
    public void example(final Map<String, String> tableRow, final int exampleIndex) {
        scenarioTodos.add(new Todo() {
            @Override
            public void doNow() {
                delegate.example(tableRow, exampleIndex);
            }
        });
    }

    @Override
    public void afterExamples() {
        scenarioTodos.add(new Todo() {
            @Override
            public void doNow() {
                delegate.afterExamples();
            }
        });
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
