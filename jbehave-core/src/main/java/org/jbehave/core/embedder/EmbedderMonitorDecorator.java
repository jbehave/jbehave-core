package org.jbehave.core.embedder;

/**
 * Decorator of EmbedderMonitor that delegates to an injected instance and allows classes extending it to override
 * only the methods that are needed.
 *
 * @deprecated Use {@link DelegatingEmbedderMonitor}
 */
@Deprecated
public class EmbedderMonitorDecorator extends DelegatingEmbedderMonitor {

    /**
     * Creates {@link EmbedderMonitorDecorator} with a delegate
     *
     * @param delegate the {@link EmbedderMonitor} to delegate to
     * @deprecated Use {@link DelegatingEmbedderMonitor}
     */
    @Deprecated
    public EmbedderMonitorDecorator(EmbedderMonitor delegate) {
        super(delegate);
    }
}
