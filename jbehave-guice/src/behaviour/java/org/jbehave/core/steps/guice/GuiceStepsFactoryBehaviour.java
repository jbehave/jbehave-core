package org.jbehave.core.steps.guice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.steps.AbstractStepsFactory;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Steps;
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
    public void assertThatStepsCanBeCreated() throws NoSuchFieldException, IllegalAccessException {
        // Given
        Injector parent = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {              
                bind(FooSteps.class).in(Scopes.SINGLETON);
            }
          });

        AbstractStepsFactory factory = new GuiceStepsFactory(new MostUsefulConfiguration(), parent);
        // When
        List<CandidateSteps> steps = factory.createCandidateSteps();
        // Then 
        assertFooStepsFound(steps);
    }


    @Test
    public void assertThatStepsWithStepsWithDependencyCanBeCreated() throws NoSuchFieldException, IllegalAccessException {
        Injector parent = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
              bind(Integer.class).toInstance(42);
              bind(FooStepsWithDependency.class).in(Scopes.SINGLETON);
            }
          });

        // When
        AbstractStepsFactory factory = new GuiceStepsFactory(new MostUsefulConfiguration(), parent);
        List<CandidateSteps> steps = factory.createCandidateSteps();
        // Then
        assertFooStepsFound(steps);
        assertEquals(42, (int) ((FooStepsWithDependency) stepsInstance.get(steps.get(0))).integer);
    }

    private void assertFooStepsFound(List<CandidateSteps> steps) throws NoSuchFieldException, IllegalAccessException {
        assertEquals(1, steps.size());
        assertTrue(steps.get(0) instanceof CandidateSteps);
        Object instance = stepsInstance.get(steps.get(0));
        assertTrue(instance instanceof FooSteps);
    }


    @Test(expected=CreationException.class)
    public void assertThatStepsWithMissingDependenciesCannotBeCreated() throws NoSuchFieldException, IllegalAccessException {
        Injector parent = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
              bind(FooStepsWithDependency.class);
            }
          });
        AbstractStepsFactory factory = new GuiceStepsFactory(new MostUsefulConfiguration(), parent);
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
