package org.jbehave.core.junit;

import java.util.List;

import org.jbehave.core.annotations.UsingPaths;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderMonitor;
import org.jbehave.core.embedder.EmbedderMonitorDecorator;
import org.jbehave.core.io.StoryNameResolver;
import org.jbehave.core.io.UnderscoredToCapitalized;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

/**
 * A JUnit {@link Runner} that uses a {@link UsingPaths} annotation to specify
 * which story paths to run and uses the {@link RunNotifier} to provide a
 * {@link Description} for each. The story description uses a
 * {@link StoryNameResolver} (overridable via the {@link #storyNameResolver()}
 * method) to resolve the story path to a name.
 */
public class AnnotatedPathRunner extends AnnotatedEmbedderRunner {

    private final AnnotationBuilder annotationBuilder;
    private final StoryNameResolver nameResolver;
    private final List<String> paths;

    /**
     * Class constructor.
     * 
     * @param annotatedClass the annotated {@link Class}.
     * @throws InitializationError if an error occurs.
     */
    public AnnotatedPathRunner(Class<?> annotatedClass) throws InitializationError {
        super(annotatedClass);
        this.annotationBuilder = annotationBuilder();
        this.nameResolver = storyNameResolver();
        this.paths = annotationBuilder.findPaths();
    }

    protected StoryNameResolver storyNameResolver() {
        return new UnderscoredToCapitalized();
    }

    @Override
    public Description getDescription() {
        Description description = Description.createSuiteDescription(testClass());
        for (String path : paths) {
            description.addChild(createDescriptionForPath(path));
        }

        return description;
    }

    private Description createDescriptionForPath(String path) {
        String name = nameResolver.resolveName(path);
        return Description.createTestDescription(testClass(), name);
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        // overridden to avoid JUnit-specific errors
    }

    @Override
    protected void validateInstanceMethods(List<Throwable> errors) {
        // overridden to avoid JUnit-specific errors
    }

    @Override
    public void run(RunNotifier notifier) {
        Embedder embedder = annotationBuilder.buildEmbedder();
        NotifierEmbedderMonitor notifierEmbedderMonitor = new NotifierEmbedderMonitor(embedder.embedderMonitor(),
                notifier);
        embedder.useEmbedderMonitor(notifierEmbedderMonitor);

        try {
            embedder.runStoriesAsPaths(paths);
        } finally {
            notifierEmbedderMonitor.storyFinished();
        }
    }

    /**
     * {@link EmbedderMonitor} that reports story updates to a
     * {@link RunNotifier}.
     */
    private final class NotifierEmbedderMonitor extends EmbedderMonitorDecorator {
        private final RunNotifier notifier;
        private Description currentStory;

        /**
         * Creates a NotifierEmbedderMonitor
         * 
         * @param delegate the EmbedderMonitor delegate
         * @param notifier the RunNotifier
         */
        private NotifierEmbedderMonitor(EmbedderMonitor delegate, RunNotifier notifier) {
            super(delegate);
            this.notifier = notifier;
        }

        @Override
        public void runningStory(String path) {
            super.runningStory(path);
            storyFinished();
            currentStory = createDescriptionForPath(path);
            notifier.fireTestStarted(currentStory);
        }

        @Override
        public void storyFailed(String path, Throwable cause) {
            super.storyFailed(path, cause);
            notifier.fireTestFailure(new Failure(currentStory, cause));
            notifier.fireTestFinished(currentStory);
            currentStory = null;
        }

        /**
         * Finishes the last story.
         */
        private void storyFinished() {
            if (currentStory == null) {
                return;
            }
            notifier.fireTestFinished(currentStory);
        }
    }
}
