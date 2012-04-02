package org.jbehave.core.steps.spring;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.Steps;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ApplicationContext;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class SpringStepsFactoryBehaviour {

    private ApplicationContext createApplicationContext(String... resourceLocations) {

        return new SpringApplicationContextFactory(resourceLocations).createApplicationContext();
    }

    @Test
    public void stepsCanBeCreated() {
        // Given
        ApplicationContext context = createApplicationContext("org/jbehave/core/steps/spring/steps.xml");
        SpringStepsFactory factory = new SpringStepsFactory(new MostUsefulConfiguration(), context);
        // When
        List<CandidateSteps> steps = factory.createCandidateSteps();
        // Then
        assertFooStepsFound(steps);
    }

    @Test
    public void annotationStepsCanBeCreated() throws Exception {

        // Given
        ApplicationContext context = createApplicationContext(AnnotationStepsConfiguration.class.getName());
        SpringStepsFactory factory = new SpringStepsFactory(new MostUsefulConfiguration(), context);
        // When
        List<CandidateSteps> steps = factory.createCandidateSteps();
        // Then
        assertFooStepsFound(steps);
    }

    @Test
    public void stepsWithDependenciesCanBeCreated() {
        // Given
        ApplicationContext context = createApplicationContext("org/jbehave/core/steps/spring/steps-with-dependency.xml");
        // When
        SpringStepsFactory factory = new SpringStepsFactory(new MostUsefulConfiguration(), context);
        List<CandidateSteps> steps = factory.createCandidateSteps();
        // Then
        assertFooStepsFound(steps);
        assertEquals(42, (int) ((FooStepsWithDependency) firstStepsInstance(steps)).integer);
    }


    @Test
    public void annotationStepsWithDependenciesCanBeCreated() {
        // Given
        ApplicationContext context = createApplicationContext(AnnotationStepsWithDependencyConfiguration.class.getName());
        // When
        SpringStepsFactory factory = new SpringStepsFactory(new MostUsefulConfiguration(), context);
        List<CandidateSteps> steps = factory.createCandidateSteps();
        // Then
        assertFooStepsFound(steps);
        assertEquals(42, (int) ((FooStepsWithDependency) firstStepsInstance(steps)).integer);
    }

    private void assertFooStepsFound(List<CandidateSteps> steps) {

        assertEquals(1, steps.size());
        assertThat(firstStepsInstance(steps), instanceOf(FooSteps.class));
    }

    private Object firstStepsInstance(List<CandidateSteps> steps) {

        return ((Steps) steps.get(0)).instance();
    }

    @Test(expected = BeanDefinitionStoreException.class)
    public void stepsWithMissingDependenciesCannotBeCreated() {
        // Given
        ApplicationContext context = createApplicationContext("org/jbehave/core/steps/spring/steps-with-missing-depedency.xml");
        SpringStepsFactory factory = new SpringStepsFactory(new MostUsefulConfiguration(), context);
        // When
        factory.createCandidateSteps();
        // Then ... expected exception is thrown
    }


    @Test
    public void beansWithUndefinedTypeOrCannotBeCreatedWillBeIgnored() {
        // Given
        ApplicationContext context = mock(ApplicationContext.class);
        SpringStepsFactory factory = new SpringStepsFactory(new MostUsefulConfiguration(), context);
        // When
        when(context.getBeanDefinitionNames()).thenReturn(new String[]{"fooSteps", "undefined", "blowUp"});
        doAnswer(new Answer<Class<FooSteps>>() {

            public Class<FooSteps> answer(InvocationOnMock invocation) throws Throwable {

                return FooSteps.class;
            }
        }).when(context).getType("fooSteps");
        when(context.getType("undefined")).thenReturn(null);
        doAnswer(new Answer<Class<BlowUp>>() {

            public Class<BlowUp> answer(InvocationOnMock invocation) throws Throwable {

                return BlowUp.class;
            }
        }).when(context).getType("blowUp");
        when(context.getBean("fooSteps")).thenReturn(new FooSteps());
        when(context.getBean("blowUp")).thenThrow(new RuntimeException("Bum!"));
        List<CandidateSteps> candidateSteps = factory.createCandidateSteps();
        // Then
        assertThat(candidateSteps.size(), equalTo(1));
        assertThat(firstStepsInstance(candidateSteps), instanceOf(FooSteps.class));
        verify(context, never()).getBean("undefined");
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

    public static class BlowUp {

    }
}
