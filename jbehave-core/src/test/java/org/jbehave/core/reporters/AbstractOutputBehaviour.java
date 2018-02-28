package org.jbehave.core.reporters;

import org.jbehave.core.io.IOUtils;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public abstract class AbstractOutputBehaviour {

    protected void assertThatOutputIs(String out, String pathToExpected) throws IOException {
        String expected = IOUtils.toString(getClass().getResourceAsStream(pathToExpected), true);
        assertEquals(dos2unix(expected), dos2unix(out));
    }

    protected String dos2unix(String string) {
        return string.replace("\r\n", "\n");
    }
}
