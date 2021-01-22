package org.jbehave.examples.core;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.context.Context;
import org.jbehave.core.context.ContextView;
import org.jbehave.core.context.JFrameContextView;
import org.jbehave.core.steps.ContextStepMonitor;
import org.jbehave.core.steps.NullStepMonitor;

/**
 * <p>
 * Core stories with JFrame-based context step monitor
 * </p>
 */
public class JFrameCoreStories extends CoreStories {

    private Context context = new Context();
    private ContextView contextView = new JFrameContextView().sized(640, 120);
    private ContextStepMonitor contextStepMonitor = new ContextStepMonitor(context, contextView, new NullStepMonitor());

    @Override
    public Configuration configuration() {
        Configuration configuration = super.configuration();
        configuration.useStepMonitor(contextStepMonitor);
        return configuration;
    }

}
