package org.jbehave.examples.trader.stories;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.NullStoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;

public class StepMonitoring extends JUnitStory {

    @Override
    public Configuration configuration() {
        return super.configuration().useStoryReporterBuilder(new StoryReporterBuilder().withReporters(new StepMonitoringReporter()));
    }
 
    @Override
    public InjectableStepsFactory stepsFactory() {
        return new InstanceStepsFactory(configuration(), new MonitoringSteps());
    }
    
    public static class StepMonitoringReporter extends NullStoryReporter {

        @Override
        public void beforeStep(String step) {
            System.out.println("beforeStep: "+step);
        }

        @Override
        public void successful(String step) {
            System.out.println("successful: "+step);
        }

        @Override
        public void ignorable(String step) {
            System.out.println("ignorable: "+step);
        }

        @Override
        public void pending(String step) {
            System.out.println("pending: "+step);
        }

        @Override
        public void notPerformed(String step) {
            System.out.println("notPerformed: "+step);
        }

        @Override
        public void failed(String step, Throwable cause) {            
            System.out.println("failed: "+step);
        }
        
    }

    public static class MonitoringSteps {
        
        @Given("a successful step")
        public void givenASuccessfulStep(){            
        }

        @Given("a failing step")
        public void givenAFailingStep(){
            throw new RuntimeException("Bum!");
        }

        @Then("following step should be performed")
        public void followingStepShouldBePerformed(){
        }

        @Then("following step should not be performed")
        public void followingStepShouldNotBePerformed(){
        }

    }

}
