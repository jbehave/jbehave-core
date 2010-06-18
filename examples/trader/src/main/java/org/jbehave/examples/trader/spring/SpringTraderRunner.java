package org.jbehave.examples.trader.spring;

import static java.util.Arrays.asList;

import java.util.List;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryPathFinder;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.trader.BeforeAfterSteps;
import org.jbehave.examples.trader.ClasspathTraderEmbedder;
import org.jbehave.examples.trader.TraderSteps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Run stories via Spring JUnit 4 runner
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/org/jbehave/examples/trader/spring/steps.xml" })
public class SpringTraderRunner {

	@Autowired
	private TraderSteps traderSteps;

	@Autowired
	private BeforeAfterSteps beforeAndAfterSteps;

	@Test
	public void runStoriesAsPaths() {
		embedder().runStoriesAsPaths(storyPaths());
	}

	@Test
	public void findMatchingCandidateSteps() {
		embedder().reportMatchingStepdocs("When traders are subset to \".*y\" by name");
		embedder().reportMatchingStepdocs("Given a step that is not matched");
	}

	@Test
	public void findMatchingCandidateStepsWithNoStepsInstancesProvided() {
		new Embedder().reportMatchingStepdocs("Given a step that cannot be matched");
	}

	private Embedder embedder() {
		Embedder embedder = new ClasspathTraderEmbedder();
		embedder.useCandidateSteps(new InstanceStepsFactory(embedder
				.configuration(), traderSteps, beforeAndAfterSteps)
				.createCandidateSteps());
		return embedder;
	}

	protected List<String> storyPaths() {
		StoryPathFinder finder = new StoryPathFinder();
		String basedir = CodeLocations.codeLocationFromClass(this.getClass()).getFile();
		return finder.listStoryPaths(basedir, "", asList("**/*.story"),
				asList(""));
	}

}