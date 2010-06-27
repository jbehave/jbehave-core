package org.jbehave.core;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.steps.CandidateSteps;

/**
 * <p>
 * Represents a runnable story facade to the {@link Embedder}. At a minimum, a
 * runnable story requires {@link CandidateSteps} instances to be added via the
 * {@link RunnableStory#addSteps(CandidateSteps...)}, specifying the mapping of
 * textual steps to Java methods. Custom {@link Configuration} and
 * {@link Embedder} can be specified to override any default via the
 * {@link RunnableStory#useConfiguration(Configuration)} and
 * {@link RunnableStory#useEmbedder(Embedder)} methods.
 * </p>
 * <p>
 * Users can either extend the abstract implementation {@link AbstractStory},
 * which does not implement the {@link RunnableStory#run()} method, or other
 * implementations, such as {@link JUnitStory} or {@link JUnitStories}, which
 * implement {@link RunnableStory#run()} using JUnit's annotations.
 * </p>
 * <p>
 * Other test frameworks can be supported in much the same way, by extending the
 * abstract implementation and implementing {@link RunnableStory#run()}.
 * </p>
 * 
 * @see AbstractStory
 * @see JUnitStory
 * @see JUnitStories
 */
public interface RunnableStory extends Embeddable {

	/**
	 * Runs the story
	 * 
	 * @throws Throwable
	 */
	void run() throws Throwable;

	/**
	 * Adds CandidateSteps instances used by the Embedder
	 * 
	 * @param steps the CandidateSteps instances used to match textual steps
	 */
	void addSteps(CandidateSteps... steps);

	/**
	 * Adds CandidateSteps instances used by the Embedder
	 * 
	 * @param steps the CandidateSteps instances used to match textual steps
	 */
	void addSteps(List<CandidateSteps> steps);

	/**
	 * Specifies the story configuration overriding any default
	 * 
	 * @param configuration the Configuration
	 */
	void useConfiguration(Configuration configuration);

}
