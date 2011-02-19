package org.jbehave.core.reporters;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.jbehave.core.model.StoryMaps;

/**
 * A view generator is responsible for creating a collective views of stories, 
 * either story maps or file-based reports of stories run.  
 */
public interface ViewGenerator {
    
    void generateMapsView(File outputDirectory, StoryMaps storyMaps,
            Properties viewResources);

    void generateReportsView(File outputDirectory, List<String> formats,
			Properties viewResources);

	ReportsCount getReportsCount();

	
}
