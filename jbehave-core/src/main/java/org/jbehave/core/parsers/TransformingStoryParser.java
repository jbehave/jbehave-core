package org.jbehave.core.parsers;

import org.jbehave.core.model.Story;

public class TransformingStoryParser implements StoryParser {

    private final StoryParser delegate;
    private StoryTransformer[] transformers; 

    public TransformingStoryParser(StoryParser delegate, StoryTransformer... transformers) {
        this.delegate = delegate;
        this.transformers = transformers;
    }

    @Override
    public Story parseStory(String storyAsText) {
        return delegate.parseStory(transform(storyAsText));
    }

    @Override
    public Story parseStory(String storyAsText, String storyPath) {
        return delegate.parseStory(transform(storyAsText), storyPath);
    }

    private String transform(String storyAsText) {
        String transformed = storyAsText;
        for (StoryTransformer transformer : transformers) {
            transformed = transformer.transform(transformed);
        }
        return transformed;
    }

}
