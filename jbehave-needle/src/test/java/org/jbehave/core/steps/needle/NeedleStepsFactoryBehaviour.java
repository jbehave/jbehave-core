package org.jbehave.core.steps.needle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import javax.inject.Inject;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.needle.NeedleInjectionProvider;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.configuration.needle.ValueGetterProvider;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.Steps;
import org.junit.jupiter.api.Test;
import org.needle4j.injection.InjectionProvider;

public class NeedleStepsFactoryBehaviour {

    @Test
    void stepsShouldBeCreated() {
        // Given
        final InjectableStepsFactory factory = new NeedleStepsFactory(new MostUsefulConfiguration(), FooSteps.class);
        // When
        List<CandidateSteps> steps = factory.createCandidateSteps();
        // Then
        assertThat(steps.size(), equalTo(1));
        boolean actual1 = steps.get(0) instanceof CandidateSteps;
        assertThat(actual1, is(true));
        Object instance = stepsInstance(steps.get(0));
        boolean actual = instance instanceof FooSteps;
        assertThat(actual, is(true));
    }

    @Test
    void stepsShouldContainInjectedDependencies() {
        // Given
        final InjectableStepsFactory factory = new NeedleStepsFactory(new MostUsefulConfiguration(),
                FooStepsWithDependency.class);
        // When
        List<CandidateSteps> steps = factory.createCandidateSteps();
        // Then
        assertThat(steps.size(), equalTo(1));
        boolean actual1 = steps.get(0) instanceof CandidateSteps;
        assertThat(actual1, is(true));
        Object instance = stepsInstance(steps.get(0));
        boolean actual = instance instanceof FooStepsWithDependency;
        assertThat(actual, is(true));
        FooStepsWithDependency withDependency = (FooStepsWithDependency) instance;
        assertThat(withDependency.getter, is(notNullValue()));
        assertThat((String)withDependency.getter.getValue(), equalTo(ValueGetter.VALUE));
    }

    private Object stepsInstance(CandidateSteps candidateSteps) {
        return ((Steps) candidateSteps).instance();
    }

    public static class FooSteps {
        @NeedleInjectionProvider
        private InjectionProvider<?> provider = new ValueGetterProvider();

        @Given("a step with a $param")
        public void stepWithAParam(String param) {
        }

    }

    public static class FooStepsWithDependency extends FooSteps {
        @Inject
        private ValueGetter getter;

        public ValueGetter getGetter() {
            return getter;
        }

    }
}
