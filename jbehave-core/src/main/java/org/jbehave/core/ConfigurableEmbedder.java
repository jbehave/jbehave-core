package org.jbehave.core;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.steps.CandidateSteps;

/**
 * <p>
 * Abstract implementation of {@link Embeddable} which allow to configure
 * the {@link Embedder} used to run the story or stories, using the
 * {@link Configuration} and the {@link CandidateSteps} specified.
 * </p>
 * <p>
 * By default {@link MostUsefulConfiguration}) is used and be overridden
 * via the {@link #useConfiguration(Configuration)} method.
 * </p>
 * <p>
 * Users need to add the {@link CandidateSteps} instances, via the
 * {@link #addSteps(CandidateSteps...)} or {@link
 * #addSteps(List<CandidateSteps>)} methods.
 * </p>
 * <p>
 * Typically, users that use JUnit will find it easier to extend other
 * implementations, such as {@link JUnitStory} or {@link JUnitStories}, which
 * implement the {@link#run()} using the configured embedder and annotate it
 * with JUnit's {@link Test} annotation.
 * </p>
 */
public abstract class ConfigurableEmbedder implements Embeddable {

    private Embedder embedder = new Embedder();
    private Configuration configuration = new MostUsefulConfiguration();
    private List<CandidateSteps> candidateSteps = new ArrayList<CandidateSteps>();

    public void useEmbedder(Embedder embedder) {
        this.embedder = embedder;
    }

    public void useConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void addSteps(CandidateSteps... steps) {
        addSteps(asList(steps));
    }

    public void addSteps(List<CandidateSteps> steps) {
        this.candidateSteps.addAll(steps);
    }

    public Configuration configuration() {
        return configuration;
    }

    public List<CandidateSteps> candidateSteps() {
        return candidateSteps;
    }

    public Embedder configuredEmbedder() {
        embedder.useConfiguration(configuration());
        embedder.useCandidateSteps(candidateSteps());
        return embedder;
    }

}
