package com.learnthinkcode.example.jbehave;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToSimpleName;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML;

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
        EmbedderControls controls = new EmbedderControls();
        controls.doIgnoreFailureInView(true);
        controls.doGenerateViewAfterStories(true);
        return controls;
    }

    @Override
    public Configuration configuration() {
        //
        // build configuration - start with defaults
        //
        Configuration configuration = new MostUsefulConfiguration();
        //
        // let each test class define its story location
        //
        configuration.useStoryPathResolver(new ExplicitStoryPathResolver(storyPath()));
        //
        // use Spring ResourceLoader to load the story text
        //
        configuration.useStoryLoader(new ResourceStoryLoader(context));
        // use simple name path resolution for reports
        configuration.useStoryReporterBuilder(new StoryReporterBuilder()
                .withCodeLocation(CodeLocations.codeLocationFromClass(getClass())).withDefaultFormats()
                .withFormats(CONSOLE, TXT, HTML, XML).withPathResolver(new ResolveToSimpleName()));
        return configuration;
    }

    @Override
    public List<CandidateSteps> candidateSteps() {
        return new SpringStepsFactory(configuration(), context).createCandidateSteps();
    }

    abstract protected String storyPath();
}