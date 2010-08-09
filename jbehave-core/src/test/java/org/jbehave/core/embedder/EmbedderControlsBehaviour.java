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
        assertThatNotAllowed(embedderControls, "doBatch", boolean.class);
        assertThatNotAllowed(embedderControls, "doGenerateViewAfterStories", boolean.class);
        assertThatNotAllowed(embedderControls, "doIgnoreFailureInStories", boolean.class);
        assertThatNotAllowed(embedderControls, "doIgnoreFailureInView", boolean.class);
        assertThatNotAllowed(embedderControls, "doSkip", boolean.class);
    }

    private void assertThatNotAllowed(EmbedderControls unmodifiable, String methodName, Class<?>... types)
            throws NoSuchMethodException, IllegalAccessException {
        Method method = unmodifiable.getClass().getMethod(methodName, types);
        try {
            method.invoke(unmodifiable, true);
        } catch (IllegalAccessException e) {
            throw e; // should not occur
        } catch (InvocationTargetException e) {
            // expected
        }
    }
    
}
