package org.jbehave.core.reporters;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * A view generator is responsible for creating a collective view of file-based
 * outputs of the story reporters. The generator assumes all story outputs have
 * been written to the output directory in the formats specified during the
 * running of the stories.
 */
public interface ViewGenerator {

	void generateView(File outputDirectory, List<String> formats,
			Properties viewResources);

	int countStories(); 
	
	int countScenarios();

	int countFailedScenarios();
	
}
