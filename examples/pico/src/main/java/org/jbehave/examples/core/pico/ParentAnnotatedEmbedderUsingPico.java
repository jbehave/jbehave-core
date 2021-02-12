package org.jbehave.examples.core.pico;

import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.pico.UsingPico;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.pico.PicoAnnotatedEmbedderRunner;
import org.jbehave.examples.core.pico.AnnotatedEmbedderUsingPico.ConfigurationModule;
import org.junit.runner.RunWith;

import java.util.List;

import static org.jbehave.core.io.CodeLocations.codeLocationFromPath;

@RunWith(PicoAnnotatedEmbedderRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingPico(modules = { ConfigurationModule.class })
public abstract class ParentAnnotatedEmbedderUsingPico extends InjectableEmbedder {

    @Override
    @org.junit.Test
    public void run() {
        injectedEmbedder().runStoriesAsPaths(storyPaths());
    }

    public List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromPath("../core/src/main/java"), "**/*.story", "");
    }

}
