package org.jbehave.examples.gherkin.steps;

import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.steps.Parameters;

public class GherkinSteps {
    
	private Car car;

    @Given("I have a license")
    public void givenIHaveALicense() {
    }
	
	@Given("I have a car")
	public void givenIHaveACar() {
		car = new Car();
	}

	@Then("I can drive them according to wheels: $table")
	public void thenICanDriveThemAccordingTo(ExamplesTable table) {
		OutcomesTable outcomes = new OutcomesTable();
		for ( Parameters row : table.getRowsAsParameters() ){
			Integer wheels = row.valueAs("wheels", Integer.class);
			Boolean canDriveWith = car.canDriveWith(wheels);
			outcomes.addOutcome("wheels "+wheels, canDriveWith, Matchers.is(row.valueAs("can_drive", Boolean.class)));
		}
		outcomes.verify();
	}
	
	private static class Car {

		public boolean canDriveWith(int wheels) {
			if ( wheels == 4 ) {
				return true;
			}
			return false;
		}
		
	}

}
