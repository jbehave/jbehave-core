package org.jbehave.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.net.MalformedURLException;
import java.util.Arrays;

import org.hamcrest.Matchers;
import org.jbehave.core.StoryClassLoader.StoryClassNotFoundException;
import org.jbehave.core.StoryClassLoader.StoryNotInstantiatedException;
import org.junit.Test;

public class StoryClassLoaderBehaviour {

    @Test
    public void shouldInstantiateNewStoryWithDefaultConstructor() throws MalformedURLException {
        StoryClassLoader classLoader = new StoryClassLoader(Arrays.<String>asList());
        assertStoryIsInstantiated(classLoader, MyStory.class.getName());
    }

    @Test
    public void shouldInstantiateNewStoryWithClassLoaderParameter() throws MalformedURLException {
        StoryClassLoader classLoader = new StoryClassLoader(Arrays.<String>asList());
        assertStoryIsInstantiated(classLoader, MyStory.class.getName(), ClassLoader.class);
    }

    @Test
    public void shouldIgnoreNullClasspathElements() throws MalformedURLException {
        StoryClassLoader classLoader = new StoryClassLoader(null);
        assertStoryIsInstantiated(classLoader, MyStory.class.getName(), RunnableStory.class);
    }

    @Test
    public void shouldIgnoreParametersIfNotClassLoaders() throws MalformedURLException {
        StoryClassLoader classLoader = new StoryClassLoader(Arrays.<String>asList());
        String storyClassName = MyStory.class.getName();
        assertStoryIsInstantiated(classLoader, storyClassName, RunnableStory.class);
    }

    private void assertStoryIsInstantiated(StoryClassLoader classLoader, String storyClassName, Class<?>... parameterTypes) {
        RunnableStory story = classLoader.newStory(storyClassName, parameterTypes);
        assertThat(story, not(nullValue()));
        assertThat(storyClassName, equalTo(story.getClass().getName()));
        assertThat(classLoader, is(sameInstance(Thread.currentThread().getContextClassLoader())));
    }

    @Test
    public void shouldProvideShortJarPathUrlContentAsString() throws MalformedURLException {
        StoryClassLoader classLoader = new StoryClassLoader(Arrays.<String>asList("/path/to/one.jar", "/target/classes"));
        assertThat(classLoader.toString(), Matchers.containsString("urls="+Arrays.<String>asList("one.jar", "/target/classes")));
    }

    @Test(expected=StoryClassNotFoundException.class)
    public void shouldNotInstantiateNewStoryWithWrongClassName() throws MalformedURLException {
        StoryClassLoader classLoader = new StoryClassLoader(Arrays.<String>asList());
        String storyClassName = "InexistentClass";
        classLoader.newStory(storyClassName);
    }

    @Test(expected=StoryNotInstantiatedException.class)
    public void shouldNotInstantiateNewStoryWithMissingConstructor() throws MalformedURLException {
        StoryClassLoader classLoader = new StoryClassLoader(Arrays.<String>asList());
        classLoader.newStory(StoryWithNoClassLoaderInjection.class.getName(), ClassLoader.class);
    }

    public static class MyStory extends JUnitStory {

		public MyStory() {
		}

		public MyStory(ClassLoader classLoader) {
		}
		
    }
    
    public static class StoryWithNoClassLoaderInjection extends JUnitStory {
		
    }

}