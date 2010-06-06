package org.jbehave.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;

import org.jbehave.core.model.OutcomesTable.Outcome;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.junit.Test;

public class OutcomesTableBehaviour {

	@Test
	public void shouldDoNothingIfOutcomesVerified() {
		OutcomesTable table = new OutcomesTable();
		Object one = "one";
		boolean two = true;
		table.addOutcome("a success", one, equalTo(one));
		table.addOutcome("another success", two, is(true));
		table.verify();
		assertThat(table.getOutcomes().size(), equalTo(2));
		assertThat(table.getFailedOutcomes().size(), equalTo(0));
	}

	@Test
	public void shouldThrowExceptionIfOutcomesFail() {
		OutcomesTable table = new OutcomesTable();
		Object one = "one";
		Boolean two = true;
		table.addOutcome("a success", one, equalTo(one));
		// add a non-failed outcome
		table.addOutcome("a failure", two, is(false));
		try {
			table.verify();
		} catch (OutcomesFailed e) {
			assertThat(e.outcomesTable().getOutcomes().size(), equalTo(2));
			List<Outcome<?>> failedOutcomes = e.outcomesTable()
					.getFailedOutcomes();
			assertThat(failedOutcomes.size(), equalTo(1));
			Outcome<?> outcome = failedOutcomes.get(0);
			assertThat(outcome.getDescription(), equalTo("a failure"));
			assertThat((Boolean)outcome.getActual(), is(true));
		}
	}

}
