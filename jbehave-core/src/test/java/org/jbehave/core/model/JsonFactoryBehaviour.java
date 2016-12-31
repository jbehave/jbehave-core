package org.jbehave.core.model;

import java.util.List;

import com.google.gson.Gson;

import org.jbehave.core.io.ResourceLoader;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonFactoryBehaviour {

    private String jsonAsString = "{\"string\":\"String1\",\"integer\":2,\"stringList\":[\"String2\",\"String3\"],"
            + "\"integerList\":[3,4]}";

    @Test
    public void shouldCreateJsonFromStringJsonInput() {
        // Given
        JsonFactory factory = new JsonFactory();

        // When
        MyJsonDto json = (MyJsonDto) factory.createJson(jsonAsString, MyJsonDto.class);

        // Then
        assertThat(new Gson().toJson(json), equalTo(jsonAsString));
    }

    @Test
    public void shouldCreateJsonFromResourceInput() {
        // Given
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        JsonFactory factory = new JsonFactory(resourceLoader);

        // When
        String resourcePath = "/path/to/json";
        when(resourceLoader.loadResourceAsText(resourcePath)).thenReturn(jsonAsString);
        MyJsonDto json = (MyJsonDto) factory.createJson(resourcePath, MyJsonDto.class);

        // Then
        assertThat(new Gson().toJson(json), equalTo(jsonAsString));
    }

    public static class MyJsonDto {

        private String string;
        private Integer integer;
        private List<String> stringList;
        private List<Integer> integerList;

    }

}
