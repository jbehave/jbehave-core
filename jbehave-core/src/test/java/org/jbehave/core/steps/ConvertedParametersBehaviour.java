package org.jbehave.core.steps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.ConvertedParameters;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.is;

public class ConvertedParametersBehaviour {

    private Map<String, String> map = Collections.singletonMap("one", "11");
    private Parameters parameters = new ConvertedParameters(map,
            new ParameterConverters(new LoadFromClasspath(), new TableTransformers()));
    private List<String> stringList;
    private List<Integer> integerList;

    @Test
    public void shouldReturnParameterValueConvertedToGivenType() throws Exception {
        assertThat(parameters.values().containsKey("one"), is(true));
        assertThat(parameters.<String>valueAs("one", String.class), is("11"));
        assertThat(parameters.<Integer>valueAs("one", Integer.class), is(11));
        assertThat(parameters.<List<String>>valueAs("one",
                ConvertedParametersBehaviour.class.getDeclaredField("stringList").getGenericType()),
                is(Collections.singletonList("11")));
        assertThat(parameters.<List<Integer>>valueAs("one",
                ConvertedParametersBehaviour.class.getDeclaredField("integerList").getGenericType()),
                is(Collections.singletonList(11)));
    }

    @Test
    public void shouldIgnoreDefaultValueWhenConvertingAParameterThatIsFound() throws Exception {
        assertThat(parameters.values().containsKey("one"), is(true));
        assertThat(parameters.valueAs("one", Integer.class, 3), is(11));
        assertThat(parameters.valueAs("one", String.class, "3"), is("11"));
        assertThat(parameters.<List<String>>valueAs("one",
                ConvertedParametersBehaviour.class.getDeclaredField("stringList").getGenericType()),
                is(Collections.singletonList("11")));
        assertThat(parameters.<List<Integer>>valueAs("one",
                ConvertedParametersBehaviour.class.getDeclaredField("integerList").getGenericType()),
                is(Collections.singletonList(11)));
    }

    @Test
    public void shouldReturnDefaultValueWhenConvertingAParameterNotFound() throws Exception {
        assertThat(parameters.values().containsKey("XX"), is(false));
        assertThat(parameters.valueAs("XX", String.class, "3"), is("3"));
        assertThat(parameters.values().containsKey("XXX"), is(false));
        assertThat(parameters.valueAs("XXX", Integer.class, 3), is(3));
        assertThat(parameters.<List<String>>valueAs("XX",
                ConvertedParametersBehaviour.class.getDeclaredField("stringList").getGenericType(),
                Collections.singletonList("3")),
                is(Collections.singletonList("3")));
        assertThat(parameters.<List<Integer>>valueAs("XXX",
                ConvertedParametersBehaviour.class.getDeclaredField("integerList").getGenericType(),
                Collections.singletonList(3)),
                is(Collections.singletonList(3)));
    }

    @Test
    public void shouldReturnValuesAsMap() throws Exception {
        assertThat(parameters.values(), is(map));
    }

}
