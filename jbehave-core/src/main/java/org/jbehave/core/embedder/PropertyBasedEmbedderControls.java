package org.jbehave.core.embedder;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.steps.ParameterConverters;

public class PropertyBasedEmbedderControls extends EmbedderControls {

    public static final String BATCH = "BATCH";
    public static final String IGNORE_FAILURE_IN_VIEW = "IGNORE_FAILURE_IN_VIEW";
    public static final String IGNORE_FAILURE_IN_STORIES = "IGNORE_FAILURE_IN_STORIES";
    public static final String GENERATE_VIEW_AFTER_STORIES = "GENERATE_VIEW_AFTER_STORIES";
    public static final String SKIP = "SKIP";
    public static final String VERBOSE_FAILURES = "VERBOSE_FAILURES";
    public static final String VERBOSE_FILTERING = "VERBOSE_FILTERING";
    public static final String STORY_TIMEOUT_IN_SECS = "STORY_TIMEOUT_IN_SECS";
    public static final String THREADS = "THREADS";

    private ParameterConverters converters = new ParameterConverters();

    @Override
    public boolean batch() {
        return propertyAs(BATCH, Boolean.class, super.batch());
    }

    @Override
    public boolean ignoreFailureInView() {
        return propertyAs(IGNORE_FAILURE_IN_VIEW, Boolean.class, super.ignoreFailureInView()); 
    }

    @Override
    public boolean ignoreFailureInStories() {
        return propertyAs(IGNORE_FAILURE_IN_STORIES, Boolean.class, super.ignoreFailureInStories()); 
    }

    @Override
    public boolean generateViewAfterStories() {
        return propertyAs(GENERATE_VIEW_AFTER_STORIES, Boolean.class, super.generateViewAfterStories()); 
    }

    @Override
    public boolean skip() {
        return propertyAs(SKIP, Boolean.class, super.skip()); 
    }
    
    @Override
    public boolean verboseFailures() {
        return propertyAs(VERBOSE_FAILURES, Boolean.class, super.verboseFailures()); 
    }

    @Override
    public boolean verboseFiltering() {
        return propertyAs(VERBOSE_FILTERING, Boolean.class, super.verboseFiltering()); 
    }
    
    @Override
    public long storyTimeoutInSecs() {
        return propertyAs(STORY_TIMEOUT_IN_SECS, Long.class, super.storyTimeoutInSecs()); 
    }

    @Override
    public int threads() {
        return propertyAs(THREADS, Integer.class, super.threads()); 
    }
   
    @SuppressWarnings("unchecked")
    private <T> T propertyAs(String name, Class<T> type, T defaultValue) {
        String property = System.getProperty(name);
        if ( property == null ){
            return defaultValue;
        }
        return (T) converters.convert(property, type);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
    
}
