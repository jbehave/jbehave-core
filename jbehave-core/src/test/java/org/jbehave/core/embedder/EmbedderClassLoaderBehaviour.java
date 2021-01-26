package org.jbehave.core.embedder;

import org.jbehave.core.Embeddable;
import org.jbehave.core.embedder.EmbedderClassLoader.InstantiationFailed;
import org.jbehave.core.junit.JUnitStory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EmbedderClassLoaderBehaviour {

    @Test
    public void shouldInstantiateNewEmbedder() {
        EmbedderClassLoader classLoader = new EmbedderClassLoader(Arrays.<String> asList());
        assertThatIsInstantiated(classLoader, MyEmbedder.class.getName(), MyEmbedder.class);
    }

    @Test
    public void shouldInstantiateNewStory() {
        EmbedderClassLoader classLoader = new EmbedderClassLoader(Arrays.<String> asList());
        assertThatIsInstantiated(classLoader, MyStory.class.getName(), MyStory.class);
    }

    @Test
    public void shouldIdentifyIfStoryIsAbstract() {
        EmbedderClassLoader classLoader = new EmbedderClassLoader(Arrays.<String> asList());
        assertThat(classLoader.isAbstract(MyStory.class.getName()), is(false));
        assertThat(classLoader.isAbstract(MyAbstractStory.class.getName()), is(true));
        assertThat(classLoader.isAbstract("InexistentClass"), is(false));        
    }

    @Test
    public void shouldIgnoreIfListOfClasspathElementsIsNull() {
        List<String> elements = null;
        EmbedderClassLoader classLoader = new EmbedderClassLoader(elements);
        assertThatIsInstantiated(classLoader, MyStory.class.getName(), MyStory.class);
    }

    @Test
    @Disabled("assertThrows does not work")
    public void shouldNotIgnoreAnIndividualClasspathElementThatIsNull(){
        List<String> elements = asList("target/classes", null);
        assertThrows(EmbedderClassLoader.InvalidClasspathElement.class, () -> new EmbedderClassLoader(elements));
    }

    private <T> void assertThatIsInstantiated(EmbedderClassLoader classLoader, String className, Class<T> type) {
        T t = classLoader.newInstance(type, className);
        assertThat(t, not(nullValue()));
        assertThat(className, equalTo(t.getClass().getName()));
        assertThat(classLoader, is(sameInstance(Thread.currentThread().getContextClassLoader())));
    }

    @Test
    public void shouldProvideShortJarPathUrlContentAsString() throws MalformedURLException {
        EmbedderClassLoader classLoader = new EmbedderClassLoader(Arrays.asList("/path/to/one.jar",
                "/target/classes"));
        List<String> expectedPaths = classLoader.asShortPaths(new File("one.jar").toURI().toURL(), new File("/target/classes").toURI().toURL());
        String expected = expectedPaths.stream()
                .collect(Collectors.joining(",", "{", "}"));
        assertThat(classLoader.toString(),
                containsString("urls=" + expected));
    }

    @Test
    @Disabled("assertThrows does not work")
    public void shouldNotInstantiateClassWithInexistentName() {
        EmbedderClassLoader classLoader = new EmbedderClassLoader(Arrays.<String> asList());
        assertThrows(InstantiationFailed.class, () -> classLoader.newInstance(Embeddable.class, "UnexistentClass"));
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
