package org.jbehave.core.steps.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.spring.SpringStepsFactoryBehaviour.FooSteps;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

public class SpringStepsFactoryAOPBehaviour {

	@Test
	public void aopEnvelopedStepsCanBeCreated() {
		// Given
		ApplicationContext context = createApplicationContext(StepsWithAOPAnnotationConfiguration.class
				.getName());
		SpringStepsFactory factory = new SpringStepsFactory(
				new MostUsefulConfiguration(), context);
		// When
		List<CandidateSteps> steps = factory.createCandidateSteps();
		// Then
		assertAOPFooStepsFound(steps);
	}

	private void assertAOPFooStepsFound(List<CandidateSteps> steps) {
		// // Only one returned, the IFooSteps will not be detected
        assertThat(steps.size(), equalTo(1));
        assertThat(steps, hasItem(isCandidateStepInstanceOf(FooSteps.class)));
		// Make it explicit that the steps bean with the annotation in the
		// interface is not provided
		assertThat(steps,
				not(hasItem(isCandidateStepInstanceOf(IFooSteps.class))));
	}

	private ApplicationContext createApplicationContext(String... resources) {
		return new SpringApplicationContextFactory(resources)
				.createApplicationContext();
	}

	@Configuration
	@EnableAspectJAutoProxy
	public static class StepsWithAOPAnnotationConfiguration {

		@Bean
		public FooAspect fooAspect() {
			return new FooAspect();
		}

		// JDK Proxy
		@Bean
		@Scope(proxyMode = ScopedProxyMode.INTERFACES)
		public SpringStepsFactoryAOPBehaviour.IFooSteps iFooSteps() {
			return new SpringStepsFactoryAOPBehaviour.FooStepsImpl();
		}

		// CGLIB-based
		@Bean
		@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
		public SpringStepsFactoryBehaviour.FooSteps fooSteps() {
			return new SpringStepsFactoryBehaviour.FooSteps();
		}

	}

	@Aspect
	public static class FooAspect {
		public final AtomicInteger executions = new AtomicInteger(0);

		@Around(value = "bean(fooSteps) || bean(iFooSteps)")
		public Object around(ProceedingJoinPoint pjp) throws Throwable {
			try {
				return pjp.proceed();

			} finally {
				System.err.println("Accounted for "
						+ executions.incrementAndGet() + " executions");
			}
		}
	}

	public static interface IFooSteps {
		@Given("a step declared in an interface, with a $param")
		void aStepWithAParam(String param);
	}

	public static class FooStepsImpl implements IFooSteps {

		@Override
        public void aStepWithAParam(String param) {

		}

	}

	public static CandidateStepsInstanceOfMatcher isCandidateStepInstanceOf(
			Class<?> target) {
		return new CandidateStepsInstanceOfMatcher(target);
	}

	private static class CandidateStepsInstanceOfMatcher extends
			BaseMatcher<CandidateSteps> {

		private final Class<?> target;

		public CandidateStepsInstanceOfMatcher(Class<?> target) {
			this.target = target;
		}

		@Override
        public boolean matches(Object item) {
			if (item instanceof CandidateSteps) {
				Object instance = ((Steps) item).instance();
                return target.isAssignableFrom(instance.getClass());
			}
			return false;
		}

		@Override
        public void describeTo(Description description) {
			description
					.appendText("Step class instantiated from this CandidateStep is of type "
							+ target.getName());
		}

	}
}
