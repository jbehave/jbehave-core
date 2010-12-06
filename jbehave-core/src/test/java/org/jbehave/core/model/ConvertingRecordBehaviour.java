package org.jbehave.core.model;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConvertingRecordBehaviour {

    private Map<String, String> map;

    @Mock
    private ValueConverter converters;

    private ConvertingRecord row;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        map = new HashMap<String, String>();
        map.put("one", "11");

        row = new ConvertingRecord(new MapRecord(map), converters);
    }

    @Test
    public void shouldReturnValues() throws Exception {
        String result = row.value("one");

        assertThat(result, is("11"));
    }

    @Test
    public void shouldReturnDefaultValues() throws Exception {
        String result = row.value("XX", "3");

        assertThat(result, is("3"));
    }

    @Test
    public void shouldConvertValues() throws Exception {
        when(converters.convert("11", Integer.class)).thenReturn(Integer.valueOf(11));

        Integer result = row.valueAs("one", Integer.class);

        assertThat(result, is(Integer.valueOf(11)));
        verify(converters).convert("11", Integer.class);
    }

    @Test
    public void shouldIgnoreDefaultValueWhenConvertingValues() throws Exception {
        when(converters.convert("11", Integer.class)).thenReturn(Integer.valueOf(11));

        Integer result = row.valueAs("one", Integer.class, Integer.valueOf(3));

        assertThat(result, is(Integer.valueOf(11)));
        verify(converters).convert("11", Integer.class);
    }

    @Test
    public void shouldReturnDefaultValueWhenConvertingValues() throws Exception {
        Integer result = row.valueAs("XXX", Integer.class, Integer.valueOf(3));

        assertThat(result, is(Integer.valueOf(3)));
    }

    @Test
    public void shouldIgnoreConvertedDefaultValueWhenConvertingValues() throws Exception {
        when(converters.convert("11", Integer.class)).thenReturn(Integer.valueOf(11));

        Integer result = row.valueAs("one", Integer.class, "3");

        assertThat(result, is(Integer.valueOf(11)));
        verify(converters).convert("11", Integer.class);
    }

    @Test
    public void shouldReturnConvertedDefaultValueWhenConvertingValues() throws Exception {
        when(converters.convert("3", Integer.class)).thenReturn(Integer.valueOf(3));

        Integer result = row.valueAs("XXX", Integer.class, "3");

        assertThat(result, is(Integer.valueOf(3)));
        verify(converters).convert("3", Integer.class);
    }
}
