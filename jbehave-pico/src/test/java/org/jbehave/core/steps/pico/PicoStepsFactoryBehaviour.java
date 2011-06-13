package org.jbehave.core.steps.pico;

import java.util.List;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Steps;
import org.junit.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.AbstractInjector;
import org.picocontainer.injectors.ConstructorInjection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PicoStepsFactoryBehaviour {

    private MutablePicoContainer createPicoContainer() {
        return new DefaultPicoContainer(new Caching().wrap(new ConstructorInjection()));
    }

    @Test
    public void assertThatStepsCanBeCreated() throws NoSuchFieldException, IllegalAccessException {
        // Given
        MutablePicoContainer parent = createPicoContainer();
        parent.as(Characteristics.USE_NAMES).addComponent(FooSteps.class);
        PicoStepsFactory factory = new PicoStepsFactory(new MostUsefulConfiguration(), parent);
        // When
        List<CandidateSteps> steps = factory.createCandidateSteps();
        // Then 
        assertFooStepsFound(steps);
    }


    @Test
    public void assertThatStepsWithStepsWithDependencyCanBeCreated() throws NoSuchFieldException, IllegalAccessException {
        MutablePicoContainer parent = createPicoContainer();
        parent.as(Characteristics.USE_NAMES).addComponent(FooStepsWithDependency.class);
        parent.addComponent(Integer.class, 42);
        // When
        PicoStepsFactory factory = new PicoStepsFactory(new MostUsefulConfiguration(), parent);
        List<CandidateSteps> steps = factory.createCandidateSteps();
        // Then
        assertFooStepsFound(steps);
        assertEquals(42, (int) ((FooStepsWithDependency) stepsInstance(steps.get(0))).integer);
    }

    private void assertFooStepsFound(List<CandidateSteps> steps) throws NoSuchFieldException, IllegalAccessException {
        assertEquals(1, steps.size());
        assertTrue(steps.get(0) instanceof CandidateSteps);
        Object instance = stepsInstance(steps.get(0));
        assertTrue(instance instanceof FooSteps);
    }

    private Object stepsInstance(CandidateSteps candidateSteps) {
        return ((Steps)candidateSteps).instance();
    }

    @Test(expected=AbstractInjector.UnsatisfiableDependenciesException.class)
    public void assertThatStepsWithMissingDependenciesCannotBeCreated() throws NoSuchFieldException, IllegalAccessException {
        MutablePicoContainer parent = createPicoContainer();
        parent.as(Characteristics.USE_NAMES).addComponent(FooStepsWithDependency.class);
        PicoStepsFactory factory = new PicoStepsFactory(new MostUsefulConfiguration(), parent);
        // When
        factory.createInstanceOfType(FooStepsWithDependency.class);
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
