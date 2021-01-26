package org.jbehave.core.steps.scala;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.configuration.scala.ScalaContext;
import org.jbehave.core.configuration.scala.ScalaContext.ScalaInstanceNotFound;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScalaStepsFactoryBehaviour {

    @Test
    public void shouldCreateStepsInstancesFromScalaWhenAnnotated() {
        ScalaContext context = new ScalaContext("AnnotatedSteps", "NonAnnotatedSteps");
        ScalaStepsFactory factory = new ScalaStepsFactory(new MostUsefulConfiguration(), context);
        List<Class<?>> types = factory.stepsTypes();
        MatcherAssert.assertThat(types.size(), Matchers.equalTo(1));
        assertThat(types.get(0).getSimpleName(), equalTo("AnnotatedSteps"));
        Object object = factory.createInstanceOfType(context.newInstance("AnnotatedSteps").getClass());
        assertThat(object.getClass().getName(), equalTo("AnnotatedSteps"));
    }

    @Test
    @Disabled("assertThrows does not work")
    public void shouldNotCreateStepsInstancesFromScalaWhenContextInvalid() {
        MostUsefulConfiguration configuration = new MostUsefulConfiguration();
        ScalaContext scalaContext = new ScalaContext("InexistentSteps");
        assertThrows(ScalaInstanceNotFound.class, () -> new ScalaStepsFactory(configuration, scalaContext));
    }

    @Test
    public void shouldNotCreateStepsInstancesFromScalaWhenNotFound() {
        ScalaStepsFactory factory = new ScalaStepsFactory(new MostUsefulConfiguration(), new ScalaContext());
        assertThrows(ScalaInstanceNotFound.class, () -> factory.createInstanceOfType(NonScalaType.class));
    }

    public static class NonScalaType {
        
    }
}
