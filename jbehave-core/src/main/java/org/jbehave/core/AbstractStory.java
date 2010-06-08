package org.jbehave.core;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.MostUsefulStoryConfiguration;
import org.jbehave.core.configuration.StoryConfiguration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.MostUsefulStepsConfiguration;

/**
 * <p>
 * Abstract implementation of {@link RunnableStory} which is intended as a base
 * class with no explicit support for any test framework. It provides the
 * {@link StoryEmbedder} used to run the story or stories, using the
 * {@link StoryConfiguration} and the {@link CandidateSteps} specified. By
 * default, {@link MostUsefulStepsConfiguration}) and
 * {@link StoryEmbedder#StoryEmbedder()} are used, but these can overridden via
 * the {@link RunnableStory#useConfiguration(StoryConfiguration)} and
 * {@link RunnableStory#useEmbedder(StoryEmbedder)} methods respectively.
 * </p>
 * <p>
 * Users need to add the {@link CandidateSteps} instances, via the
 * {@link RunnableStory#addSteps(CandidateSteps...)} method.
 * </p>
 * <p>
 * Typically, users that use JUnit will find it easier to extend other
 * implementations, such as {@link JUnitStory} or {@link JUnitStories}, which
 * implement the {@link RunnableStory#run()} via JUnit's annotations.
 * </p>
 */
public abstract class AbstractStory implements RunnableStory {

	private StoryConfiguration configuration = new MostUsefulStoryConfiguration();
	private List<CandidateSteps> candidateSteps = new ArrayList<CandidateSteps>();
	private StoryEmbedder embedder = new StoryEmbedder();

	public void useConfiguration(StoryConfiguration configuration) {
		this.configuration = configuration;
	}

	public void addSteps(CandidateSteps... steps) {
		this.candidateSteps.addAll(asList(steps));
	}

	public void useEmbedder(StoryEmbedder embedder) {
		this.embedder = embedder;
	}

	protected StoryEmbedder configuredEmbedder() {
		embedder.useConfiguration(configuration);
		embedder.useCandidateSteps(candidateSteps);
		return embedder;
	}

}
