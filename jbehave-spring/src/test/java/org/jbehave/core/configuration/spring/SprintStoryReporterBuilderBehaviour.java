package org.jbehave.core.configuration.spring;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.reporters.StoryReporterBuilder.Format;
import org.junit.Test;

public class SprintStoryReporterBuilderBehaviour {

    @Test
    public void shouldAllowUseOfGettersAndSetters(){
        SpringStoryReporterBuilder builder = new SpringStoryReporterBuilder();
        
        URL codeLocation = CodeLocations.codeLocationFromClass(this.getClass());
        builder.setCodeLocation(codeLocation);
        assertThat(builder.getCodeLocation(), equalTo(codeLocation));
        
        List<Format> formats = asList(Format.CONSOLE, Format.HTML);
        builder.setFormats(formats);
        assertThat(builder.getFormats(), equalTo(formats));
        
        Keywords keywords = new LocalizedKeywords();
        builder.setKeywords(keywords);
        assertThat(builder.getKeywords(), equalTo(keywords));
        
        String relativeDirectory = "reports";
        builder.setRelativeDirectory(relativeDirectory);
        assertThat(builder.getRelativeDirectory(), equalTo(relativeDirectory));
        assertThat(builder.getOutputDirectory(), endsWith(relativeDirectory));
        
        Properties viewResources = new Properties();
        builder.setViewResources(viewResources);
        assertThat(builder.getViewResources(), equalTo(viewResources));
        
        boolean reportFailureTrace = true;
        builder.setReportFailureTrace(reportFailureTrace);
        assertThat(builder.isReportFailureTrace(), equalTo(reportFailureTrace));
    }
}
