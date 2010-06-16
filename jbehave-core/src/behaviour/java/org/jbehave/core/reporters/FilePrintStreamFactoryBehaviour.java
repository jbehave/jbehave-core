package org.jbehave.core.reporters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.net.URL;

import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryLocation;
import org.jbehave.core.reporters.FilePrintStreamFactory.FileConfiguration;
import org.junit.Test;


public class FilePrintStreamFactoryBehaviour {

    @Test
    public void shouldHandleStoryPathInClasspath() {
        URL codeLocation = CodeLocations.codeLocationFromClass(this.getClass());
        String storyPath = "org/jbehave/examples/trader/stories/my_given.story";
        ensureOutputFileIsSame(codeLocation, storyPath);
    }

    @Test
    public void shouldHandleStoryPathAsURL() {
        URL codeLocation = CodeLocations.codeLocationFromClass(this.getClass());
        String storyPath = codeLocation + "org/jbehave/examples/trader/stories/my_given.story";
        ensureOutputFileIsSame(codeLocation, storyPath);
    }

    @Test
    public void shouldHandleStoryPathAsURLWithSpecifiedCodeSourceClass() {
        URL codeLocation = CodeLocations.codeLocationFromClass(FilePrintStreamFactory.class);
        String storyPath = codeLocation + "org/jbehave/examples/trader/stories/my_given.story";
        ensureOutputFileIsSame(codeLocation, storyPath);
    }

    private void ensureOutputFileIsSame(URL codeLocation, String storyPath) {
        FileConfiguration configuration = new FileConfiguration("ext");
        FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(codeLocation, storyPath), configuration);
        File outputFile = factory.getOutputFile();
        String expected = new File(codeLocation.getFile()).getParent() + "/" + configuration.getOutputDirectory() + "/"
                + "org.jbehave.examples.trader.stories.my_given." + configuration.getExtension();
        assertThat(outputFile.toString(), equalTo(expected));

    }

}
