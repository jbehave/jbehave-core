package org.jbehave.core.embedder.executors;

import java.util.concurrent.ExecutorService;

import org.jbehave.core.embedder.EmbedderControls;

import com.google.common.util.concurrent.MoreExecutors;

/**
 *  Creates instances of {@link MoreExecutors#sameThreadExecutor()}.
 *  @deprecated Use {@link DirectExecutorService}
 */
@Deprecated
public class SameThreadExecutors implements ExecutorServiceFactory {

    @Override
    public ExecutorService create(EmbedderControls controls) {
        return MoreExecutors.sameThreadExecutor();
    }

}
