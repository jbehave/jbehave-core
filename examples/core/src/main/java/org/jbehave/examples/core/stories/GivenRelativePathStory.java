package org.jbehave.examples.core.stories;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.io.RelativePathCalculator;
import org.jbehave.examples.core.CoreStory;

public class GivenRelativePathStory extends CoreStory {

    @Override
    public Configuration configuration() {
        return super.configuration().usePathCalculator(new RelativePathCalculator());
    }

}