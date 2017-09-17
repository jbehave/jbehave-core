package org.jbehave.examples.core.stories.pending;

import org.jbehave.examples.core.CoreStory;

import java.util.Arrays;

public class MetaFiltering extends CoreStory {

    public MetaFiltering() {
        // Uncomment to set meta filter, which can also be set via Ant or Maven
        configuredEmbedder().useMetaFilters(Arrays.asList("+run yes"));
    }

}