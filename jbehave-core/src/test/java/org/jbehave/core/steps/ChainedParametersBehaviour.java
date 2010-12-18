package org.jbehave.core.steps;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ChainedParametersBehaviour {

    @Test
    public void shouldReturnParameterValueConvertedToGivenType() throws Exception {
        Map<String,String> map1 = new HashMap<String, String>();
        map1.put("one", "11");
        Map<String,String> map2 = new HashMap<String, String>();
        map2.put("two", "22");

        Parameters parameters = new ChainedParameters(new ConvertedParameters(map1, new ParameterConverters()),
                new ConvertedParameters(map2, new ParameterConverters()));
        assertThat(parameters.hasValue("one"), is(true));
        assertThat(parameters.valueAs("one", String.class), is("11"));
        assertThat(parameters.valueAs("one", Integer.class), is(11));
        assertThat(parameters.hasValue("two"), is(true));
        assertThat(parameters.valueAs("two", String.class), is("22"));
        assertThat(parameters.valueAs("two", Integer.class), is(22));
        assertThat(parameters.hasValue("three"), is(false));
        assertThat(parameters.valueAs("three", String.class, "33"), is("33"));
        assertThat(parameters.valueAs("three", Integer.class, 33), is(33));
    }

    @Test
    public void shouldNotOverwriteParametersAlreadyExisting() throws Exception {
        Map<String,String> map1 = new HashMap<String, String>();
        map1.put("one", "11");
        Map<String,String> map2 = new HashMap<String, String>();
        map2.put("one", "21");
        map2.put("two", "22");

        Parameters parameters = new ChainedParameters(new ConvertedParameters(map1, new ParameterConverters()),
                new ConvertedParameters(map2, new ParameterConverters()));
        Map<String,String> values = parameters.values();
        assertThat(values.get("one"), equalTo("11"));
        assertThat(values.get("two"), equalTo("22"));
    }

    
}
