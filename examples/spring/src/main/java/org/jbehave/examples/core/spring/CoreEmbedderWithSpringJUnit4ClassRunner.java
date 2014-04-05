package org.jbehave.examples.core.spring;

import java.util.List;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.jbehave.examples.core.CoreEmbedder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

/**
 * Run stories using Spring's {@link SpringJUnit4ClassRunner} to inject the
 * steps ApplicationContext then used to configure the candidate steps used by
 * the CoreEmbedder.  This examples shows how the Embedder can be used within
 * any testing framework.  Similary, we could extend a JUnit 3 test class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/org/jbehave/examples/core/spring/steps.xml" })
public class CoreEmbedderWithSpringJUnit4ClassRunner {

    @Autowired
    protected ApplicationContext context;

    private Embedder embedder;

    @Before
    public void setup() {
        embedder = new CoreEmbedder();
        embedder.useCandidateSteps(new SpringStepsFactory(embedder.configuration(), context).createCandidateSteps());
    }

    @Test
    public void runStoriesAsPaths() {
        List<String> storyPaths = new StoryFinder().findPaths(codeLocationFromPath("../core/src/main/java"),
                "**/*.story", "");
        embedder.runStoriesAsPaths(storyPaths);
    }

    @Test
    public void findMatchingCandidateSteps() {
        embedder.reportMatchingStepdocs("When traders are subset to \".*y\" by name");
        embedder.reportMatchingStepdocs("Given a step that is not matched");
    }

    @Test
    public void findMatchingCandidateStepsWithNoStepsInstancesProvided() {
        embedder.reportMatchingStepdocs("Given a step that cannot be matched");
    }

}