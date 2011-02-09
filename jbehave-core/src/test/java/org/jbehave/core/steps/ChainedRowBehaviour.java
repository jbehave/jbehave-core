package org.jbehave.core.steps;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;

public class ChainedRowBehaviour {

    @Test
    public void shouldNotOverwriteParametersAlreadyExisting() throws Exception {
        Map<String,String> map1 = new HashMap<String, String>();
        map1.put("one", "11");
        Map<String,String> map2 = new HashMap<String, String>();
        map2.put("one", "21");
        map2.put("two", "22");

        Row row = new ChainedRow(new ConvertedParameters(map1, new ParameterConverters()),
                new ConvertedParameters(map2, new ParameterConverters()));
        Map<String,String> values = row.values();
        assertThat(values.get("one"), equalTo("11"));
        assertThat(values.get("two"), equalTo("22"));
    }

    
}
