package org.jbehave.examples.core;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.context.Context;
import org.jbehave.core.context.ContextView;
import org.jbehave.core.context.JFrameContextView;
import org.jbehave.core.reporters.ContextOutput;
import org.jbehave.core.reporters.CrossReference;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.steps.ContextStepMonitor;

/**
 * <p>
 * Core stories with JFrame-based context step monitor
 * </p>
 */
public class JFrameCoreStories extends CoreStories {

    private final CrossReference xref = new CrossReference();
    private Context context = new Context();
    private Format contextFormat = new ContextOutput(context);
    private ContextView contextView = new JFrameContextView().sized(640, 120);
    private ContextStepMonitor contextStepMonitor = new ContextStepMonitor(context, contextView, xref.getStepMonitor());

    @Override
    public Configuration configuration() {
        Configuration configuration = super.configuration();
        configuration.useStepMonitor(contextStepMonitor);
        return configuration;
    }

}
