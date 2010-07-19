package org.jbehave.examples.trader.stories;

import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.reporters.StoryReporterBuilder.Format;
import org.jbehave.core.steps.InstanceStepsFactory;

public class UnmatchedSearchTerm extends JUnitStory {
    public UnmatchedSearchTerm() {
        Class<? extends JUnitStory> storyClass = this.getClass();
        Configuration configuration = new MostUsefulConfiguration()
            .useStoryLoader(new LoadFromClasspath(storyClass.getClassLoader()))
            .useStoryReporterBuilder(new StoryReporterBuilder().withFormats(Format.CONSOLE));
        useConfiguration(configuration);
        addSteps(new InstanceStepsFactory(configuration, new SearchSteps()).createCandidateSteps());
    }

    public class SearchSteps {
        @Given("that I am on Google's Homepage")
        public void onGoogle() {
            System.out.println(" ... on Google!");
        }

        @When("I enter the search term <ridiculousSearchTerm> and proceed")
        public void enterSearchTermAndProceed(@Named("ridiculousSearchTerm") String ridiculousSearchTerm) {
            System.out.println(" ... entering " + ridiculousSearchTerm + " into box and clicking continue!");
        }

        @Then("I should see ridiculous things")
        public void seeResults() {
            System.out.println(" ... ahhh, so much pink!!!");
        }

        @AfterStory
        public void killBrowser() {
            System.out.println(" ... Browser has been put to rest \n\n");
        }

    }
}