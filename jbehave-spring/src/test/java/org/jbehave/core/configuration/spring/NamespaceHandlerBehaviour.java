package org.jbehave.core.configuration.spring;

import org.hamcrest.Matchers;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.steps.spring.SpringApplicationContextFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;

public class NamespaceHandlerBehaviour {

    @Test
    @Ignore("FIXME: should return Embedder, not LoadFromClasspath")
    public void shouldBuildEmbedderFromNamespaceSchema() {
        ConfigurableApplicationContext context = new SpringApplicationContextFactory(
                "org/jbehave/core/configuration/spring/configuration-namespace.xml").createApplicationContext();
        Object bean = context.getBean("embedder");
        assertThat(bean, Matchers.notNullValue());
        assertThat(bean, Matchers.instanceOf(Embedder.class));
    }

}
