package org.jbehave.core.model;

import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.OutcomesTable.Outcome;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class OutcomesTableBehaviour {

    @Test
    void shouldDoNothingIfOutcomesVerified() {
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
    void shouldThrowExceptionIfOutcomesFail() {
        OutcomesTable table = new OutcomesTable();
        Object one = "one";
        Boolean two = true;
        table.addOutcome("a success", one, equalTo(one));
        // add a failed outcome
        table.addOutcome("a failure", two, is(false));
        try {
            table.verify();
            throw new AssertionError("Exception was not thrown");
        } catch (UUIDExceptionWrapper ce) {
            OutcomesFailed e = (OutcomesFailed) ce.getCause();
            assertThat(e.outcomesTable().getOutcomes().size(), equalTo(2));
            List<Outcome<?>> failedOutcomes = e.outcomesTable().getFailedOutcomes();
            assertThat(failedOutcomes.size(), equalTo(1));
            Outcome<?> outcome = failedOutcomes.get(0);
            assertThat(outcome.getDescription(), equalTo("a failure"));
            assertThat((Boolean) outcome.getValue(), is(true));
        }
    }

    @Test
    void shouldAllowStringRepresentationOfOutcomes() {
        OutcomesTable table = new OutcomesTable();
        Object one = "one";
        Boolean two = true;
        table.addOutcome("a success", one, equalTo(one));
        // add a non-failed outcome
        table.addOutcome("a failure", two, is(false));
        assertThat(table.asString(), equalTo("|Description|Value|Matcher|Verified|\n"
                + "|a success|one|\"one\"|true|\n" + "|a failure|true|is <false>|false|\n"));
        List<Outcome<?>> outcomes = table.getOutcomes();
        assertThat(outcomes.get(0).toString(), equalTo("OutcomesTable.Outcome[description=a success,matcher=\"one\",value=one,verified=true]"));
        assertThat(outcomes.get(1).toString(), equalTo("OutcomesTable.Outcome[description=a failure,matcher=is <false>,value=true,verified=false]"));
    }

}
