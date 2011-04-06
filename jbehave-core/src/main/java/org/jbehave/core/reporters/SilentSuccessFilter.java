package org.jbehave.core.reporters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;

/**
 * Filters out the reports from all stories that pass, The delegate receives
 * output only for failing or pending stories.
 */
public class SilentSuccessFilter implements StoryReporter {

    private final StoryReporter delegate;
    private State runState = State.SILENT;
    private State beforeStoryState = State.SILENT;
    private State afterStoryState = State.SILENT;
    private State scenarioState = State.SILENT;
    private List<Todo> scenarioTodos = new ArrayList<Todo>();
    private boolean givenStory;

    public SilentSuccessFilter(StoryReporter delegate) {
        this.delegate = delegate;
    }

    public void dryRun() {
        runState = new State(){
            public void report(){
                delegate.dryRun();
            }
        };
        runState.report();
    }
    
    public void pendingMethods(final List<String> methods) {
        runState = new State(){
            public void report(){
                delegate.pendingMethods(methods);
            }
        };
        runState.report();
    }

    public void beforeStory(final Story story, final boolean givenStory) {
        this.givenStory = givenStory;
        beforeStoryState = new State() {
            public void report() {
                delegate.beforeStory(story, givenStory);
                beforeStoryState = State.SILENT;
            }
        };
    }

    public void narrative(final Narrative narrative) {
        beforeStoryState = new State() {
            public void report() {
                delegate.narrative(narrative);
            }
        };
        beforeStoryState.report();
    }


    public void storyNotAllowed(final Story story, final String filter) {
        beforeStoryState = new State() {
            public void report() {
                delegate.storyNotAllowed(story, filter);
            }
        };
        beforeStoryState.report();
    }

    public void afterStory(boolean givenStory) {
        afterStoryState.report();
    }

    public void ignorable(final String step) {
        scenarioTodos.add(new Todo() {
            public void doNow() {
                delegate.ignorable(step);
            }
        });
    }

    public void failed(final String step, final Throwable cause) {
        scenarioTodos.add(new Todo() {
            public void doNow() {
                delegate.failed(step, cause);
            }
        });
        setStateToNoisy();
    }

    public void failedOutcomes(final String step, final OutcomesTable table) {
        scenarioTodos.add(new Todo() {
            public void doNow() {
                delegate.failedOutcomes(step, table);
            }
        });
        setStateToNoisy();
    }

    public void notPerformed(final String step) {
        scenarioTodos.add(new Todo() {
            public void doNow() {
                delegate.notPerformed(step);
            }
        });
        setStateToNoisy();
    }

    public void pending(final String step) {
        scenarioTodos.add(new Todo() {
            public void doNow() {
                delegate.pending(step);
            }
        });
        setStateToNoisy();
    }

    public void successful(final String step) {
        scenarioTodos.add(new Todo() {
            public void doNow() {
                delegate.successful(step);
            }
        });
    }

    public void afterScenario() {
        scenarioTodos.add(new Todo() {
            public void doNow() {
                delegate.afterScenario();
            }
        });
        scenarioState.report();
    }

    public void beforeScenario(final String scenarioTitle) {
        scenarioTodos = new ArrayList<Todo>();
        scenarioTodos.add(new Todo() {
            public void doNow() {
                delegate.beforeScenario(scenarioTitle);
            }
        });
    }

    public void scenarioNotAllowed(final Scenario scenario, final String filter) {
        scenarioState = new State() {
            public void report() {
                delegate.scenarioNotAllowed(scenario, filter);
            }
        };
        scenarioState.report();
    }

    public void scenarioMeta(final Meta meta) {
        scenarioTodos = new ArrayList<Todo>();
        scenarioTodos.add(new Todo() {
            public void doNow() {
                delegate.scenarioMeta(meta);
            }
        });
    }

    public void givenStories(final GivenStories givenStories) {
        scenarioTodos.add(new Todo() {
            public void doNow() {
                delegate.givenStories(givenStories);
            }
        });
    }

    public void givenStories(final List<String> storyPaths) {
        scenarioTodos.add(new Todo() {
            public void doNow() {
                delegate.givenStories(storyPaths);
            }
        });
    }

    public void beforeExamples(final List<String> steps, final ExamplesTable table) {
        scenarioTodos.add(new Todo() {
            public void doNow() {
                delegate.beforeExamples(steps, table);
            }
        });
    }

    public void example(final Map<String, String> tableRow) {
        scenarioTodos.add(new Todo() {
            public void doNow() {
                delegate.example(tableRow);
            }
        });
    }

    public void afterExamples() {
        scenarioTodos.add(new Todo() {
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
            public void report() {
            }
        };

        void report();
    }

    private void setStateToNoisy() {
        scenarioState = new State() {
            public void report() {
                beforeStoryState.report();
                for (Todo todo : scenarioTodos) {
                    todo.doNow();
                }
                afterStoryState = new State() {
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
