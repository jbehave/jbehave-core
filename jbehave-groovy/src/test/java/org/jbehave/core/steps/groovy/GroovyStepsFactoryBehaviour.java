package org.jbehave.core.steps.groovy;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;

import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.steps.groovy.GroovyStepsFactory.GroovyClassInstantiationFailed;
import org.junit.Test;

public class GroovyStepsFactoryBehaviour {

    @Test
    public void shouldCreateStepsInstancesFromGroovyWhenAnnotated() {
        List<String> resources = new StoryFinder().findPaths(codeLocationFromClass(this.getClass()).getFile(),
                asList("**/groovy/*.groovy"), asList("**/groovy/invalid*.groovy"));
        GroovyStepsFactory factory = new GroovyStepsFactory(new MostUsefulConfiguration(), resources);
        List<Object> instances = factory.stepsInstances();
        MatcherAssert.assertThat(instances.size(), Matchers.equalTo(1));
        Object object = instances.get(0);
        assertThat(object.getClass().getSimpleName(), equalTo("AnnotatedSteps"));
    }

    @Test(expected = GroovyClassInstantiationFailed.class)
    public void shouldNotCreateStepsInstancesFromGroovyWhenResourceInvalid() {
        GroovyStepsFactory factory = new GroovyStepsFactory(new MostUsefulConfiguration(),
                asList("/org/jbehave/core/steps/groovy/invalidSteps.groovy"));
        factory.stepsInstances();
    }
}
