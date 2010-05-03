package org.jbehave.core;

import java.util.List;

import org.jbehave.core.steps.CandidateSteps;

/**
 * <p>
 * Represents a runnable story.
 * </p>
 * <p>
 * Typically users will need to extend an abstract implementation, such as
 * {@link AbstractStory} or a decorator, such as
 * {@link JUnitStory}, which also provide support for test frameworks.
 * </p>
 * 
 * @see AbstractStory
 * @see JUnitStory
 */
public interface RunnableStory {

    void run() throws Throwable;
   
    void useConfiguration(StoryConfiguration configuration);
    
    StoryConfiguration getConfiguration();
    
    void addSteps(CandidateSteps... steps);

    List<CandidateSteps> getSteps();

}
