package org.jbehave.core.steps.pico;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Steps;
import org.junit.jupiter.api.Test;
import org.picocontainer.Characteristics;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.behaviors.Caching;
import org.picocontainer.injectors.AbstractInjector;
import org.picocontainer.injectors.ConstructorInjection;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PicoStepsFactoryBehaviour {

    private MutablePicoContainer createPicoContainer() {
        return new DefaultPicoContainer(new Caching().wrap(new ConstructorInjection()));
    }

    @Test
    void assertThatStepsCanBeCreated() {
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
    void assertThatStepsWithStepsWithDependencyCanBeCreated() {
        MutablePicoContainer parent = createPicoContainer();
        parent.as(Characteristics.USE_NAMES).addComponent(FooStepsWithDependency.class);
        parent.addComponent(Integer.class, 42);
        // When
        PicoStepsFactory factory = new PicoStepsFactory(new MostUsefulConfiguration(), parent);
        List<CandidateSteps> steps = factory.createCandidateSteps();
        // Then
        assertFooStepsFound(steps);
        assertThat(((FooStepsWithDependency) stepsInstance(steps.get(0))).integer, equalTo(42));
    }

    private void assertFooStepsFound(List<CandidateSteps> steps) {
        assertThat(steps.size(), equalTo(1));
        boolean actual1 = steps.get(0) instanceof CandidateSteps;
        assertThat(actual1, is(true));
        Object instance = stepsInstance(steps.get(0));
        boolean actual = instance instanceof FooSteps;
        assertThat(actual, is(true));
    }

    private Object stepsInstance(CandidateSteps candidateSteps) {
        return ((Steps)candidateSteps).instance();
    }

    @Test
    void assertThatStepsWithMissingDependenciesCannotBeCreated() {
        MutablePicoContainer parent = createPicoContainer();
        parent.as(Characteristics.USE_NAMES).addComponent(FooStepsWithDependency.class);
        PicoStepsFactory factory = new PicoStepsFactory(new MostUsefulConfiguration(), parent);
        // When
        assertThrows(AbstractInjector.UnsatisfiableDependenciesException.class,
                () -> factory.createInstanceOfType(FooStepsWithDependency.class));
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
