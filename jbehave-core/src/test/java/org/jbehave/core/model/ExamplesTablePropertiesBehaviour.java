package org.jbehave.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;

/**
 * @author Valery Yatsynovich
 */
public class ExamplesTablePropertiesBehaviour {

    @Test
    public void canGetCustomProperties() {
        ExamplesTableProperties properties = new ExamplesTableProperties("ignorableSeparator=!--,headerSeparator=!,valueSeparator=!,"
                + "commentSeparator=#,trim=false,metaByRow=true,transformer=CUSTOM_TRANSFORMER", "|", "|",
                "|--");
        assertEquals("\n", properties.getRowSeparator());
        assertEquals("!", properties.getHeaderSeparator());
        assertEquals("!", properties.getValueSeparator());
        assertEquals("!--", properties.getIgnorableSeparator());
        assertEquals("#", properties.getCommentSeparator());
        assertFalse(properties.isTrim());
        assertTrue(properties.isMetaByRow());
        assertEquals("CUSTOM_TRANSFORMER", properties.getTransformer());
    }

    @Test
    public void canGetDefaultProperties() {
        ExamplesTableProperties properties = new ExamplesTableProperties(new Properties());
        assertEquals("|", properties.getHeaderSeparator());
        assertEquals("|", properties.getValueSeparator());
        assertEquals("|--", properties.getIgnorableSeparator());
        assertEquals("#", properties.getCommentSeparator());
        assertTrue(properties.isTrim());
        assertFalse(properties.isMetaByRow());
        assertNull(properties.getTransformer());
    }

    @Test
    public void canGetAllProperties() {
        Properties properties = new Properties();
        properties.setProperty("key", "value");
        ExamplesTableProperties tableProperties = new ExamplesTableProperties(properties);
        assertTrue(tableProperties.getProperties().containsKey("key"));
    }
}
