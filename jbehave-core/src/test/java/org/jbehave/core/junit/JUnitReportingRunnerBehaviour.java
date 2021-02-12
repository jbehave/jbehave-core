package org.jbehave.core.junit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JUnitReportingRunnerBehaviour {

    @Test
    void shouldPrepareEmbedder() {
        EmbedderControls ec = mock(EmbedderControls.class);
        when(ec.doIgnoreFailureInStories(Mockito.anyBoolean())).thenReturn(ec);
        when(ec.doIgnoreFailureInView(Mockito.anyBoolean())).thenReturn(ec);
        Embedder e = mock(Embedder.class);
        when(e.embedderControls()).thenReturn(ec);
        EmbedderControls recommendedControls = JUnitReportingRunner.recommendedControls(e);
        assertThat(recommendedControls, is(ec));
        verify(ec).doIgnoreFailureInView(true);
        verify(ec).doIgnoreFailureInStories(true);
    }
}
