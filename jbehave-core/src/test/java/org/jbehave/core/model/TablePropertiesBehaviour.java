package org.jbehave.core.model;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static java.util.Collections.singletonMap;
import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Valery Yatsynovich
 */
class TablePropertiesBehaviour {

    private TableProperties createTablePropertiesWithDefaultSeparators(String propertiesAsString) {
        return new TableProperties(propertiesAsString, "|", "|", "|--");
    }

    @Test
    void canGetCustomProperties() {
        TableProperties properties = createTablePropertiesWithDefaultSeparators(
                "ignorableSeparator=!--,headerSeparator=!,valueSeparator=!,"
                        + "commentSeparator=#,trim=false,metaByRow=true,transformer=CUSTOM_TRANSFORMER");
        assertThat(properties.getRowSeparator(), equalTo("\n"));
        assertThat(properties.getHeaderSeparator(), equalTo("!"));
        assertThat(properties.getValueSeparator(), equalTo("!"));
        assertThat(properties.getIgnorableSeparator(), equalTo("!--"));
        assertThat(properties.getCommentSeparator(), equalTo("#"));
        assertThat(properties.isTrim(), is(false));
        assertThat(properties.isMetaByRow(), is(true));
        assertThat(properties.getTransformer(), equalTo("CUSTOM_TRANSFORMER"));
    }

    @Test
    void canSetPropertiesWithBackwardSlash() {
        TableProperties properties = createTablePropertiesWithDefaultSeparators("custom=\\");
        assertThat(properties.getProperties().getProperty("custom"), equalTo("\\"));
    }

    @Test
    void canSetPropertiesWithSpecialCharsInValues() {
        TableProperties properties = createTablePropertiesWithDefaultSeparators("withSpecialChars=value;/=:*$\\");
        assertThat(properties.getProperties().getProperty("withSpecialChars"), equalTo("value;/=:*$\\"));
    }

    @Test
    void canSetPropertiesWithWhitespaceInValue() {
        TableProperties properties = createTablePropertiesWithDefaultSeparators("withWhitespace=a value");
        assertThat(properties.getProperties().getProperty("withWhitespace"), equalTo("a value"));
    }

    @Test
    void canSetPropertiesWithMixedCharsInValues() {
        TableProperties properties = createTablePropertiesWithDefaultSeparators("withMixedChars=value1;value2:*");
        assertThat(properties.getProperties().getProperty("withMixedChars"), equalTo("value1;value2:*"));
    }

    @Test
    void canSetPropertiesWithSpecialCharsInName() {
        TableProperties properties = createTablePropertiesWithDefaultSeparators("p.r:o*p$e|r;t#y=value");
        assertThat(properties.getProperties().getProperty("p.r:o*p$e|r;t#y"), equalTo("value"));
    }

    @Test
    void canSetPropertiesStartingWithSpecialCharsAndContainingBracketsInValue() {
        TableProperties properties = createTablePropertiesWithDefaultSeparators("placeholderKey=${placeholderValue}");
        assertThat(properties.getProperties().getProperty("placeholderKey"), equalTo("${placeholderValue}"));
    }

    @Test
    void canGetDefaultProperties() {
        TableProperties properties = new TableProperties(new Properties());
        assertThat(properties.getHeaderSeparator(), equalTo("|"));
        assertThat(properties.getValueSeparator(), equalTo("|"));
        assertThat(properties.getIgnorableSeparator(), equalTo("|--"));
        assertThat(properties.getCommentSeparator(), equalTo("#"));
        assertThat(properties.isTrim(), is(true));
        assertThat(properties.isMetaByRow(), is(false));
        assertThat(properties.getTransformer(), is(nullValue()));
    }

    @Test
    void canGetAllProperties() {
        Properties properties = new Properties();
        properties.setProperty("key", "value");
        TableProperties tableProperties = new TableProperties(properties);
        assertThat(tableProperties.getProperties().containsKey("key"), is(true));
    }

    @Test
    void canGetPropertiesWithNestedTransformersWithoutEscaping() {
        TableProperties properties = new TableProperties("transformer=CUSTOM_TRANSFORMER, " +
                "tables={transformer=CUSTOM_TRANSFORMER\\, parameter1=value1}");
        assertThat(properties.isTrim(), is(true));
        assertThat(properties.isMetaByRow(), is(false));
        assertThat(properties.getTransformer(), equalTo("CUSTOM_TRANSFORMER"));
        assertThat(properties.getProperties().getProperty("tables"),
                equalTo("{transformer=CUSTOM_TRANSFORMER, parameter1=value1}"));
    }

    @Test
    void canDecoratePropertyValuesToTrimOrKeepVerbatim() {
        TableProperties properties = new TableProperties("{key1|trim}= surroundedWithSpaces, {key2|verbatim}= surroundedWithSpaces ");
        assertThat(properties.getProperties().getProperty("key1"), equalTo("surroundedWithSpaces"));
        assertThat(properties.getProperties().getProperty("key2"), equalTo(" surroundedWithSpaces "));
    }

    @Test
    void canDecoratePropertyValuesToUpperAndLowerCase() {
        TableProperties properties = new TableProperties("{key1|uppercase}=toUpper, {key2|lowercase}=toLower");
        assertThat(properties.getProperties().getProperty("key1"), equalTo("TOUPPER"));
        assertThat(properties.getProperties().getProperty("key2"), equalTo("tolower"));
    }

    @Test
    void canTrimPropertyValuesByDefault() {
        TableProperties properties = new TableProperties("key1= surroundedWithSpaces , key2= ");
        assertThat(properties.getProperties().getProperty("key1"), equalTo("surroundedWithSpaces"));
        assertThat(properties.getProperties().getProperty("key2"), equalTo(""));
    }

    @Test
    void canChainDecoratorsToDecoratePropertyValues() {
        TableProperties properties = new TableProperties("{key1|uppercase|verbatim}= toUpper , {key2|lowercase|trim}= toLower ");
        assertThat(properties.getProperties().getProperty("key1"), equalTo(" TOUPPER "));
        assertThat(properties.getProperties().getProperty("key2"), equalTo("tolower"));
    }

    @Test
    void cantGetMandatoryProperty() {
        TableProperties properties = new TableProperties(createProperties(emptyMap()));
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> properties.getMandatoryIntProperty("key"));
        assertEquals("'key' is not set in ExamplesTable properties", thrown.getMessage());
    }

    @Test
    void canGetMandatoryIntProperty() {
        TableProperties properties = new TableProperties(createProperties(singletonMap("key", "1")));
        assertEquals(1, properties.getMandatoryIntProperty("key"));
    }

    @Test
    void canGetMandatoryLongProperty() {
        TableProperties properties = new TableProperties(createProperties(singletonMap("key", "1")));
        assertEquals(1l, properties.getMandatoryLongProperty("key"));
    }

    @Test
    void canGetMandatoryDoubleProperty() {
        TableProperties properties = new TableProperties(createProperties(singletonMap("key", "1")));
        assertEquals(1d, properties.getMandatoryDoubleProperty("key"));
    }

    @Test
    void canGetMandatoryBooleanProperty() {
        TableProperties properties = new TableProperties(createProperties(singletonMap("key", "true")));
        assertTrue(properties.getMandatoryBooleanProperty("key"));
    }

    @Test
    void canGetMandatoryNonBlankProperty() {
        TableProperties properties = new TableProperties(createProperties(singletonMap("key", "string")));
        assertEquals("string", properties.getMandatoryNonBlankProperty("key"));
    }

    @Test
    void canGetMandatoryEnumProperty() {
        TableProperties properties = new TableProperties(createProperties(singletonMap("key", "BLACK")));
        assertEquals(TestEnum.BLACK, properties.getMandatoryEnumProperty("key", TestEnum.class));
    }

    @Test
    void cantGetMandatoryEnumProperty() {
        TableProperties properties = new TableProperties(createProperties(singletonMap("key", "YELLOW")));
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> properties.getMandatoryEnumProperty("key", TestEnum.class));
        assertEquals("Value of ExamplesTable property 'key' must be from range [BLACK, WHITE], but got 'YELLOW'",
                thrown.getMessage());
    }

    private Properties createProperties(Map<String, String> map) {
        Properties properties = new Properties();
        properties.putAll(map);
        return properties;
    }

    private static enum TestEnum {
        BLACK, WHITE
    }
}
