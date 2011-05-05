package org.jbehave.examples.trader.stories;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.io.RelativePathCalculator;
import org.jbehave.examples.trader.TraderStory;

public class GivenRelativePathStory extends TraderStory {

    @Override
    public Configuration configuration() {
        return super.configuration().usePathCalculator(new RelativePathCalculator());
    }

}