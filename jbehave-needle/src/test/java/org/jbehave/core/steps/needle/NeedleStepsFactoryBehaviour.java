package org.jbehave.core.steps.needle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.inject.Inject;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.needle.NeedleInjectionProvider;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.configuration.needle.ValueGetterProvider;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.Steps;
import org.junit.Test;

import de.akquinet.jbosscc.needle.injection.InjectionProvider;

public class NeedleStepsFactoryBehaviour {

	@Test
	public void stepsShouldBeCreated() throws NoSuchFieldException, IllegalAccessException {
		// Given
		final InjectableStepsFactory factory = new NeedleStepsFactory(new MostUsefulConfiguration(), FooSteps.class);
		// When
		List<CandidateSteps> steps = factory.createCandidateSteps();
		// Then
		assertEquals(1, steps.size());
		assertTrue(steps.get(0) instanceof CandidateSteps);
		Object instance = stepsInstance(steps.get(0));
		assertTrue(instance instanceof FooSteps);
	}

	@Test
	public void stepsShouldContainInjectedDependencies() throws NoSuchFieldException, IllegalAccessException {
		// Given
		final InjectableStepsFactory factory = new NeedleStepsFactory(new MostUsefulConfiguration(),
				FooStepsWithDependency.class);
		// When
		List<CandidateSteps> steps = factory.createCandidateSteps();
		// Then
		assertEquals(1, steps.size());
		assertTrue(steps.get(0) instanceof CandidateSteps);
		Object instance = stepsInstance(steps.get(0));
		assertTrue(instance instanceof FooStepsWithDependency);
		FooStepsWithDependency withDependency = (FooStepsWithDependency) instance;
		assertNotNull(withDependency.getter);
		assertEquals(ValueGetter.VALUE, withDependency.getter.getValue());
	}

	private Object stepsInstance(CandidateSteps candidateSteps) {
		return ((Steps) candidateSteps).instance();
	}

	public static class FooSteps {
		@NeedleInjectionProvider
		private InjectionProvider<?> provider = new ValueGetterProvider();

		@Given("a step with a $param")
		public void aStepWithAParam(String param) {
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