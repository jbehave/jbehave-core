package org.jbehave.core.embedder.executors;

import com.google.common.util.concurrent.MoreExecutors;
import org.jbehave.core.embedder.EmbedderControls;

import java.util.concurrent.ExecutorService;

/**
 *  Creates instances of {@link MoreExecutors#sameThreadExecutor()}.
 */
public class DirectExecutorService implements ExecutorServiceFactory {

    public ExecutorService create(EmbedderControls controls) {
        return MoreExecutors.newDirectExecutorService();
    }

}
