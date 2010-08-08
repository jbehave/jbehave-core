package org.jbehave.core.io;

import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URL;

import org.hamcrest.Matchers;
import org.jbehave.core.io.CodeLocations.InvalidCodeLocation;
import org.junit.Test;

public class CodeLocationsBehaviour {

    @Test
    public void shouldCreateURLFromPath() {
        URL codeLocation = CodeLocations.codeLocationFromPath("target/classes");
        assertThat(codeLocation.getFile(), Matchers.endsWith("target/classes/"));
    }

    @Test(expected=InvalidCodeLocation.class)
    public void shouldNotCreateURLFromPathIfInvalid() {
        CodeLocations.codeLocationFromPath(null);
    }

}