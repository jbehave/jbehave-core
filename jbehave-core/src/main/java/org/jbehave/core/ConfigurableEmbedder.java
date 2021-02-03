package org.jbehave.core;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.steps.InjectableStepsFactory;

/**
 * <p>
 * Abstract implementation of {@link Embeddable} which allows to configure the
 * {@link Embedder} used to run the stories, using the {@link Configuration} and
 * the {@link InjectableStepsFactory} specified.
 * </p>
 * <p>
 * The instances of the {@link Configuration} and the
 * {@link InjectableStepsFactory} can be provided either via the
 * {@link #useConfiguration(Configuration)} and
 * {@link #useStepsFactory(InjectableStepsFactory)} methods or overriding the
 * {@link #configuration()} and {@link #stepsFactory()} methods.
 * </p>
 * <p>
 * If overriding the {@link #configuration()} method and providing an
 * {@link InjectableStepsFactory} which requires a {@link Configuration}, then
 * care must be taken to avoid re-instantiating the {@link Configuration}. E.g.:
 * 
 * <pre>
 * {@code
 * public Configuration configuration() {
 *   if ( super.hasConfiguration() ){
 *     return super.configuration();
 *   }
 *   return new MostUsefulConfiguration()...;
 * }
 * </pre>
 * 
 * </p>
 * <p>
 * Note that no defaults are provided by this implementation. If no values are
 * set by the subclasses, then <code>null</code> values are passed to the
 * configured {@link Embedder}, which is responsible for setting the default
 * values.
 * </p>
 * <p>
 * Typically, users that use JUnit will find it easier to extend other
 * implementations, such as {@link JUnitStories}, which
 * implement the {@link#run()} using the configured {@link Embedder} and
 * annotate it with JUnit's annotations.
 * </p>
 */
public abstract class ConfigurableEmbedder implements Embeddable {

	private Embedder embedder = new Embedder();
	private Configuration configuration;
	private InjectableStepsFactory stepsFactory;

	@Override
    public void useEmbedder(Embedder embedder) {
		this.embedder = embedder;
	}

	public void useConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public Configuration configuration() {
		return configuration;
	}

	public boolean hasConfiguration() {
		return configuration != null;
	}

	public void useStepsFactory(InjectableStepsFactory stepsFactory) {
		this.stepsFactory = stepsFactory;
	}

	public InjectableStepsFactory stepsFactory() {
		return stepsFactory;
	}

	public boolean hasStepsFactory() {
		return stepsFactory != null;
	}

	public Embedder configuredEmbedder() {
		if (configuration == null) {
			configuration = configuration();
		}
		embedder.useConfiguration(configuration);
		if (stepsFactory == null) {
			stepsFactory = stepsFactory();
		}
		embedder.useStepsFactory(stepsFactory);
		return embedder;
	}

}
