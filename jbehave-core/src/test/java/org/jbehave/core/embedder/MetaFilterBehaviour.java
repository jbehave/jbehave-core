package org.jbehave.core.embedder;

import org.jbehave.core.model.Meta;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
        assertFilterAllowsProperty("+theme smoke testing", "theme smoke testing", true);
        assertFilterAllowsProperty("+theme smoke testing", "theme testing", false);
    }

    @Test
    public void shouldFilterByNameOnly() {
        assertFilterAllowsProperty("-skip", "skip", false);
    }
    
    @Test
    public void shouldFilterWithBothIncludeAndExclude() {
        assertFilterAllowsProperty("+theme smoke testing -skip", "theme smoke testing", true);
        assertFilterAllowsProperty("+theme smoke testing -skip", "skip", false);
        assertFilterAllowsProperty("+theme smoke testing", "theme smoke testing", true);
        assertFilterAllowsProperty("+theme smoke testing", "theme testing", false);
        assertFilterAllowsProperty("-skip", "theme testing", true);
        assertFilterAllowsProperty("-skip", "skip", false);
        assertFilterAllowsProperty("+theme smoke testing -theme UI", "theme smoke testing", true);
        assertFilterAllowsProperty("+theme smoke testing -theme UI", "theme UI", false);
    }

    @Test
    public void shouldFilterWithIncludeWinningOverExclude() {
        assertFilterAllowsProperty("+theme smoke testing -theme UI", "theme smoke testing", true);
        assertFilterAllowsProperty("+theme smoke testing -theme UI", "theme UI", false);
    }

    @Test
    public void shouldFilterByValueWithAsterisk() {
        assertFilterAllowsProperty("+map *API", "map Service API", true);
    }

    private void assertFilterAllowsProperty(String filter, String property, boolean allowed) {
        assertThat(new MetaFilter(filter).allow(new Meta(asList(property))), equalTo(allowed));
    }

}
