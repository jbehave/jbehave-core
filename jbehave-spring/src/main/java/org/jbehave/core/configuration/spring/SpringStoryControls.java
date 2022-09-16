package org.jbehave.core.configuration.spring;

import org.jbehave.core.embedder.StoryControls;

/**
 * Extends {@link StoryControls} to provide getter/setter methods for all
 * control properties, so it can be used by Spring's property mechanism.
 */
public class SpringStoryControls extends StoryControls {

    public boolean getDryRun() {
        return dryRun();
    }

    public void setDryRun(boolean dryRun) {
        doDryRun(dryRun);
    }

    public boolean isResetStateBeforeStory() {
        return resetStateBeforeStory();
    }

    public void setResetStateBeforeStory(boolean resetStateBeforeStory) {
        doResetStateBeforeStory(resetStateBeforeStory);
    }

    public boolean isResetStateBeforeScenario() {
        return resetStateBeforeScenario();
    }

    public void setResetStateBeforeScenario(boolean resetStateBeforeScenario) {
        doResetStateBeforeScenario(resetStateBeforeScenario);
    }

    public boolean isSkipScenariosAfterFailure() {
        return skipScenariosAfterFailure();
    }

    public void setSkipScenariosAfterFailure(boolean skipScenariosAfterFailure) {
        doSkipScenariosAfterFailure(skipScenariosAfterFailure);
    }

    public boolean isSkipBeforeAndAfterScenarioStepsIfGivenStory() {
        return skipBeforeAndAfterScenarioStepsIfGivenStory();
    }

    public void setSkipBeforeAndAfterScenarioStepsIfGivenStory(boolean skipBeforeAndAfterScenarioStepsIfGivenStory) {
        doSkipBeforeAndAfterScenarioStepsIfGivenStory(skipBeforeAndAfterScenarioStepsIfGivenStory);
    }

    public boolean isIgnoreMetaFiltersIfGivenStory() {
        return ignoreMetaFiltersIfGivenStory();
    }

    public void setIgnoreMetaFiltersIfGivenStory(boolean ignoreMetaFiltersIfGivenStory) {
        doIgnoreMetaFiltersIfGivenStory(ignoreMetaFiltersIfGivenStory);
    }

    public boolean isMetaByRow() {
        return metaByRow();
    }

    public void setMetaByRow(boolean metaByRow) {
        doMetaByRow(metaByRow);
    }

    public String getStoryMetaPrefix() {
        return storyMetaPrefix();
    }

    public void setStoryMetaPrefix(String storyMetaPrefix) {
        useStoryMetaPrefix(storyMetaPrefix);
    }

    public String getScenarioMetaPrefix() {
        return scenarioMetaPrefix();
    }

    public void setScenarioMetaPrefix(String scenarioMetaPrefix) {
        useScenarioMetaPrefix(scenarioMetaPrefix);
    }

    public boolean isSkipStoryIfGivenStoryFailed() {
        return skipStoryIfGivenStoryFailed();
    }

    public void setSkipStoryIfGivenStoryFailed(boolean skipStoryIfGivenStoryFailed) {
        doSkipStoryIfGivenStoryFailed(skipStoryIfGivenStoryFailed);
    }

    public String getStoryIndexFormat() {
        return storyIndexFormat();
    }

    public void setStoryIndexFormat(String storyIndexFormat) {
        useStoryIndexFormat(storyIndexFormat);
    }
}
