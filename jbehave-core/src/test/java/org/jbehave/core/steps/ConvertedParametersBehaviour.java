package org.jbehave.core.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.Parameter;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.ConvertedParameters.ParametersNotMappableToType;
import org.junit.jupiter.api.Test;

class ConvertedParametersBehaviour {

    private final ParameterConverters converters = new ParameterConverters(new LoadFromClasspath(),
            new TableTransformers());
    private final Map<String, String> map = Collections.singletonMap("one", "11");
    private final Parameters parameters = new ConvertedParameters(map, converters);

    private List<String> stringList;
    private List<Integer> integerList;

    @Test
    void shouldReturnParameterValueConvertedToGivenType() throws Exception {
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
    void shouldIgnoreDefaultValueWhenConvertingAParameterThatIsFound() throws Exception {
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
    void shouldReturnDefaultValueWhenConvertingAParameterNotFound() throws Exception {
        assertThat(parameters.values().containsKey("XX"), is(false));
        assertThat(parameters.valueAs("XX", String.class, "3"), is("3"));
        assertThat(parameters.values().containsKey("XXX"), is(false));
        assertThat(parameters.valueAs("XXX", Integer.class, 3), is(3));
        assertThat(parameters.valueAs("XX",
                ConvertedParametersBehaviour.class.getDeclaredField("stringList").getGenericType(),
                Collections.singletonList("3")),
                is(Collections.singletonList("3")));
        assertThat(parameters.valueAs("XXX",
                ConvertedParametersBehaviour.class.getDeclaredField("integerList").getGenericType(),
                Collections.singletonList(3)),
                is(Collections.singletonList(3)));
    }

    @Test
    void shouldReturnValuesAsMap() {
        assertThat(parameters.values(), is(map));
    }

    @Test
    void shouldConvertParametersToType() {
        Parameters parameters = new ConvertedParameters(Collections.singletonMap("identifier", "id-1"),
                converters);

        assertThat(parameters.mapTo(Identifier.class).getIdentifier(), is("id-1"));
    }

    @Test
    void shouldConvertParametersToTypeWithFieldMapping() {
        Map<String, String> row = new HashMap<>();
        row.put("years", "53");
        row.put("firstName", "Boba");
        row.put("l_name", "Fett");
        row.put("identifier", "boba-fett-clone");

        Parameters parameters = new ConvertedParameters(row, converters);

        Map<String, String> mapping = new HashMap<>();
        mapping.put("years", "age");
        Person person = parameters.mapTo(Person.class, mapping);

        assertThat(person.getIdentifier(), is("boba-fett-clone"));
        assertThat(person.getAge(), is(53));
        assertThat(person.getFirstName(), is("Boba"));
        assertThat(person.getLastName(), is("Fett"));
    }

    @Test
    void shouldThrowExceptionOnUnknownFieldWhileConversion() {
        Parameters parameters = new ConvertedParameters(Collections.singletonMap("unknown_field", ""),
                converters);

        ParametersNotMappableToType thrown = assertThrows(ParametersNotMappableToType.class,
                () -> parameters.mapTo(Identifier.class));
        assertThat(thrown.getMessage(), is("Unable to map [unknown_field] field(s) for type class "
                + "org.jbehave.core.steps.ConvertedParametersBehaviour$Identifier"));
    }

    public static class Identifier {

        private String identifier;

        public String getIdentifier() {
            return identifier;
        }

    }

    public static final class Person extends Identifier {

        private int age;
        private String firstName;
        @Parameter(name = "l_name")
        private String lastName;

        public int getAge() {
            return age;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

    }
}
