package org.jbehave.core.steps.scala;

import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.configuration.scala.ScalaContext;
import org.jbehave.core.configuration.scala.ScalaContext.ScalaInstanceNotFound;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;

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

    @Test(expected = ScalaInstanceNotFound.class)
    public void shouldNotCreateStepsInstancesFromScalaWhenContextInvalid() {
        new ScalaStepsFactory(new MostUsefulConfiguration(), new ScalaContext("InexistentSteps"));
    }

    @Test(expected = ScalaInstanceNotFound.class)
    public void shouldNotCreateStepsInstancesFromScalaWhenNotFound() {
        ScalaStepsFactory factory = new ScalaStepsFactory(new MostUsefulConfiguration(), new ScalaContext());
        factory.createInstanceOfType(NonScalaType.class);
    }

    public static class NonScalaType {
        
    }
}
