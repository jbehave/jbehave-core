package org.jbehave.core.reporters;

import org.jbehave.core.io.StoryLocation;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


public class FilePrintStreamFactoryBehaviour {

    @Test
    public void shouldHandleStoryPathInClasspath() {
        String codeLocation = codeLocation();
        String storyPath = "org/jbehave/examples/trader/stories/my_given.story";
        ensureOutputFileIsSame(codeLocation, storyPath);
    }

    @Test
    public void shouldHandleStoryPathAsURL() {
        String codeLocation = codeLocation();
        String storyPath = "file:" + codeLocation + "org/jbehave/examples/trader/stories/my_given.story";
        ensureOutputFileIsSame(codeLocation, storyPath);
    }

    @Test
    public void shouldHandleStoryPathAsURLWithSpecifiedCodeSourceClass() {
        String codeLocation = new StoryLocation("", FilePrintStreamFactory.class).getCodeLocation().getFile();
        String storyPath = "file:" + codeLocation + "org/jbehave/examples/trader/stories/my_given.story";
        ensureOutputFileIsSame(codeLocation, storyPath);
    }

    private void ensureOutputFileIsSame(String codeLocation, String storyPath) {
        FilePrintStreamFactory.FileConfiguration configuration = new FilePrintStreamFactory.FileConfiguration("ext");
        StoryLocation storyLocation = new StoryLocation(storyPath, this.getClass());
		FilePrintStreamFactory factory = new FilePrintStreamFactory(storyLocation, configuration);
        File outputFile = factory.getOutputFile();
        String expected = new File(codeLocation).getParent() + "/" + configuration.getOutputDirectory() + "/"
                + "org.jbehave.examples.trader.stories.my_given." + configuration.getExtension();
        assertThat(outputFile.toString(), equalTo(expected));

    }

	private String codeLocation() {
		return new StoryLocation("", this.getClass()).getCodeLocation()
				.getFile();
	}

}
