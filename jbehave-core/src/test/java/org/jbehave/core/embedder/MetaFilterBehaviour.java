package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.jbehave.core.model.Meta;
import org.junit.Test;

public class MetaFilterBehaviour {

    @Test
    public void shouldParseIncludesAndExcludes() {
        String filterAsString = "+author Mauro -theme smoke testing +map *API -skip";
        MetaFilter filter = new MetaFilter(filterAsString);
        assertThat(filter.asString(), equalTo(filterAsString));
        assertThat(filter.include().toString(), equalTo("{author=Mauro, map=*API}"));
        assertThat(filter.exclude().toString(), equalTo("{skip=, theme=smoke testing}"));
    }

    @Test
    public void shouldFilterByNameAndValue() {
        assertAllowed("+theme smoke testing", "theme smoke testing", true);
        assertAllowed("+theme smoke testing", "theme testing", false);
    }

    @Test
    public void shouldFilterByNameOnly() {
        assertAllowed("-skip", "skip", false);
    }

    @Test
    public void shouldFilterByValueWithAsterisk() {
        assertAllowed("+map *API", "map Service API", true);
    }

    private void assertAllowed(String filterAsString, String property, boolean allowed) {
        MetaFilter filter = new MetaFilter(filterAsString);
        assertThat(filter.allow(new Meta(asList(property))), equalTo(allowed));
    }

}
