package org.jbehave.examples.core.needle;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingPaths;
import org.jbehave.core.annotations.needle.UsingNeedle;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.needle.NeedleAnnotatedPathRunner;
import org.junit.runner.RunWith;

/**
 * Run stories via annotated embedder configuration and steps using Needle. The
 * textual stories are exactly the same ones found in the
 * jbehave-core-example. Here we are only concerned with using the container
 * to compose the configuration and the steps instances.
 */
@RunWith(NeedleAnnotatedPathRunner.class)
@Configure
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true,
        ignoreFailureInView = true)
@UsingNeedle
@UsingPaths(searchIn = "../trader/src/main/java", includes = { "**/*.story" },
        excludes = { "**/examples_table*.story" })
public class AnnotatedPathRunnerUsingNeedle {

}
