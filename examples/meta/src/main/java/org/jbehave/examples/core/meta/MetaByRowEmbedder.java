package org.jbehave.examples.core.meta;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.spring.UsingSpring;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.spring.SpringAnnotatedEmbedderRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringAnnotatedEmbedderRunner.class)
@UsingEmbedder(embedder = Embedder.class, ignoreFailureInStories = false, ignoreFailureInView = false)
@UsingSpring(resources = { "org.jbehave.examples.core.meta.StepsScanner" })
public final class MetaByRowEmbedder extends InjectableEmbedder {

	@Override
    @Test
	public void run() {
		injectedEmbedder().metaFilters().add("+smoke");
		injectedEmbedder().runStoriesAsPaths(new StoryFinder().findPaths(
				codeLocationFromClass(this.getClass()), "**/*.story", ""));
	}

}
