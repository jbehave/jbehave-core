package org.jbehave.core.embedder;

import org.hamcrest.Matchers;
import org.jbehave.core.embedder.MetaFilter.DefaultMetaMatcher;
import org.jbehave.core.embedder.MetaFilter.MetaMatcher;
import org.jbehave.core.model.Meta;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

public class MetaFilterBehaviour {

	MetaBuilder metaBuilder = new MetaBuilder();

    @Test
    public void shouldParseIncludesAndExcludesUsingDefaultMetaMatcher() {
        String filterAsString = "+author Mauro -theme smoke testing +map *API -skip +defect-4321 -defect-1234";
        MetaFilter filter = filter(filterAsString);
        assertThat(filter.asString(), equalTo(filterAsString));
        MetaMatcher metaMatcher = filter.metaMatcher();
        assertThat(metaMatcher, Matchers.instanceOf(DefaultMetaMatcher.class));
        DefaultMetaMatcher defaultMetaMatcher = (DefaultMetaMatcher)metaMatcher;
        assertThat(defaultMetaMatcher.include().toString(), equalTo("{defect-4321=, author=Mauro, map=*API}"));
        assertThat(defaultMetaMatcher.exclude().toString(), equalTo("{defect-1234=, skip=, theme=smoke testing}"));
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
        assertThat(filter.allow(metaBuilder.clear().a(11).b(33).build()), is(true));
        assertThat(filter.allow(metaBuilder.clear().a(22).b(33).build()), is(true));
        assertThat(filter.allow(metaBuilder.clear().a(44).b(33).build()), is(false));
        assertThat(filter.allow(metaBuilder.clear().a(11).b(44).build()), is(false));
        assertThat(filter.allow(metaBuilder.clear().a(11).build()), is(false));
        assertThat(filter.allow(metaBuilder.clear().b(33).build()), is(false));
        assertThat(filter.allow(metaBuilder.clear().c(99).build()), is(false));
    }

    @Test
    public void shouldFilterByNegativeBooleanExpressionsUsingGroovy() {
        MetaFilter filter = filter("groovy: a != '11' && b != '22'");
        assertThat(filter.allow(metaBuilder.clear().a(11).b(33).build()), is(false));
        assertThat(filter.allow(metaBuilder.clear().a(33).b(33).build()), is(true));
    }

    @Test
    public void shouldFilterByPresenceOfPropertyUsingGroovy() {
        MetaFilter filter = filter("groovy: d");
        assertThat(filter.allow(metaBuilder.clear().a(11).build()), is(false));
        assertThat(filter.allow(metaBuilder.clear().a(11).d("").build()), is(true));
        assertThat(filter.allow(metaBuilder.clear().a(11).d("4nyth1ng").build()), is(true));
    }

    @Test
    public void shouldFilterByNonPresenceOfPropertyUsingGroovy() {
        MetaFilter filter = filter("groovy: !d");
        assertThat(filter.allow(metaBuilder.clear().a(11).build()), is(true));
        assertThat(filter.allow(metaBuilder.clear().a(11).d("").build()), is(false));
    }

    @Test
    public void shouldFilterByRegexUsingGroovy() {
        MetaFilter filter = filter("groovy: d ==~ /.*\\d+.*/");
        assertThat(filter.allow(metaBuilder.clear().d("fr3ddie").build()), is(true));
        assertThat(filter.allow(metaBuilder.clear().d("mercury").build()), is(false));
    }

    @Test
    @Ignore("Run on-demand depending when the env allows it")
    public void shouldBeFastUsingGroovy() {
        MetaFilter filter = filter("groovy: a != '11' && b != '22'");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            boolean allow = filter.allow(metaBuilder.clear().a(11).b(33).build());
            if ( allow ){
                continue;
            } else {
                break;
            }
        }
        long delta = System.currentTimeMillis() - start;
        assertThat("1000 matches should take less than a second, but took "+delta+" ms.", delta, lessThan(1000L));
    }
        
    private MetaFilter filter(String filterAsString) {
        return new MetaFilter(filterAsString, new SilentEmbedderMonitor(System.out));
    }

    @Test
    public void shouldFilterUsingCustomMetaMatcher() {
        String filterAsString = "custom: anything goes";
		Map<String, MetaMatcher> metaMatchers = new HashMap<String, MetaMatcher>();
		metaMatchers.put("custom:", new AnythingGoesMetaMatcher());
		MetaFilter filter = new MetaFilter(filterAsString, new SilentEmbedderMonitor(System.out), metaMatchers);
		assertThat(filter.metaMatcher(), instanceOf(AnythingGoesMetaMatcher.class));
        assertThat(filter.allow(metaBuilder.clear().d("anything").build()), is(true));
    }

    public class AnythingGoesMetaMatcher implements MetaMatcher {

		public void parse(String filterAsString) {
		}

		public boolean match(Meta meta) {
			return true;
		}

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
