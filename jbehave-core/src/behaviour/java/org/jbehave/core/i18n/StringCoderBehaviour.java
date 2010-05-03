package org.jbehave.core.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringCoderBehaviour {
    
    @Test
    public void shouldSupportUTF8() {        
        StringCoder coder = new StringCoder("UTF-8");
        assertTrue(coder.isCharsetSupported());
        assertEquals("UTF-8", coder.getCharsetName());
    }

}
