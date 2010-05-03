package org.jbehave.examples.trader.stories;

import java.util.Calendar;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.MostUsefulStoryConfiguration;
import org.jbehave.core.StoryConfiguration;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.parser.UnderscoredCamelCaseResolver;
import org.jbehave.core.reporters.PrintStreamOutput;
import org.jbehave.core.steps.MostUsefulStepsConfiguration;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.StepsConfiguration;
import org.jbehave.core.steps.StepsFactory;
import org.jbehave.examples.trader.converters.CalendarConverter;

public class ClaimsWithNullCalendar extends JUnitStory {

    public ClaimsWithNullCalendar() {
        StoryConfiguration storyConfiguration = new MostUsefulStoryConfiguration();
        storyConfiguration.useStoryPathResolver(new UnderscoredCamelCaseResolver(".story"));        
        storyConfiguration.useStoryReporter(new PrintStreamOutput());
        useConfiguration(storyConfiguration);

        StepsConfiguration stepsConfiguration = new MostUsefulStepsConfiguration();
		stepsConfiguration.useParameterConverters(new ParameterConverters(
				stepsConfiguration.monitor(), new CalendarConverter("dd/MM/yyyy"))); 
        addSteps(new StepsFactory(stepsConfiguration).createCandidateSteps(new CalendarSteps()));
    }

    public static class CalendarSteps {
     
        @Given("a plan with calendar date of <date>") 
        public void aPlanWithCalendar(@Named("date") Calendar calendar) {
            System.out.println(calendar);
        }
        
        @Then("the claimant should receive an amount of <amount>")         
        public void theClaimantReceivesAmount(@Named("amount") double amount) {
            System.out.println(amount);
        }

    }

}
