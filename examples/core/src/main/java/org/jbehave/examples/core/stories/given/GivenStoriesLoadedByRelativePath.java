package org.jbehave.examples.core.stories.given;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.io.RelativePathCalculator;
import org.jbehave.examples.core.CoreStory;

public class GivenStoriesLoadedByRelativePath extends CoreStory {

    @Override
    public Configuration configuration() {
        return super.configuration().usePathCalculator(new RelativePathCalculator());
    }

}