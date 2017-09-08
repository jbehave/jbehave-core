package org.jbehave.examples.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.Assert;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

public class FailureCorrelationStories extends CoreStories {

    private List<String> failures = new ArrayList<String>();

    public FailureCorrelationStories() {
        configuredEmbedder().embedderControls()
                .doGenerateViewAfterStories(true)
                .doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(true)
                .useThreads(1).useStoryTimeouts("60");
    }

    @Override
    public Configuration configuration() {
        return super.configuration().useStoryControls(new StoryControls().doResetStateBeforeScenario(true));
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), this);
    }

    @Override
    protected List<String> storyPaths() {
        return new StoryFinder().findPaths(codeLocationFromClass(this.getClass()), "**/failure_correlation*.story", "");                
    }

    @When("a failure occurs in story $count")
    public void whenSomethingHappens(int count){
        throw new RuntimeException("BUM! in story "+count);
    }
    
    @AfterScenario(uponOutcome = Outcome.FAILURE)
    public void afterScenarioFailure(UUIDExceptionWrapper failure) throws Exception {
        System.out.println("After Failed Scenario ...");
        File file = new File("target/failures/"+failure.getUUID().toString());
        file.getParentFile().mkdirs();
        file.createNewFile();
        failures.add(file.toString());
        System.out.println("Failure: "+file);
    }

    @AfterStories
    public void afterStories(){
        Assert.assertThat(failures.size(), Matchers.equalTo(2));
    }
}
