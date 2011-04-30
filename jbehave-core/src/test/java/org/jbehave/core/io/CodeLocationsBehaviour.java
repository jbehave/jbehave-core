package org.jbehave.core.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.net.URL;

import org.jbehave.core.io.CodeLocations.InvalidCodeLocation;
import org.junit.Test;

public class CodeLocationsBehaviour {

    @Test
    public void shouldCreateURLFromPath() {
        String path = "target/classes/";
        URL codeLocation = CodeLocations.codeLocationFromPath(path);
        assertThat(codeLocation.getFile(), endsWith(path));
    }

    @Test(expected = InvalidCodeLocation.class)
    public void shouldNotCreateURLFromPathIfInvalid() {
        CodeLocations.codeLocationFromPath(null);
    }

    @Test
    public void shouldCreateURLFromURL() {
        String url = "http://company.com/stories/";
        URL codeLocation = CodeLocations.codeLocationFromURL(url);
        assertThat(codeLocation.toString(), equalTo(url));
        assertThat(codeLocation.toExternalForm(), equalTo(url));
    }

    @Test(expected = InvalidCodeLocation.class)
    public void shouldNotCreateURLFromURLIfInvalid() {
        CodeLocations.codeLocationFromURL("htp://company.com/stories/");
    }

    @Test
    public void shouldAllowInstantiation() {
        assertThat(new CodeLocations(), is(notNullValue()));
    }

}