package org.jbehave.core.embedder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.net.MalformedURLException;
import java.util.Arrays;

import org.jbehave.core.JUnitStory;
import org.jbehave.core.RunnableStory;
import org.jbehave.core.embedder.EmbedderClassLoader.InstantiationFailed;
import org.junit.Test;

public class EmbedderClassLoaderBehaviour {

    @Test
    public void shouldInstantiateNewEmbedderWithDefaultConstructor() throws MalformedURLException {
        EmbedderClassLoader classLoader = new EmbedderClassLoader(Arrays.<String> asList());
        assertThatIsInstantiated(classLoader, MyEmbedder.class.getName(), MyEmbedder.class);
    }

    @Test
    public void shouldInstantiateNewStoryWithDefaultConstructor() throws MalformedURLException {
        EmbedderClassLoader classLoader = new EmbedderClassLoader(Arrays.<String> asList());
        assertThatIsInstantiated(classLoader, MyStory.class.getName(), MyStory.class);
    }

    @Test
    public void shouldIdentifyIfStoryIsAbstract() throws MalformedURLException {
        EmbedderClassLoader classLoader = new EmbedderClassLoader(Arrays.<String> asList());
        assertThat(classLoader.isAbstract(MyStory.class.getName()), is(false));
        assertThat(classLoader.isAbstract(MyAbstractStory.class.getName()), is(true));
    }

    @Test
    public void shouldIgnoreNullClasspathElements() throws MalformedURLException {
        EmbedderClassLoader classLoader = new EmbedderClassLoader(null);
        assertThatIsInstantiated(classLoader, MyStory.class.getName(), MyStory.class);
    }

    private <T> void assertThatIsInstantiated(EmbedderClassLoader classLoader, String className, Class<T> type) {
        T t = classLoader.newInstance(type, className);
        assertThat(t, not(nullValue()));
        assertThat(className, equalTo(t.getClass().getName()));
        assertThat(classLoader, is(sameInstance(Thread.currentThread().getContextClassLoader())));
    }

    @Test
    public void shouldProvideShortJarPathUrlContentAsString() throws MalformedURLException {
        EmbedderClassLoader classLoader = new EmbedderClassLoader(Arrays.<String> asList("/path/to/one.jar",
                "/target/classes"));
        assertThat(classLoader.toString(),
                containsString("urls=" + Arrays.<String> asList("one.jar", "/target/classes")));
    }

    @Test(expected = InstantiationFailed.class)
    public void shouldNotInstantiateClassWithInexistentName() throws MalformedURLException {
        EmbedderClassLoader classLoader = new EmbedderClassLoader(Arrays.<String> asList());
        classLoader.newInstance(RunnableStory.class, "InexistentClass");
    }

    public static class MyEmbedder extends Embedder {

        public MyEmbedder() {
        }

    }

    public static class MyStory extends JUnitStory {

        public MyStory() {
        }

    }

    public static abstract class MyAbstractStory extends JUnitStory {

    }

}