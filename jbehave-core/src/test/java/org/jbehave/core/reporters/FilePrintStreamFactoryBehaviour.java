package org.jbehave.core.reporters;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.net.URL;

import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryLocation;
import org.jbehave.core.reporters.FilePrintStreamFactory.FileConfiguration;
import org.jbehave.core.reporters.FilePrintStreamFactory.FilePathResolver;
import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToSimpleName;
import org.jbehave.core.reporters.FilePrintStreamFactory.PrintStreamCreationFailed;
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
    
    @Test
    public void shouldAllowOverrideOfDefaultConfiguration(){
        // Given
        URL codeLocation = CodeLocations.codeLocationFromClass(this.getClass());
        String storyPath = "org/jbehave/examples/trader/stories/my_given.story";
        FileConfiguration configuration = new FileConfiguration("ext");
        
        // When
        FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(codeLocation, storyPath), configuration);
        assertThat(factory.configuration(), equalTo(configuration));        
        FileConfiguration newConfiguration = new FileConfiguration();
        factory.useConfiguration(newConfiguration);
        
        // Then
        assertThat(factory.configuration(), not(equalTo(configuration)));        
        assertThat(factory.configuration().toString(), containsString(FileConfiguration.EXTENSION));        
        assertThat(factory.configuration().toString(), containsString(FileConfiguration.RELATIVE_DIRECTORY));        
    }

    @Test
    public void shouldResolveOutputNameFromStoryLocationWithDefaultResolver() {
        assertThatOutputNameIs("org/jbehave/examples/trader/stories/my_given.story",
                "org.jbehave.examples.trader.stories.my_given.ext", null);
        assertThatOutputNameIs("/org/jbehave/examples/trader/stories/my_given.story",
                "org.jbehave.examples.trader.stories.my_given.ext", null);
        assertThatOutputNameIs("my_given.story", "my_given.ext", null);
        assertThatOutputNameIs("my_given", "my_given.ext", null);
    }

    @Test
    public void shouldResolveOutputNameFromStoryLocationWithSimpleNameResolver() {
        FilePathResolver resolver = new ResolveToSimpleName();
        assertThatOutputNameIs("org/jbehave/examples/trader/stories/my_given.story",
                "my_given.ext", resolver);
        assertThatOutputNameIs("/org/jbehave/examples/trader/stories/my_given.story",
                "my_given.ext", resolver);
        assertThatOutputNameIs("my_given.story", "my_given.ext", resolver);
        assertThatOutputNameIs("my_given", "my_given.ext", resolver);
    }

    private void assertThatOutputNameIs(String storyPath, String outputName, FilePathResolver pathResolver) {
        // Given
        URL codeLocation = CodeLocations.codeLocationFromClass(this.getClass());
        String extension = "ext";        
        FileConfiguration configuration = (pathResolver != null ? new FileConfiguration("", extension, pathResolver) : new FileConfiguration(extension));
        // When
        FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(codeLocation, storyPath), configuration);
        // Then
        assertThat(factory.outputName(), equalTo(outputName));
    }
    
    @Test(expected=PrintStreamCreationFailed.class)
    public void shouldFailIfPrintStreamCannotBeCreated(){
        // Given
        URL codeLocation = CodeLocations.codeLocationFromClass(this.getClass());
        String storyPath = "org/jbehave/examples/trader/stories/my_given.story";
        FileConfiguration configuration = new FileConfiguration("ext");
        FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(codeLocation, storyPath), configuration){
            protected File outputDirectory() {
                return new File((String)null);
            }
        };
        // When
        factory.createPrintStream();
        
        // Then fail as expected
    }

    private void ensureOutputFileIsSame(URL codeLocation, String storyPath) {
        FileConfiguration configuration = new FileConfiguration("ext");
        FilePrintStreamFactory factory = new FilePrintStreamFactory(new StoryLocation(codeLocation, storyPath), configuration);
        factory.createPrintStream();
        File outputFile = factory.getOutputFile();
        String expected = new File(codeLocation.getFile()).getParent().replace('\\', '/') + "/" + configuration.getRelativeDirectory() + "/"
                + "org.jbehave.examples.trader.stories.my_given." + configuration.getExtension();
        assertThat(outputFile.toString().replace('\\', '/'), equalTo(expected));

    }

}
