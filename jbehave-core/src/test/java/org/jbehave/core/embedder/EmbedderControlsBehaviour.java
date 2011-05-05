package org.jbehave.core.embedder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class EmbedderControlsBehaviour {
        
    @Test
    public void shouldAllowPropertyBasedControls() throws Throwable {
        EmbedderControls embedderControls = new PropertyBasedEmbedderControls();

        assertThat(embedderControls.toString(), containsString(PropertyBasedEmbedderControls.class.getSimpleName()));
        
        EmbedderControls defaultControls = new EmbedderControls();
        assertThat(embedderControls.batch(), is(defaultControls.batch()));
        assertThat(embedderControls.generateViewAfterStories(), is(defaultControls.generateViewAfterStories()));
        assertThat(embedderControls.ignoreFailureInStories(), is(defaultControls.ignoreFailureInStories()));
        assertThat(embedderControls.ignoreFailureInView(), is(defaultControls.ignoreFailureInView()));
        assertThat(embedderControls.skip(), is(defaultControls.skip()));
        assertThat(embedderControls.storyTimeoutInSecs(), equalTo(defaultControls.storyTimeoutInSecs()));
        assertThat(embedderControls.threads(), equalTo(defaultControls.threads()));

        System.setProperty(PropertyBasedEmbedderControls.BATCH, "true");
        System.setProperty(PropertyBasedEmbedderControls.GENERATE_VIEW_AFTER_STORIES, "true");
        System.setProperty(PropertyBasedEmbedderControls.IGNORE_FAILURE_IN_STORIES, "true");
        System.setProperty(PropertyBasedEmbedderControls.IGNORE_FAILURE_IN_VIEW, "true");
        System.setProperty(PropertyBasedEmbedderControls.SKIP, "true");
        System.setProperty(PropertyBasedEmbedderControls.STORY_TIMEOUT_IN_SECS, "500");
        System.setProperty(PropertyBasedEmbedderControls.THREADS, "5");

        assertThat(embedderControls.batch(), is(true));
        assertThat(embedderControls.generateViewAfterStories(), is(true));
        assertThat(embedderControls.ignoreFailureInStories(), is(true));
        assertThat(embedderControls.ignoreFailureInView(), is(true));
        assertThat(embedderControls.skip(), is(true));
        assertThat(embedderControls.storyTimeoutInSecs(), equalTo(500L));
        assertThat(embedderControls.threads(), equalTo(5));
    }
    
    @Test
    public void shouldNotAllowModificationOfUnmodifiableControls() throws Throwable {
        EmbedderControls delegate = new EmbedderControls();
        EmbedderControls embedderControls = new UnmodifiableEmbedderControls(delegate);
        assertThat(embedderControls.toString(), containsString(UnmodifiableEmbedderControls.class.getSimpleName()));
        assertThat(embedderControls.batch(), equalTo(delegate.batch()));
        assertThat(embedderControls.generateViewAfterStories(), equalTo(delegate.generateViewAfterStories()));
        assertThat(embedderControls.ignoreFailureInStories(), equalTo(delegate.ignoreFailureInStories()));
        assertThat(embedderControls.ignoreFailureInView(), equalTo(delegate.ignoreFailureInView()));
        assertThat(embedderControls.skip(), equalTo(delegate.skip()));
        assertThat(embedderControls.storyTimeoutInSecs(), equalTo(delegate.storyTimeoutInSecs()));
        assertThat(embedderControls.threads(), equalTo(delegate.threads()));
        assertThatNotAllowed(embedderControls, "doBatch", boolean.class, true);
        assertThatNotAllowed(embedderControls, "doGenerateViewAfterStories", boolean.class, true);
        assertThatNotAllowed(embedderControls, "doIgnoreFailureInStories", boolean.class, true);
        assertThatNotAllowed(embedderControls, "doIgnoreFailureInView", boolean.class, true);
        assertThatNotAllowed(embedderControls, "doSkip", boolean.class, true);
        assertThatNotAllowed(embedderControls, "useStoryTimeoutInSecs", long.class, 1);
        assertThatNotAllowed(embedderControls, "useThreads", int.class, 1);
    }

    private void assertThatNotAllowed(EmbedderControls unmodifiable, String methodName, Class<?> type, Object value)
            throws NoSuchMethodException, IllegalAccessException {
        Method method = unmodifiable.getClass().getMethod(methodName, type);
        try {
            method.invoke(unmodifiable, value);
        } catch (IllegalAccessException e) {
            throw e; // should not occur
        } catch (InvocationTargetException e) {
            // expected
        }
    }

}
