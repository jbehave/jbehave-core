package org.jbehave.core.model;

import java.util.List;

import com.google.gson.Gson;

import org.jbehave.core.annotations.AsJson;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class JsonBehaviour {

    @Test
    public void shouldMapJsonToType() throws Exception {
        // Given
        JsonFactory factory = new JsonFactory();

        // When
        String jsonAsString = "{\"string\":\"11\",\"integer\":22,\"stringList\":[\"1\",\"1\"],\"integerList\":[2,2]}";
        MyJsonDto jsonDto = (MyJsonDto) factory.createJson(jsonAsString, MyJsonDto.class);

        // Then
        assertThat(jsonDto.string, equalTo("11"));
        assertThat(jsonDto.integer, equalTo(22));
        assertThat(jsonDto.stringList, equalTo(asList("1", "1")));
        assertThat(jsonDto.integerList, equalTo(asList(2, 2)));
    }

    @Test
    public void shouldMapListOfJsonsToType() throws Exception {
        // Given
        JsonFactory factory = new JsonFactory();

        // When
        String jsonAsString = "{\"string\":\"11\",\"integer\":22,\"stringList\":[\"1\",\"1\"],\"integerList\":[2,2]}";
        String listOfJsonsAsString = String.format("[%s, %s]", jsonAsString, jsonAsString);
        MyJsonDto[] jsonList = (MyJsonDto[]) factory.createJson(listOfJsonsAsString, MyJsonDto[].class);

        // Then
        for (MyJsonDto jsonDto : jsonList) {
            assertThat(jsonDto.string, equalTo("11"));
            assertThat(jsonDto.integer, equalTo(22));
            assertThat(jsonDto.stringList, equalTo(asList("1", "1")));
            assertThat(jsonDto.integerList, equalTo(asList(2, 2)));
        }
    }

    @Test
    public void shouldPutNullsIfValuesOfObjectNotFoundInJson() throws Exception {
        // Given
        ParameterConverters parameterConverters = new ParameterConverters();
        JsonFactory factory = new JsonFactory(parameterConverters);

        // When
        String jsonAsString = "{\"integer\":22,\"stringList\":[\"1\",\"1\"]}";
        MyJsonDto jsonDto = (MyJsonDto) factory.createJson(jsonAsString, MyJsonDto.class);

        // Then
        assertThat(jsonDto.string, equalTo(null));
        assertThat(jsonDto.integer, equalTo(22));
        assertThat(jsonDto.stringList, equalTo(asList("1", "1")));
        assertThat(jsonDto.integerList, equalTo(null));
    }

    @Test
    public void shouldPutAllNullsIfNoJsonArgumentsMatched() {
        // Given
        JsonFactory factory = new JsonFactory();

        // When
        String jsonAsString = "{\"string2\":\"11\",\"integer2\":22,\"stringList2\":[\"1\",\"1\"],\"integerList2\":[2,2]}";
        MyJsonDto jsonDto = (MyJsonDto) factory.createJson(jsonAsString, MyJsonDto.class);

        // Then
        assertThat(jsonDto.string, equalTo(null));
        assertThat(jsonDto.integer, equalTo(null));
        assertThat(jsonDto.stringList, equalTo(null));
        assertThat(jsonDto.integerList, equalTo(null));
    }

    @Test
    public void shouldNotBeEqualJsonWithWhitespaces() {
        // Given
        JsonFactory factory = new JsonFactory();

        // When
        String jsonAsString = "{ \"string\" : \"11\" , \"integer\" : 22 , \"stringList\" : [ \"1\" , \"1\" ] , "
                + "\"integerList\" : [ 2 , 2 ] }";
        MyJsonDto jsonDto = (MyJsonDto) factory.createJson(jsonAsString, MyJsonDto.class);

        // Then
        assertThat(new Gson().toJson(jsonDto), not(equalTo(jsonAsString)));
    }

    @Test
    public void shouldBeEqualDtosConvertedFromJsonWithWhitespaces() {
        // Given
        JsonFactory factory = new JsonFactory();

        // When
        String jsonAsString = "{ \"string\" : \"11\" , \"integer\" : 22 , \"stringList\" : [ \"1\" , \"1\" ] , "
                + "\"integerList\" : [ 2 , 2 ] }";
        MyJsonDto convertedJsonDto = (MyJsonDto) factory.createJson(jsonAsString, MyJsonDto.class);
        MyJsonDto createdJsonDto = new MyJsonDto("11", 22, asList("1", "1"), asList(2, 2));

        // Then
        assertThat(createdJsonDto.getString(), equalTo(convertedJsonDto.getString()));
        assertThat(createdJsonDto.getInteger(), equalTo(convertedJsonDto.getInteger()));
        assertThat(createdJsonDto.getStringList(), equalTo(convertedJsonDto.getStringList()));
        assertThat(createdJsonDto.getIntegerList(), equalTo(convertedJsonDto.getIntegerList()));
    }

    @AsJson
    public static class MyJsonDto {

        private String string;
        private Integer integer;
        private List<String> stringList;
        private List<Integer> integerList;

        public String getString() {
            return string;
        }

        public Integer getInteger() {
            return integer;
        }

        public List<String> getStringList() {
            return stringList;
        }

        public List<Integer> getIntegerList() {
            return integerList;
        }

        public MyJsonDto(String string, Integer integer, List<String> stringList, List<Integer> integerList) {
            this.string = string;
            this.integer = integer;
            this.stringList = stringList;
            this.integerList = integerList;
        }
    }

}
