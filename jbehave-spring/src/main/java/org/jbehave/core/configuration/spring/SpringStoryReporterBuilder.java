package org.jbehave.core.configuration.spring;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.reporters.FilePrintStreamFactory.FilePathResolver;
import org.jbehave.core.reporters.StoryReporterBuilder;

/**
 * Extends {@link StoryReporterBuilder} to provide getter/setter methods for all
 * builder properties, so it can be used by Spring's property mechanism.
 */
public class SpringStoryReporterBuilder extends StoryReporterBuilder {

    public List<org.jbehave.core.reporters.Format> getFormats() {
        return formats();
    }

    public void setFormats(List<org.jbehave.core.reporters.Format> formats) {
        withFormats(formats.toArray(new org.jbehave.core.reporters.Format[formats.size()]));
    }

    public String getOutputDirectory() {
        return outputDirectory().getPath();
    }

    public String getRelativeDirectory() {
        return relativeDirectory();
    }

    public void setRelativeDirectory(String relativeDirectory) {
        withRelativeDirectory(relativeDirectory);
    }

    public URL getCodeLocation() {
        return codeLocation();
    }

    public void setCodeLocation(URL codeLocation) {
        withCodeLocation(codeLocation);
    }

    public Keywords getKeywords() {
        return keywords();
    }
    
    public void setKeywords(Keywords keywords) {
        withKeywords(keywords);
    }
    
    public FilePathResolver getPathResolver() {
        return pathResolver();
    }

    public void setPathResolver(FilePathResolver pathResolver) {
        withPathResolver(pathResolver);
    }

    public boolean isReportFailureTrace() {
        return reportFailureTrace();
    }

    public void setReportFailureTrace(boolean reportFailureTrace) {
        withFailureTrace(reportFailureTrace);
    }


    public boolean isReportFailureTraceCompression() {
        return compressFailureTrace();
    }

    public void setReportFailureTraceCompression(boolean reportFailureTraceCompression) {
        withFailureTraceCompression(reportFailureTraceCompression);
    }

    public Properties getViewResources() {
        return viewResources();
    }

    public void setViewResources(Properties viewResources) {
        withViewResources(viewResources);
    }

    public boolean isMultiThreading() {
        return multiThreading();
    }

    public void setMultiThreading(boolean multiThreading) {
        withMultiThreading(multiThreading);
    }

}
