package org.jbehave.core.model;

import org.jbehave.core.model.ExamplesTable.ExamplesTableProperties;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Valery Yatsynovich
 */
public class ExamplesTablePropertiesBehaviour {

    @Test
    public void canGetCustomProperties() {
        ExamplesTableProperties properties = new ExamplesTableProperties("ignorableSeparator=!--,headerSeparator=!,valueSeparator=!,"
                + "commentSeparator=#,trim=false,metaByRow=true,transformer=CUSTOM_TRANSFORMER", "|", "|",
                "|--");
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
    public void canSetPropertiesWithBackwardSlash() {
        ExamplesTableProperties properties = new ExamplesTableProperties("custom=\\", "|", "|", "|--");
        assertThat(properties.getProperties().getProperty("custom"), equalTo("\\"));
    }

    @Test
    public void canGetDefaultProperties() {
        ExamplesTableProperties properties = new ExamplesTableProperties(new Properties());
        assertThat(properties.getHeaderSeparator(), equalTo("|"));
        assertThat(properties.getValueSeparator(), equalTo("|"));
        assertThat(properties.getIgnorableSeparator(), equalTo("|--"));
        assertThat(properties.getCommentSeparator(), equalTo("#"));
        assertThat(properties.isTrim(), is(true));
        assertThat(properties.isMetaByRow(), is(false));
        assertThat(properties.getTransformer(), is(nullValue()));
    }

    @Test
    public void canGetAllProperties() {
        Properties properties = new Properties();
        properties.setProperty("key", "value");
        ExamplesTableProperties tableProperties = new ExamplesTableProperties(properties);
        assertThat(tableProperties.getProperties().containsKey("key"), is(true));
    }

    @Test
    public void canGetPropertiesWithNestedTransformersWithoutEscaping() {
        ExamplesTableProperties properties = new ExamplesTableProperties("transformer=CUSTOM_TRANSFORMER, " +
                "tables={transformer=CUSTOM_TRANSFORMER\\, parameter1=value1}", "|", "|",
                "|--");
        assertThat(properties.getRowSeparator(), equalTo("\n"));
        assertThat(properties.getHeaderSeparator(), equalTo("|"));
        assertThat(properties.getValueSeparator(), equalTo("|"));
        assertThat(properties.getIgnorableSeparator(), equalTo("|--"));
        assertThat(properties.isTrim(), is(true));
        assertThat(properties.isMetaByRow(), is(false));
        assertThat(properties.getTransformer(), equalTo("CUSTOM_TRANSFORMER"));
        assertThat(properties.getProperties().getProperty("tables"),
                equalTo("{transformer=CUSTOM_TRANSFORMER, parameter1=value1}"));
    }
}
