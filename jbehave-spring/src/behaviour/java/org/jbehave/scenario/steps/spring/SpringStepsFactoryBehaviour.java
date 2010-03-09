package org.jbehave.scenario.steps.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.steps.CandidateSteps;
import org.jbehave.scenario.steps.Steps;
import org.jbehave.scenario.steps.StepsConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringStepsFactoryBehaviour {

    private static Field stepsInstance;

    @Before
    public void setUp() throws NoSuchFieldException {
        stepsInstance = Steps.class.getDeclaredField("instance");
        stepsInstance.setAccessible(true);
    }

    private ListableBeanFactory createBeanFactory(String... xmlResources) {
        return (ListableBeanFactory)new ClassPathXmlApplicationContext(xmlResources);
    }

    @Test
    public void ensureThatStepsCanBeCreated() throws NoSuchFieldException, IllegalAccessException {
        // Given
        ListableBeanFactory parent = createBeanFactory("org/jbehave/scenario/steps/spring/steps.xml");
        SpringStepsFactory factory = new SpringStepsFactory(new StepsConfiguration(), parent);
        // When
        CandidateSteps[] steps = factory.createCandidateSteps();
        // Then 
        assertFooStepsFound(steps);
    }


    @Test
    public void ensureThatStepsWithStepsWithDependencyCanBeCreated() throws NoSuchFieldException, IllegalAccessException {
        ListableBeanFactory parent = createBeanFactory("org/jbehave/scenario/steps/spring/steps-with-dependency.xml");
        // When
        SpringStepsFactory factory = new SpringStepsFactory(new StepsConfiguration(), parent);
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


    @Test(expected=BeanDefinitionStoreException.class)
    public void ensureThatStepsWithMissingDependenciesCannotBeCreated() throws NoSuchFieldException, IllegalAccessException {
        ListableBeanFactory parent = createBeanFactory("org/jbehave/scenario/steps/spring/steps-with-missing-depedency.xml");
        SpringStepsFactory factory = new SpringStepsFactory(new StepsConfiguration(), parent);
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
}
