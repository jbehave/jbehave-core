package org.jbehave.core.configuration.spring;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

import java.net.URL;
import java.util.List;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.failures.SilentlyAbsorbingFailure;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.LoadFromRelativeFile;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.steps.spring.SpringApplicationContextFactory;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;

public class NamespaceHandlerBehaviour {

	@Test
	public void shouldBuildEmbedderFromNamespaceSchema() {
		ConfigurableApplicationContext context = new SpringApplicationContextFactory(
				"org/jbehave/core/configuration/spring/configuration-namespace.xml")
				.createApplicationContext();
		Object bean = context.getBean("embedder");
		assertThat(bean, notNullValue());
		assertThat(bean, instanceOf(Embedder.class));

		Object loadFromClassparhObject = context
				.getBean(LoadFromClasspath.class.getName());
		assertThat(loadFromClassparhObject, notNullValue());
		assertThat(loadFromClassparhObject, instanceOf(LoadFromClasspath.class));

		Object storyReporterBuilder = context
				.getBean(SpringStoryReporterBuilder.class.getName());
		assertThat(storyReporterBuilder, notNullValue());
		assertThat(storyReporterBuilder,
				instanceOf(SpringStoryReporterBuilder.class));

		SpringStoryReporterBuilder springStoryReporterBuilder = (SpringStoryReporterBuilder) storyReporterBuilder;
		List<Format> formats = springStoryReporterBuilder.getFormats();
		assertThat(formats, hasItems(Format.TXT, Format.CONSOLE, Format.HTML));

		Object regexPrefixCapturingPatternParserObject = context
				.getBean(RegexPrefixCapturingPatternParser.class.getName());
		assertThat(regexPrefixCapturingPatternParserObject, notNullValue());
		assertThat(regexPrefixCapturingPatternParserObject,
				instanceOf(RegexPrefixCapturingPatternParser.class));

		RegexPrefixCapturingPatternParser regexPrefixCapturingPatternParser = (RegexPrefixCapturingPatternParser) regexPrefixCapturingPatternParserObject;
		assertThat("MyPrefix",
				is(regexPrefixCapturingPatternParser.getPrefix()));

	}

	@Test
	public void shouldBuildEmbedderWithFailureStrategyIfSpecified() {
		ConfigurableApplicationContext context = new SpringApplicationContextFactory(
				"org/jbehave/core/configuration/spring/configuration-namespace-failure-strategy.xml")
				.createApplicationContext();
		Object bean = context.getBean("embedder");
		assertThat(bean, notNullValue());
		assertThat(bean, instanceOf(Embedder.class));

		Object failureStrategy = context.getBean(SilentlyAbsorbingFailure.class
				.getName());
		assertThat(failureStrategy, notNullValue());
		assertThat(failureStrategy, instanceOf(SilentlyAbsorbingFailure.class));
	}

	@Test
	public void shouldBuildEmbedderWithLoadFromURLStoryLoaderIfSpecified() {
		ConfigurableApplicationContext context = new SpringApplicationContextFactory(
				"org/jbehave/core/configuration/spring/configuration-namespace-url-loader.xml")
				.createApplicationContext();
		Object bean = context.getBean("embedder");
		assertThat(bean, notNullValue());
		assertThat(bean, instanceOf(Embedder.class));

		Object loadFromURLObject = context.getBean(LoadFromURL.class.getName());
		assertThat(loadFromURLObject, notNullValue());
		assertThat(loadFromURLObject, instanceOf(LoadFromURL.class));
	}

	@Test
	public void shouldBuildEmbedderWithLoadFromRelativePathStoryLoaderIfSpecified() {

		ConfigurableApplicationContext context = new SpringApplicationContextFactory(
				"org/jbehave/core/configuration/spring/configuration-namespace-relative-path-loader.xml")
				.createApplicationContext();
		Object bean = context.getBean("embedder");
		assertThat(bean, notNullValue());
		assertThat(bean, instanceOf(Embedder.class));

		Object loadFromRelativeObject = context
				.getBean(LoadFromRelativeFile.class.getName());
		assertThat(loadFromRelativeObject, notNullValue());
		assertThat(loadFromRelativeObject,
				instanceOf(LoadFromRelativeFile.class));
		
	}

}
