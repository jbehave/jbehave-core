package org.jbehave.core.embedder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class EmbedderControlsBehaviour {

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
        assertThat(embedderControls.threads(), equalTo(delegate.threads()));
        assertThatNotAllowed(embedderControls, "doBatch", boolean.class, true);
        assertThatNotAllowed(embedderControls, "doGenerateViewAfterStories", boolean.class, true);
        assertThatNotAllowed(embedderControls, "doIgnoreFailureInStories", boolean.class, true);
        assertThatNotAllowed(embedderControls, "doIgnoreFailureInView", boolean.class, true);
        assertThatNotAllowed(embedderControls, "doSkip", boolean.class, true);
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
