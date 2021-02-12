package org.jbehave.core.reporters;

import org.jbehave.core.context.Context;

public class ContextOutput extends Format {
    private final Context context;

    public ContextOutput(Context context) {
        super("CONTEXT");
        this.context = context;
    }

    @Override
    public StoryReporter createStoryReporter(
            FilePrintStreamFactory filePrintStreamFactory,
            StoryReporterBuilder storyReporterBuilder) {
        return new ContextStoryReporter(context);
    }
}
