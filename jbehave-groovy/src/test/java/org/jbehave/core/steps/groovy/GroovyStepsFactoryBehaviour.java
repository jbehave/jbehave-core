package org.jbehave.core.steps.groovy;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.configuration.groovy.GroovyContext;
import org.jbehave.core.configuration.groovy.GroovyContext.GroovyClassInstantiationFailed;
import org.jbehave.core.configuration.groovy.GroovyResourceFinder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GroovyStepsFactoryBehaviour {

    @Test
    public void shouldCreateStepsInstancesFromGroovyWhenAnnotated() {
        GroovyResourceFinder resourceFinder = new GroovyResourceFinder(codeLocationFromClass(this.getClass()),
                "**/steps/groovy/*.groovy", "**/invalidSteps.groovy");
        GroovyStepsFactory factory = new GroovyStepsFactory(new MostUsefulConfiguration(), new GroovyContext(resourceFinder));
        List<Class<?>> types = factory.stepsTypes();
        MatcherAssert.assertThat(types.size(), Matchers.equalTo(1));
        assertThat(types.get(0).getSimpleName(), equalTo("AnnotatedSteps"));
    }

    @Test
    public void shouldNotCreateStepsInstancesFromGroovyWhenResourceInvalid() {
        assertThrows(GroovyClassInstantiationFailed.class,
                () -> new GroovyContext(asList("/org/jbehave/core/steps/groovy/invalidSteps.groovy")));
    }
}
