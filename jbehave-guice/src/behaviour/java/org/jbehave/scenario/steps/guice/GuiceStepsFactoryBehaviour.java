package org.jbehave.scenario.steps.guice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.steps.CandidateSteps;
import org.jbehave.scenario.steps.Steps;
import org.jbehave.scenario.steps.StepsConfiguration;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;

public class GuiceStepsFactoryBehaviour {

    private static Field stepsInstance;

    @Before
    public void setUp() throws NoSuchFieldException {
        stepsInstance = Steps.class.getDeclaredField("instance");
        stepsInstance.setAccessible(true);
    }

    @Test
    public void ensureThatStepsCanBeCreated() throws NoSuchFieldException, IllegalAccessException {
        // Given
        Injector parent = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {              
                bind(FooSteps.class).in(Scopes.SINGLETON);
            }
          });

        GuiceStepsFactory factory = new GuiceStepsFactory(new StepsConfiguration(), parent);
        // When
        CandidateSteps[] steps = factory.createCandidateSteps();
        // Then 
        assertFooStepsFound(steps);
    }


    @Test
    public void ensureThatStepsWithStepsWithDependencyCanBeCreated() throws NoSuchFieldException, IllegalAccessException {
        Injector parent = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
              bind(Integer.class).toInstance(42);
              bind(FooStepsWithDependency.class).in(Scopes.SINGLETON);
            }
          });

        // When
        GuiceStepsFactory factory = new GuiceStepsFactory(new StepsConfiguration(), parent);
        CandidateSteps[] steps = factory.createCandidateSteps();
        // Then
        assertFooStepsFound(steps);
        assertEquals(42, (int) ((FooStepsWithDependency) stepsInstance.get(steps[0])).integer);
    }

    private void assertFooStepsFound(CandidateSteps[] steps) throws NoSuchFieldException, IllegalAccessException {
        assertEquals(1, steps.length);
        assertTrue(steps[0] instanceof CandidateSteps);
        Object instance = stepsInstance.get(steps[0]);
        assertTrue(instance instanceof FooSteps);
    }


    @Test(expected=CreationException.class)
    public void ensureThatStepsWithMissingDependenciesCannotBeCreated() throws NoSuchFieldException, IllegalAccessException {
        Injector parent = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
              bind(FooStepsWithDependency.class);
            }
          });
        GuiceStepsFactory factory = new GuiceStepsFactory(new StepsConfiguration(), parent);
        // When
        factory.createCandidateSteps();
        // Then ... expected exception is thrown        
    }

    public static class FooSteps {

        @Given("a step with a $param")
        public void aStepWithAParam(String param) {
        }

    }

    public static class FooStepsWithDependency extends FooSteps {
        private final Integer integer;

        @Inject
        public FooStepsWithDependency(Integer steps) {
            this.integer = steps;
        }

    }
}
