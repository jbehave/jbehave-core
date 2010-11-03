package com.learnthinkcode.example.jbehave;

import static org.jbehave.core.reporters.StoryReporterBuilder.Format.CONSOLE;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.HTML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.TXT;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.XML;
import static org.jbehave.core.reporters.StoryReporterBuilder.Format.STATS;

import java.util.List;
import java.util.Properties;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext.xml" })
abstract public class AbstractSpringJBehaveIntegrationTest extends JUnitStory {

	@Autowired
	protected ApplicationContext context;

	public AbstractSpringJBehaveIntegrationTest() {
		Embedder embedder = new Embedder();
		embedder.useEmbedderControls(embedderControls());
		useEmbedder(embedder);
	}

	protected EmbedderControls embedderControls() {
		EmbedderControls result = new EmbedderControls();
		result.doGenerateViewAfterStories(true);
		return result;
	}

	@Override
	public Configuration configuration() {
		//
		// build configuration - start with defaults
		//
		Configuration result = new MostUsefulConfiguration();
		//
		// let each test class define its story location
		//
		result.useStoryPathResolver(new ExplicitStoryPathResolver(storyPath()));
		//
		// use Spring ResourceLoader to load the story text
		//
		result.useStoryLoader(new ResourceStoryLoader(context));
		//
		// report in all formats but based on the class name, not the story location
		//
		Properties viewProperties = new Properties();
        viewProperties.setProperty("reports", "view/ftl/jbehave-reports.ftl");
        viewProperties.setProperty("decorated", "view/ftl/jbehave-report-decorated.ftl");
        viewProperties.setProperty("nonDecorated", "view/ftl/jbehave-report-non-decorated.ftl");
		StoryReporterBuilder storyReporterBuilder = new StoryReporterBuilder();
		storyReporterBuilder.withCodeLocation(CodeLocations.codeLocationFromClass(getClass()));
		storyReporterBuilder.withFormats(STATS, CONSOLE, TXT, HTML, XML);
		storyReporterBuilder.withViewResources(viewProperties);
		storyReporterBuilder.withPathResolver(new ClassnameFilePathResolver(getClass().getName()));
		result.useStoryReporterBuilder(storyReporterBuilder);
		return result;
	}

	@Override
	public List<CandidateSteps> candidateSteps() {
		return new SpringStepsFactory(configuration(), context).createCandidateSteps();
	}

	abstract protected String storyPath();	
}