package org.jbehave.core.embedder.executors;

import java.util.concurrent.ExecutorService;

import com.google.common.util.concurrent.MoreExecutors;

import org.jbehave.core.embedder.EmbedderControls;

/**
 *  Creates instances of {@link MoreExecutors#newDirectExecutorService()}.
 */
public class DirectExecutorService implements ExecutorServiceFactory {

    @Override
    public ExecutorService create(EmbedderControls controls) {
        return MoreExecutors.newDirectExecutorService();
    }

}
