package org.jbehave.core.embedder;

import java.util.Properties;

import org.hamcrest.Matchers;
import org.jbehave.core.embedder.MetaFilter.DefaultMetaMatcher;
import org.jbehave.core.embedder.MetaFilter.MetaMatcher;
import org.jbehave.core.model.Meta;
import org.junit.Test;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MetaFilterBehaviour {

    MetaBuilder metaBuilder = new MetaBuilder();

    @Test
    public void shouldParseIncludesAndExcludesUsingDefaultMetaMatcher() {
        String filterAsString = "+author Mauro -theme smoke testing +map *API -skip";
        MetaFilter filter = filter(filterAsString);
        assertThat(filter.asString(), equalTo(filterAsString));
        MetaMatcher metaMatcher = filter.metaMatcher();
        assertThat(metaMatcher, Matchers.instanceOf(DefaultMetaMatcher.class));
        DefaultMetaMatcher defaultMetaMatcher = (DefaultMetaMatcher)metaMatcher;
        assertThat(defaultMetaMatcher.include().toString(), equalTo("{author=Mauro, map=*API}"));
        assertThat(defaultMetaMatcher.exclude().toString(), equalTo("{skip=, theme=smoke testing}"));
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

    @Test
    public void shouldTreatNullFiltersAsEmptyFilters() {
        assertFilterAllowsProperty(null, "skip", true);
        assertFilterAllowsProperty("", "skip", true);
    }

    private void assertFilterAllowsProperty(String filter, String property, boolean allowed) {
        assertThat(filter(filter).allow(new Meta(asList(property))), equalTo(allowed));
    }
    
    @Test
    public void shouldFilterByAdditiveBooleanExpressionsUsingGroovy() {
        MetaFilter filter = filter("groovy: (a == '11' | a == '22') && b == '33'");
        assertTrue(filter.allow(metaBuilder.clear().a(11).b(33).build()));
        assertTrue(filter.allow(metaBuilder.clear().a(22).b(33).build()));
        assertFalse(filter.allow(metaBuilder.clear().a(44).b(33).build()));
        assertFalse(filter.allow(metaBuilder.clear().a(11).b(44).build()));
        assertFalse(filter.allow(metaBuilder.clear().a(11).build()));
        assertFalse(filter.allow(metaBuilder.clear().b(33).build()));
        assertFalse(filter.allow(metaBuilder.clear().c(99).build()));
    }

    @Test
    public void shouldFilterByNegativeBooleanExpressionsUsingGroovy() {
        MetaFilter filter = filter("groovy: a != '11' && b != '22'");
        assertFalse(filter.allow(metaBuilder.clear().a(11).b(33).build()));
        assertTrue(filter.allow(metaBuilder.clear().a(33).b(33).build()));
    }

    @Test
    public void shouldFilterByPresenceOfPropertyUsingGroovy() {
        MetaFilter filter = filter("groovy: d");
        assertFalse(filter.allow(metaBuilder.clear().a(11).build()));
        assertTrue(filter.allow(metaBuilder.clear().a(11).d("").build()));
    }

    @Test
    public void shouldFilterByNonPresenceOfPropertyUsingGroovy() {
        MetaFilter filter = filter("groovy: !d");
        assertTrue(filter.allow(metaBuilder.clear().a(11).build()));
        assertFalse(filter.allow(metaBuilder.clear().a(11).d("").build()));
    }

    @Test
    public void shouldFilterByRegexUsingGroovy() {
        MetaFilter filter = filter("groovy: d ==~ /.*\\d+.*/");
        assertTrue(filter.allow(metaBuilder.clear().d("fr3ddie").build()));
        assertFalse(filter.allow(metaBuilder.clear().d("mercury").build()));
    }

    @Test
    public void shouldBeFastUsingGroovy() {
        long start = System.currentTimeMillis();
        MetaFilter filter = filter("groovy: a != '11' && b != '22'");
        for (int i = 0; i < 1000; i++) {
            assertFalse(filter.allow(metaBuilder.clear().a(11).b(33).build()));
        }
        assertTrue("should be less than half a second for 1000 matches on a simple case", System.currentTimeMillis() - start < 500);
    }

    private MetaFilter filter(String filterAsString) {
        return new MetaFilter(filterAsString, new SilentEmbedderMonitor(System.out));
    }

    public static class MetaBuilder {

        Properties meta = new Properties();

        public MetaBuilder a(int i) {
            meta.setProperty("a", "" + i);
            return this;
        }
        public MetaBuilder b(int i) {
            meta.setProperty("b", "" + i);
            return this;
        }
        public MetaBuilder c(int i) {
            meta.setProperty("c", "" + i);
            return this;
        }

        public MetaBuilder d(String val) {
            meta.setProperty("d", val);
            return this;
        }

        public Meta build() {
            return new Meta(meta);
        }

        public MetaBuilder clear() {
            meta.clear();
            return this;
        }

    }
}
