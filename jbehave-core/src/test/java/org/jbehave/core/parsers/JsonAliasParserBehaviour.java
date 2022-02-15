package org.jbehave.core.parsers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.model.Alias;
import org.jbehave.core.model.AliasVariant;
import org.jbehave.core.parsers.JsonAliasParser.InvalidAliasType;
import org.jbehave.core.steps.StepType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JsonAliasParserBehaviour {

    private static final String RESOURCE_FOLDER = "org/jbehave/core/parsers/";
    private final ResourceLoader resourceLoader = new LoadFromClasspath(getClass());
    private final Keywords keywords = new Keywords();

    @Test
    void shouldDoNothingIfResourcesAreEmpty() {

        Keywords keywordsSpy = spy(keywords);
        AliasParser parser = new JsonAliasParser(keywordsSpy);

        Collection<Alias> aliases = parser.parse(Collections.emptySet());

        assertThat(aliases, is(empty()));
        verifyNoInteractions(keywordsSpy);

    }

    @Test
    void shouldParseAliasesFromResources() {

        AliasParser parser = new JsonAliasParser(keywords);

        Set<String> resources = Stream.of("aliases-main.json", "aliases-with-same-step.json")
                                      .map(RESOURCE_FOLDER::concat)
                                      .map(resourceLoader::loadResourceAsText)
                                      .collect(Collectors.toSet());

        Collection<Alias> aliases = parser.parse(resources);

        assertThat(aliases, hasSize(3));
        Iterator<Alias> aliasIterator = aliases.iterator();

        Alias ailas1 = aliasIterator.next();
        assertThat(ailas1.getStepIdentifier(), equalTo("I write hello world in JVM language"));
        assertThat(ailas1.getType(), equalTo(StepType.WHEN));

        List<AliasVariant> variants1 = ailas1.getVariants();
        assertThat(variants1, hasSize(3));
        assertThat(variants1.get(0).getValue(), equalTo("I write hello world in Groovy"));
        assertThat(variants1.get(1).getValue(), equalTo("I write hello world in Java"));
        assertThat(variants1.get(2).getValue(), equalTo("I write hello world in Scala"));

        Alias ailas2 = aliasIterator.next();
        assertThat(ailas2.getStepIdentifier(), equalTo("HotStop JVM"));
        assertThat(ailas2.getType(), equalTo(StepType.GIVEN));
        List<AliasVariant> variants2 = ailas2.getVariants();
        assertThat(variants2, hasSize(1));
        assertThat(variants2.get(0).getValue(), equalTo("system with HotStop JVM"));

        Alias ailas3 = aliasIterator.next();
        assertThat(ailas3.getStepIdentifier(), equalTo("java compiler"));
        assertThat(ailas3.getType(), equalTo(StepType.GIVEN));
        List<AliasVariant> variants3 = ailas3.getVariants();
        assertThat(variants3, hasSize(1));
        assertThat(variants3.get(0).getValue(), equalTo("javac"));

    }

    @Test
    void shouldFailIfAlasTypeDoesntCorrespondStepType() {

        AliasParser parser = new JsonAliasParser(keywords);

        String resource = resourceLoader.loadResourceAsText(RESOURCE_FOLDER + "aliases-wrong-step-type-in-alias.json");
        Set<String> resources = Collections.singleton(resource);

        InvalidAliasType thrown = assertThrows(InvalidAliasType.class, () -> parser.parse(resources));
        assertThat(thrown.getMessage(), equalTo("The alias 'Then I write hello world in Java' must be of type 'WHEN'"));
    }

    @Test
    void shouldFailIfTestIdentifierIsNotSet() {

        AliasParser parser = new JsonAliasParser(keywords);
        Set<String> resources = Collections.singleton("[{}]");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> parser.parse(resources));
        assertThat(thrown.getMessage(), equalTo("The 'name' property that identifies step must be set"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "[{\"name\" : \"step\", \"aliases\": null}]",
        "[{\"name\" : \"step\"}]",
    })
    void shouldFailIfAliasesAreNotDefinedOrEmpty(String resource) {
        AliasParser parser = new JsonAliasParser(keywords);
        Set<String> resources = Collections.singleton(resource);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> parser.parse(resources));
        assertThat(thrown.getMessage(), equalTo("The 'aliases' property must be set"));
    }

    @Test
    void shouldFailIfAliasNameIsNotSet() {
        AliasParser parser = new JsonAliasParser(keywords);
        Set<String> resources = Collections.singleton("[{\"name\" : \"step\", \"aliases\": [{}]}]");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> parser.parse(resources));
        assertThat(thrown.getMessage(), equalTo("The 'name' property of alias must be set"));
    }
}
