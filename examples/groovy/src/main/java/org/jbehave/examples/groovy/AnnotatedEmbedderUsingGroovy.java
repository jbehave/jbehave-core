package org.jbehave.examples.groovy;

import static java.util.Arrays.asList;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

import java.util.List;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.groovy.UsingGroovy;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.groovy.GroovyAnnotatedEmbedderRunner;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

/**
 * Run stories via annotated embedder configuration and steps using Groovy.
 */
@RunWith(GroovyAnnotatedEmbedderRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingGroovy()
public class AnnotatedEmbedderUsingGroovy extends InjectableEmbedder {

    @Override
    @Test
    public void run() {
        injectedEmbedder().runStoriesAsPaths(storyPaths());
    }

    protected List<String> storyPaths() {
        return new StoryFinder()
                .findPaths(codeLocationFromClass(this.getClass()).getFile(), asList("**/*.story"), null);
    }
    
}
