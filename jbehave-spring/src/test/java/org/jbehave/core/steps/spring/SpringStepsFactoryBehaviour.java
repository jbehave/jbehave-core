package org.jbehave.core.steps.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Steps;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ApplicationContext;

public class SpringStepsFactoryBehaviour {

    private static Field stepsInstance;

    @Before
    public void setUp() throws NoSuchFieldException {
        stepsInstance = Steps.class.getDeclaredField("instance");
        stepsInstance.setAccessible(true);
    }

    private ApplicationContext createApplicationContext(String... resourceLocations) {
        return new SpringApplicationContextFactory(resourceLocations).createApplicationContext();
    }

    @Test
    public void assertThatStepsCanBeCreated() throws NoSuchFieldException, IllegalAccessException {
        // Given
        ApplicationContext context = createApplicationContext("org/jbehave/core/steps/spring/steps.xml");
        SpringStepsFactory factory = new SpringStepsFactory(new MostUsefulConfiguration(), context);
        // When
        List<CandidateSteps> steps = factory.createCandidateSteps();
        // Then 
        assertFooStepsFound(steps);
    }


    @Test
    public void assertThatStepsWithStepsWithDependencyCanBeCreated() throws NoSuchFieldException, IllegalAccessException {
        ApplicationContext context = createApplicationContext("org/jbehave/core/steps/spring/steps-with-dependency.xml");
        // When
        SpringStepsFactory factory = new SpringStepsFactory(new MostUsefulConfiguration(), context);
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


    @Test(expected=BeanDefinitionStoreException.class)
    public void assertThatStepsWithMissingDependenciesCannotBeCreated() throws NoSuchFieldException, IllegalAccessException {
        ApplicationContext context = createApplicationContext("org/jbehave/core/steps/spring/steps-with-missing-depedency.xml");
        SpringStepsFactory factory = new SpringStepsFactory(new MostUsefulConfiguration(), context);
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

        public FooStepsWithDependency(Integer integer) {
            this.integer = integer;
        }

    }
    
    public static abstract class AbstractSteps {
        
    }
}
