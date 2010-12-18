package org.jbehave.core.steps;

import java.util.HashMap;
import java.util.Map;

import org.jbehave.core.steps.ConvertedParameters;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.is;

public class ConvertedParametersBehaviour {

    private Map<String, String> map;

    private ConvertedParameters parameters;

    @Before
    public void setUp() throws Exception {

        map = new HashMap<String, String>();
        map.put("one", "11");

        parameters = new ConvertedParameters(map, new ParameterConverters());
    }

    @Test
    public void shouldReturnParameterValueConvertedToGivenType() throws Exception {
        assertThat(parameters.valueAs("one", String.class), is("11"));
        assertThat(parameters.valueAs("one", Integer.class), is(11));
    }

    @Test
    public void shouldIgnoreDefaultValueWhenConvertingAParameterThatIsFound() throws Exception {
        assertThat(parameters.valueAs("one", Integer.class, 3), is(11));
        assertThat(parameters.valueAs("one", String.class, "3"), is("11"));
    }

    @Test
    public void shouldReturnDefaultValueWhenConvertingAParameterNotFound() throws Exception {
        assertThat(parameters.valueAs("XX", String.class, "3"), is("3"));
        assertThat(parameters.valueAs("XXX", Integer.class, 3), is(3));
    }

    @Test
    public void shouldReturnValuesAsMap() throws Exception {
        assertThat(parameters.values(), is(map));
    }

}
