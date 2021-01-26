package org.jbehave.core.steps.scala;

import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.configuration.scala.ScalaContext;
import org.jbehave.core.configuration.scala.ScalaContext.ScalaInstanceNotFound;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScalaStepsFactoryBehaviour {

    @Test
    void shouldCreateStepsInstancesFromScalaWhenAnnotated() {
        ScalaContext context = new ScalaContext("AnnotatedSteps", "NonAnnotatedSteps");
        ScalaStepsFactory factory = new ScalaStepsFactory(new MostUsefulConfiguration(), context);
        List<Class<?>> types = factory.stepsTypes();
        assertThat(types.size(), equalTo(1));
        assertThat(types.get(0).getSimpleName(), equalTo("AnnotatedSteps"));
        Object object = factory.createInstanceOfType(context.newInstance("AnnotatedSteps").getClass());
        assertThat(object.getClass().getName(), equalTo("AnnotatedSteps"));
    }

    @Test
    void shouldNotCreateStepsInstancesFromScalaWhenContextInvalid() {
        assertThrows(ScalaInstanceNotFound.class, () -> new ScalaContext("UnexistentSteps"));
    }

    @Test
    void shouldNotCreateStepsInstancesFromScalaWhenNotFound() {
        ScalaStepsFactory factory = new ScalaStepsFactory(new MostUsefulConfiguration(), new ScalaContext());
        assertThrows(ScalaInstanceNotFound.class, () -> factory.createInstanceOfType(NonScalaType.class));
    }

    public static class NonScalaType {
        
    }
}
