package org.jbehave.core.embedder.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jbehave.core.embedder.EmbedderControls;

/**
 *  Creates instances of {@link Executors.newFixedThreadPool(int)}.
 */
public class FixedThreadExecutors implements ExecutorServiceFactory {

    public ExecutorService create(EmbedderControls controls) {
        return Executors.newFixedThreadPool(controls.threads());
    }

}
