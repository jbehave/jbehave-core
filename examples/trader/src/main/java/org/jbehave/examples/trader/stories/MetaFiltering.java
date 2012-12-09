package org.jbehave.examples.trader.stories;

import java.util.Arrays;

import org.jbehave.examples.trader.TraderStory;

public class MetaFiltering extends TraderStory {

    public MetaFiltering() {
        // Uncomment to set meta filter, which can also be set via Ant or Maven
        configuredEmbedder().useMetaFilters(Arrays.asList("+run yes"));
    }

}