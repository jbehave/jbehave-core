package org.jbehave.core.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.net.URL;

import org.jbehave.core.io.CodeLocations.InvalidCodeLocation;
import org.junit.Test;

public class CodeLocationsBehaviour {

    @Test
    public void shouldCreateURLFromPath() {
        URL codeLocation = CodeLocations.codeLocationFromPath("target/classes");
        assertThat(codeLocation.getFile(), endsWith("target/classes/"));
    }

    @Test(expected=InvalidCodeLocation.class)
    public void shouldNotCreateURLFromPathIfInvalid() {
        CodeLocations.codeLocationFromPath(null);
    }

    @Test
    public void shouldAllowInstantiation() {
        assertThat(new CodeLocations(), is(notNullValue()));
    }

}