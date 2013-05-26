package org.jbehave.examples.core.weld;

import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingPaths;
import org.jbehave.core.annotations.weld.UsingWeld;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.weld.WeldAnnotatedPathRunner;
import org.junit.runner.RunWith;

/**
 * Run stories via annotated embedder configuration and steps using Guice. The
 * textual trader stories are exactly the same ones found in the
 * jbehave-trader-example. Here we are only concerned with using the container
 * to compose the configuration and the steps instances.
 */
@RunWith(WeldAnnotatedPathRunner.class)
@Configure()
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
@UsingWeld
@UsingPaths(searchIn = "../core/src/main/java", includes = { "**/*.story" }, excludes = { "" })
public class AnnotatedPathRunnerUsingWeld {


}
