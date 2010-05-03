package org.jbehave.core.steps.pico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.MostUsefulStepsConfiguration;
import org.jbehave.core.steps.Steps;
import org.jbehave.core.steps.StepsConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.AbstractInjector;
import org.picocontainer.injectors.ConstructorInjection;

public class PicoStepsFactoryBehaviour {

    private static Field stepsInstance;

    @Before
    public void setUp() throws NoSuchFieldException {
        stepsInstance = Steps.class.getDeclaredField("instance");
        stepsInstance.setAccessible(true);
    }

    private MutablePicoContainer createPicoContainer() {
        return new DefaultPicoContainer(new Caching().wrap(new ConstructorInjection()));
    }

    @Test
    public void ensureThatStepsCanBeCreated() throws NoSuchFieldException, IllegalAccessException {
        // Given
        MutablePicoContainer parent = createPicoContainer();
        parent.as(Characteristics.USE_NAMES).addComponent(FooSteps.class);
        PicoStepsFactory factory = new PicoStepsFactory(new MostUsefulStepsConfiguration(), parent);
        // When
        CandidateSteps[] steps = factory.createCandidateSteps();
        // Then 
        assertFooStepsFound(steps);
    }


    @Test
    public void ensureThatStepsWithStepsWithDependencyCanBeCreated() throws NoSuchFieldException, IllegalAccessException {
        MutablePicoContainer parent = createPicoContainer();
        parent.as(Characteristics.USE_NAMES).addComponent(FooStepsWithDependency.class);
        parent.addComponent(Integer.class, 42);
        // When
        PicoStepsFactory factory = new PicoStepsFactory(new MostUsefulStepsConfiguration(), parent);
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


    @Test(expected=AbstractInjector.UnsatisfiableDependenciesException.class)
    public void ensureThatStepsWithMissingDependenciesCannotBeCreated() throws NoSuchFieldException, IllegalAccessException {
        MutablePicoContainer parent = createPicoContainer();
        parent.as(Characteristics.USE_NAMES).addComponent(FooStepsWithDependency.class);
        PicoStepsFactory factory = new PicoStepsFactory(new MostUsefulStepsConfiguration(), parent);
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

        public FooStepsWithDependency(Integer steps) {
            this.integer = steps;
        }

    }
}
