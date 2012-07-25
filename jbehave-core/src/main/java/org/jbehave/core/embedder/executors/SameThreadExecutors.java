package org.jbehave.core.embedder.executors;

import java.util.concurrent.ExecutorService;

import org.jbehave.core.embedder.EmbedderControls;

import com.google.common.util.concurrent.MoreExecutors;

/**
 *  Creates instances of {@link MoreExecutors#sameThreadExecutor()}.
 */
public class SameThreadExecutors implements ExecutorServiceFactory {

    public ExecutorService create(EmbedderControls controls) {
        return MoreExecutors.sameThreadExecutor();
    }

}
