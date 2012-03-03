package org.jbehave.core.io;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.net.URL;

import org.jbehave.core.io.CodeLocations.InvalidCodeLocation;
import org.junit.Test;
import org.junit.runner.JUnitCore;

public class CodeLocationsBehaviour {

    @Test
    public void shouldCreateCodeLocationFromPath() {
        String path = "target/classes/";
        URL codeLocation = CodeLocations.codeLocationFromPath(path);
        assertThat(codeLocation.getFile(), endsWith(path));
    }

    @Test
    public void shouldCreateCodeLocationFromURL() {
        String url = "http://company.com/stories/";
        URL codeLocation = CodeLocations.codeLocationFromURL(url);
        assertThat(codeLocation.toString(), equalTo(url));
        assertThat(codeLocation.toExternalForm(), equalTo(url));
    }

    @Test
    public void shouldCreateCodeLocationFromJarClass() {
        //  wrong output looks like this:
        //  "C:/Projects/jbehave/file:/C:/Users/Name/.m2/repository/junit/junit-dep/4.8.2/junit-dep-4.8.2.jar!"
        assertThat(CodeLocations.codeLocationFromClass(this.getClass()).getFile(), not(containsString("/file:")));
        assertThat(CodeLocations.codeLocationFromClass(JUnitCore.class).getFile(), not(containsString("/file:")));
    }

    @Test(expected = InvalidCodeLocation.class)
    public void shouldNotCreateCodeLocationFromPathIfInvalid() {
        CodeLocations.codeLocationFromPath(null);
    }

    @Test(expected = InvalidCodeLocation.class)
    public void shouldNotCreateCodeLocationFromURLIfInvalid() {
        CodeLocations.codeLocationFromURL("htp://company.com/stories/");
    }

    @Test
    public void shouldAllowInstantiation() {
        assertThat(new CodeLocations(), is(notNullValue()));
    }

}
