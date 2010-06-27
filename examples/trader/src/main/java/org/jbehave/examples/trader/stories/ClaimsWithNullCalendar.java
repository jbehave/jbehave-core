package org.jbehave.examples.trader.stories;

import java.util.Calendar;

import org.jbehave.core.annotations.AsParameterConverter;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.examples.trader.converters.CalendarConverter;

public class ClaimsWithNullCalendar extends JUnitStory {

	public ClaimsWithNullCalendar() {
		addSteps(new InstanceStepsFactory(configuration(), new CalendarSteps())
				.createCandidateSteps());
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
		
		@AsParameterConverter
		public Calendar calendarDate(String value){
		    return (Calendar) new CalendarConverter("dd/MM/yyyy").convertValue(value, Calendar.class);
		}

	}

}
