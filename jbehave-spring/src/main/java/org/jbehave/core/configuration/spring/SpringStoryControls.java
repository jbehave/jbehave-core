package org.jbehave.core.configuration.spring;

import org.jbehave.core.embedder.StoryControls;

/**
 * Extends {@link StoryControls} to provide getter/setter methods for all
 * control properties, so it can be used by Spring's property mechanism.
 */
public class SpringStoryControls extends StoryControls {

    public boolean getDryRun(){
        return dryRun();
    }
    
    public void setDryRun(boolean dryRun){
        doDryRun(dryRun);
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
    
}
