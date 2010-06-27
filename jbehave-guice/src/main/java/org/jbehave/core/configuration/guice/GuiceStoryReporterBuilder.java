package org.jbehave.core.configuration.guice;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.jbehave.core.reporters.StoryReporterBuilder;

public class GuiceStoryReporterBuilder extends StoryReporterBuilder {

    public List<Format> getFormats() {
        return formats();
    }

    public void setFormats(List<Format> formats) {
        withFormats(formats.toArray(new Format[formats.size()]));
    }

    public String getOutputDirectory() {
        return outputDirectory().getPath();
    }

    public void setOutputDirectory(String outputDirectory) {
        withOutputDirectory(outputDirectory);
    }

    public URL getCodeLocation() {
        return codeLocation();
    }

    public void setCodeLocation(URL codeLocation) {
        withCodeLocation(codeLocation);
    }

    public Properties getViewResources() {
        return viewResources();
    }

    public void setViewResources(Properties viewResources) {
        withViewResources(viewResources);
    }

    public boolean isReportFailureTrace() {
        return reportFailureTrace();
    }

    public void setReportFailureTrace(boolean reportFailureTrace) {
        withFailureTrace(reportFailureTrace);
    }
}
