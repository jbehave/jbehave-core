package org.jbehave.core.steps;

import java.util.HashMap;
import java.util.Map;

import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.TableTransformers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;

public class ChainedRowBehaviour {

    @Test
    public void shouldChainRowValuesWithoutOverwritingAlreadyExisting() throws Exception {
        Map<String,String> map1 = new HashMap<String, String>();
        map1.put("one", "11");
        Map<String,String> map2 = new HashMap<String, String>();
        map2.put("one", "21");
        map2.put("two", "22");

        ParameterConverters parameterConverters = new ParameterConverters(new LoadFromClasspath(),
                new TableTransformers());
        Map<String,String> chainedValues = new ChainedRow(new ConvertedParameters(map1, parameterConverters),
                new ConvertedParameters(map2, parameterConverters)).values();
        assertThat(chainedValues.get("one"), equalTo("11"));
        assertThat(chainedValues.get("two"), equalTo("22"));
    }

}
