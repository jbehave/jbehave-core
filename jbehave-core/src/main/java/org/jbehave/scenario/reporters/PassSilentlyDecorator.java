package org.jbehave.scenario.reporters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbehave.scenario.definition.Blurb;
import org.jbehave.scenario.definition.ExamplesTable;
import org.jbehave.scenario.definition.StoryDefinition;

/**
 * Swallows the reports from all scenarios that pass, providing output only for
 * failing or pending scenarios.
 */
public class PassSilentlyDecorator implements ScenarioReporter {

    private final ScenarioReporter delegate;
    private List<Todo> currentScenario = new ArrayList<Todo>();
    private State scenarioState = State.SILENT;
    private State beforeStoryState = State.SILENT;
    private State afterStoryState = State.SILENT;
    private boolean embeddedStory;

    public PassSilentlyDecorator(ScenarioReporter delegate) {
        this.delegate = delegate;
    }

    public void afterStory(boolean embeddedStory) {
        afterStoryState.report();
    }

    public void afterStory() {
        afterStoryState.report();
    }

    public void beforeStory(final StoryDefinition story, final boolean embeddedStory) {
        this.embeddedStory = embeddedStory;
        beforeStoryState = new State() {
            public void report() {
                delegate.beforeStory(story, embeddedStory);
                beforeStoryState = State.SILENT;
            }
        };
    }
    
    public void beforeStory(final Blurb blurb) {
        beforeStory(new StoryDefinition(blurb), false);
    };
    
    public void ignorable(final String step) {
        currentScenario.add(new Todo() {
            public void doNow() {
                delegate.ignorable(step);
            }
        });
    }

    public void failed(final String step, final Throwable e) {
        currentScenario.add(new Todo() {
            public void doNow() {
                delegate.failed(step, e);
            }
        });
        setStateToNoisy();
    }

    public void notPerformed(final String step) {
        currentScenario.add(new Todo() {
            public void doNow() {
                delegate.notPerformed(step);
            }
        });
    }

    public void pending(final String step) {
        currentScenario.add(new Todo() {
            public void doNow() {
                delegate.pending(step);
            }
        });
        setStateToNoisy();
    }

    private void setStateToNoisy() {
        scenarioState = new State() {
            public void report() {
                beforeStoryState.report();
                for (Todo todo : currentScenario) {
                    todo.doNow();
                }
                afterStoryState = new State() {
                    public void report() {
                        delegate.afterStory(embeddedStory);
                        afterStoryState = State.SILENT;
                    }
                };
                scenarioState = State.SILENT;
            }
        };
    }

    public void successful(final String step) {
        currentScenario.add(new Todo() {
            public void doNow() {
                delegate.successful(step);
            }
        });
    }

    public void afterScenario() {
        currentScenario.add(new Todo() {
            public void doNow() {
                delegate.afterScenario();
            }
        });
        scenarioState.report();
    }

    public void beforeScenario(final String title) {
        currentScenario = new ArrayList<Todo>();
        currentScenario.add(new Todo() {
            public void doNow() {
                delegate.beforeScenario(title);
            }
        });
    }

	public void givenScenarios(final List<String> givenScenarios) {
        currentScenario.add(new Todo() {
            public void doNow() {
                delegate.givenScenarios(givenScenarios);
            }
        });
	}
	
	public void beforeExamples(final List<String> steps, final ExamplesTable table) {
        currentScenario.add(new Todo() {
            public void doNow() {
                delegate.beforeExamples(steps, table);
            }
        });		
	}

	public void example(final Map<String, String> tableRow) {
        currentScenario.add(new Todo() {
            public void doNow() {
                delegate.example(tableRow);
            }
        });		
	}
	
    public void afterExamples() {
        currentScenario.add(new Todo() {
            public void doNow() {
                delegate.afterExamples();
            }
        });     
    }

    public void examplesTable(ExamplesTable table) {
        beforeExamples(new ArrayList<String>(), table);        
    }

    public void examplesTableRow(Map<String, String> tableRow) {
        example(tableRow);        
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

}
