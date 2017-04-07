package org.jbehave.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Test;

/**
 * @author Valery_Yatsynovich
 */
public class ExamplesTablePropertiesBehaviour {

    private static final String DEFAULT_SEPARATOR = "|";
    private static final String DEFAULT_IGNORABLE_SEPARATOR = "|--";

    private static final String PROPERTIES_AS_STRING = "ignorableSeparator=!--,headerSeparator=!,valueSeparator=!,"
            + "commentSeparator=#,trim=false,metaByRow=true,transformer=CUSTOM_TRANSFORMER";

    private ExamplesTableProperties customExamplesTableProperties() {
        return new ExamplesTableProperties(PROPERTIES_AS_STRING, DEFAULT_SEPARATOR, DEFAULT_SEPARATOR,
                DEFAULT_IGNORABLE_SEPARATOR);
    }

    private ExamplesTableProperties emptyExamplesTableProperties() {
        return new ExamplesTableProperties("", DEFAULT_SEPARATOR, DEFAULT_SEPARATOR, DEFAULT_IGNORABLE_SEPARATOR);
    }

    @Test
    public void testGetRowSeparator() {
        assertEquals("\n", customExamplesTableProperties().getRowSeparator());
    }

    @Test
    public void testGetCustomHeaderSeparator() {
        assertEquals("!", customExamplesTableProperties().getHeaderSeparator());
    }

    @Test
    public void testGetCustomValueSeparator() {
        assertEquals("!", customExamplesTableProperties().getValueSeparator());
    }

    @Test
    public void testGetCustomIgnorableSeparator() {
        assertEquals("!--", customExamplesTableProperties().getIgnorableSeparator());
    }

    @Test
    public void testGetCustomCommentSeparator() {
        assertEquals("#", customExamplesTableProperties().getCommentSeparator());
    }

    @Test
    public void testGetCustomTrim() {
        assertFalse(customExamplesTableProperties().isTrim());
    }

    @Test
    public void testGetCustomMetaByRow() {
        assertTrue(customExamplesTableProperties().isMetaByRow());
    }

    @Test
    public void testGetCustomTransformer() {
        assertEquals("CUSTOM_TRANSFORMER", customExamplesTableProperties().getTransformer());
    }

    @Test
    public void testGetDefaultHeaderSeparator() {
        assertEquals(DEFAULT_SEPARATOR, emptyExamplesTableProperties().getHeaderSeparator());
    }

    @Test
    public void testGetDefaultValueSeparator() {
        assertEquals(DEFAULT_SEPARATOR, emptyExamplesTableProperties().getValueSeparator());
    }

    @Test
    public void testGetDefaultIgnorableSeparator() {
        assertEquals(DEFAULT_IGNORABLE_SEPARATOR, emptyExamplesTableProperties().getIgnorableSeparator());
    }

    @Test
    public void testGetDefaultCommentSeparator() {
        assertNull(emptyExamplesTableProperties().getCommentSeparator());
    }

    @Test
    public void testGetDefaultTrim() {
        assertTrue(emptyExamplesTableProperties().isTrim());
    }

    @Test
    public void testGetDefaultMetaByRow() {
        assertFalse(emptyExamplesTableProperties().isMetaByRow());
    }

    @Test
    public void testGetDefaultTransformer() {
        assertNull(emptyExamplesTableProperties().getTransformer());
    }

    @Test
    public void testGetAllProperties() {
        Properties properties = new Properties();
        properties.setProperty("key", "value");
        assertEquals(properties, new ExamplesTableProperties(properties).getProperties());
    }
}
