package org.jbehave.examples.trader.stories;

import java.util.Calendar;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.examples.trader.converters.CalendarConverter;

public class ClaimsWithNullCalendar extends JUnitStory {

	public ClaimsWithNullCalendar() {
		Configuration configuration = new MostUsefulConfiguration()
				.useParameterConverters(new ParameterConverters().addConverters(
						new CalendarConverter("dd/MM/yyyy")));
		addSteps(new InstanceStepsFactory(configuration, new CalendarSteps())
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

	}

}
