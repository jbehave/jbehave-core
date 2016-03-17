package org.jbehave.examples.example.meta;

import java.util.List;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.spring.UsingSpring;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.spring.SpringAnnotatedEmbedderRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringAnnotatedEmbedderRunner.class)
@UsingEmbedder(embedder = Embedder.class, ignoreFailureInStories = false, ignoreFailureInView = false)
@UsingSpring(resources = { "org.jbehave.examples.example.meta.TestConfig" })
public final class ExampleMetaBehaviour extends InjectableEmbedder {

    @Test
    public void run() {
        final List<String> stories = new StoryFinder().findPaths(CodeLocations.codeLocationFromClass(this.getClass()).getFile(),
                "stories/*.story", " ");

        final List<String> metaFilters = injectedEmbedder().metaFilters();
        metaFilters.add("+smoke");

        injectedEmbedder().runStoriesAsPaths(stories);
    }

}
