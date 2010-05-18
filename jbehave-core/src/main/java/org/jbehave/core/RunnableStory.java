package org.jbehave.core;

import java.util.List;

import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.steps.CandidateSteps;

/**
 * <p>
 * Represents a runnable story facade to the {@link StoryEmbedder}.
 * </p>
 * <p>
 * Users can either extend an abstract implementation, such as
 * {@link AbstractStory} or a concrete implementation, such as
 * {@link JUnitStory} or {@link JUnitStories}, which also provide support for
 * test frameworks.
 * </p>
 * 
 * @see AbstractStory
 * @see JUnitStory
 */
public interface RunnableStory {

	void run() throws Throwable;

	StoryConfiguration getConfiguration();

	List<CandidateSteps> getSteps();

	void addSteps(CandidateSteps... steps);

	void useConfiguration(StoryConfiguration configuration);

	void useEmbedder(StoryEmbedder embedder);

}
