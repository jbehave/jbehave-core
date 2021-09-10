package org.jbehave.core.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;

class TablePropertiesBehaviour {

    private final Keywords keywords = new LocalizedKeywords();

    private TableProperties createTablePropertiesWithDefaultSeparators(String propertiesAsString) {
        return new TableProperties(propertiesAsString, new LocalizedKeywords(), null);
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
        TableProperties properties = new TableProperties("", keywords, null);
        assertThat(properties.getHeaderSeparator(), equalTo("|"));
        assertThat(properties.getValueSeparator(), equalTo("|"));
        assertThat(properties.getIgnorableSeparator(), equalTo("|--"));
        assertThat(properties.getCommentSeparator(), equalTo(null));
        assertThat(properties.isTrim(), is(true));
        assertThat(properties.isMetaByRow(), is(false));
        assertThat(properties.getTransformer(), is(nullValue()));
    }

    @Test
    void canGetAllProperties() {
        TableProperties tableProperties = new TableProperties("key=value", keywords, null);
        assertThat(tableProperties.getProperties().containsKey("key"), is(true));
    }

    @Test
    void canGetPropertiesWithNestedTransformersWithoutEscaping() {
        TableProperties properties = new TableProperties(
                "transformer=CUSTOM_TRANSFORMER, tables={transformer=CUSTOM_TRANSFORMER\\, parameter1=value1}",
                keywords, null);
        assertThat(properties.isTrim(), is(true));
        assertThat(properties.isMetaByRow(), is(false));
        assertThat(properties.getTransformer(), equalTo("CUSTOM_TRANSFORMER"));
        assertThat(properties.getProperties().getProperty("tables"),
                equalTo("{transformer=CUSTOM_TRANSFORMER, parameter1=value1}"));
    }

    @Test
    void canDecoratePropertyValuesToTrimOrKeepVerbatim() {
        TableProperties properties = new TableProperties(
                "{key1|trim}= surroundedWithSpaces, {key2|verbatim}= surroundedWithSpaces ", keywords, null);
        assertThat(properties.getProperties().getProperty("key1"), equalTo("surroundedWithSpaces"));
        assertThat(properties.getProperties().getProperty("key2"), equalTo(" surroundedWithSpaces "));
    }

    @Test
    void canDecoratePropertyValuesToUpperAndLowerCase() {
        TableProperties properties = new TableProperties("{key1|uppercase}=toUpper, {key2|lowercase}=toLower", keywords,
                null);
        assertThat(properties.getProperties().getProperty("key1"), equalTo("TOUPPER"));
        assertThat(properties.getProperties().getProperty("key2"), equalTo("tolower"));
    }

    @Test
    void canTrimPropertyValuesByDefault() {
        TableProperties properties = new TableProperties("key1= surroundedWithSpaces , key2= ", keywords, null);
        assertThat(properties.getProperties().getProperty("key1"), equalTo("surroundedWithSpaces"));
        assertThat(properties.getProperties().getProperty("key2"), equalTo(""));
    }

    @Test
    void canChainDecoratorsToDecoratePropertyValues() {
        TableProperties properties = new TableProperties(
                "{key1|uppercase|verbatim}= toUpper , {key2|lowercase|trim}= toLower ", keywords, null);
        assertThat(properties.getProperties().getProperty("key1"), equalTo(" TOUPPER "));
        assertThat(properties.getProperties().getProperty("key2"), equalTo("tolower"));
    }

    @Test
    void cantGetMandatoryProperty() {
        TableProperties properties = new TableProperties("", keywords, null);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> properties.getMandatoryNonBlankProperty("key", int.class));
        assertEquals("'key' is not set in ExamplesTable properties", thrown.getMessage());
    }

    @Test
    void canGetMandatoryIntProperty() {
        TableProperties properties = new TableProperties("key=1", keywords, new ParameterConverters());
        int actual = properties.getMandatoryNonBlankProperty("key", int.class);
        assertEquals(1, actual);
    }

    @Test
    void canGetMandatoryLongProperty() {
        TableProperties properties = new TableProperties("key=1", keywords, new ParameterConverters());
        long actual = properties.getMandatoryNonBlankProperty("key", long.class);
        assertEquals(1L, actual);
    }

    @Test
    void canGetMandatoryDoubleProperty() {
        TableProperties properties = new TableProperties("key=1", keywords, new ParameterConverters());
        assertEquals(1d, properties.getMandatoryNonBlankProperty("key", double.class));
    }

    @Test
    void canGetMandatoryBooleanProperty() {
        TableProperties properties = new TableProperties("key=true", keywords, new ParameterConverters());
        boolean value = properties.getMandatoryNonBlankProperty("key", boolean.class);
        assertTrue(value);
    }

    @Test
    void canGetMandatoryNonBlankProperty() {
        TableProperties properties = new TableProperties("key=string", keywords, new ParameterConverters());
        assertEquals("string", properties.<String>getMandatoryNonBlankProperty("key", String.class));
    }

    @Test
    void canGetMandatoryEnumProperty() {
        TableProperties properties = new TableProperties("key=BLACK", keywords, new ParameterConverters());
        assertEquals(TestEnum.BLACK, properties.getMandatoryNonBlankProperty("key", TestEnum.class));
    }

    private enum TestEnum {
        BLACK, WHITE
    }
}
