package org.jbehave.examples.core.stories.pending;

import java.util.Arrays;

import org.jbehave.examples.core.CoreStory;

public class MetaFiltering extends CoreStory {

    public MetaFiltering() {
        // Uncomment to set meta filter, which can also be set via Ant or Maven
        configuredEmbedder().useMetaFilters(Arrays.asList("+run yes"));
    }

}