package org.jbehave.core.embedder.executors;

import java.util.concurrent.ExecutorService;

import org.jbehave.core.embedder.EmbedderControls;

public interface ExecutorServiceFactory {

    ExecutorService create(EmbedderControls controls);
    
}
