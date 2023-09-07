package org.jbehave.core.embedder;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hamcrest.Matchers;
import org.jbehave.core.embedder.MetaFilter.DefaultMetaMatcher;
import org.jbehave.core.embedder.MetaFilter.MetaMatcher;
import org.jbehave.core.model.Meta;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MetaFilterBehaviour {

    MetaBuilder metaBuilder = new MetaBuilder();

    @Test
    void shouldParseIncludesAndExcludesUsingDefaultMetaMatcher() {
        String filterAsString = "+author Mauro -theme smoke testing +map *API -skip +defect-4321 -defect-1234";
        MetaFilter filter = filter(filterAsString);
        assertThat(filter.asString(), equalTo(filterAsString));
        MetaMatcher metaMatcher = filter.metaMatcher();
        assertThat(metaMatcher, Matchers.instanceOf(DefaultMetaMatcher.class));
        DefaultMetaMatcher defaultMetaMatcher = (DefaultMetaMatcher) metaMatcher;

        Map<String, String> expectedIncludes = new HashMap<>();
        expectedIncludes.put("author", "Mauro");
        expectedIncludes.put("map", "*API");
        expectedIncludes.put("defect-4321", "");
        assertThat(defaultMetaMatcher.include(), equalTo(expectedIncludes));

        Map<String, String> expectedExcludes = new HashMap<>();
        expectedExcludes.put("theme", "smoke testing");
        expectedExcludes.put("skip", "");
        expectedExcludes.put("defect-1234", "");
        assertThat(defaultMetaMatcher.exclude(), equalTo(expectedExcludes));
    }

    @ParameterizedTest
    @CsvSource({
        "+theme smoke testing,           theme smoke testing, false",
        "+theme smoke testing,           theme testing,       true",
        "-skip,                          skip,                true",
        "+theme smoke testing -skip,     theme smoke testing, false",
        "+theme smoke testing -skip,     skip,                true",
        "+theme smoke testing,           theme smoke testing, false",
        "+theme smoke testing,           theme testing,       true",
        "-skip,                          theme testing,       false",
        "-skip,                          skip,                true",
        "+theme smoke testing -theme UI, theme smoke testing, false",
        "+theme smoke testing -theme UI, theme UI,            true",
        "+theme smoke testing -theme UI, theme UI,            true",
        "+theme smoke testing -theme UI, theme smoke testing, false",
        "+map *API,                      map Service API,     false",
        ",                               skip,                false",
        "'',                             skip,                false"
    })
    void shouldApplyFilter(String filterAsString, String property, boolean excluded) {
        assertThat(filter(filterAsString).excluded(new Meta(asList(property))), equalTo(excluded));
    }
    
    @Test
    void shouldFilterByAdditiveBooleanExpressionsUsingGroovy() {
        MetaFilter filter = filter("groovy: (a == '11' | a == '22') && b == '33'");
        assertThat(filter.excluded(metaBuilder.clear().a(11).b(33).build()), is(false));
        assertThat(filter.excluded(metaBuilder.clear().a(22).b(33).build()), is(false));
        assertThat(filter.excluded(metaBuilder.clear().a(44).b(33).build()), is(true));
        assertThat(filter.excluded(metaBuilder.clear().a(11).b(44).build()), is(true));
        assertThat(filter.excluded(metaBuilder.clear().a(11).build()), is(true));
        assertThat(filter.excluded(metaBuilder.clear().b(33).build()), is(true));
        assertThat(filter.excluded(metaBuilder.clear().c(99).build()), is(true));
    }

    @Test
    void shouldFilterByNegativeBooleanExpressionsUsingGroovy() {
        MetaFilter filter = filter("groovy: a != '11' && b != '22'");
        assertThat(filter.excluded(metaBuilder.clear().a(11).b(33).build()), is(true));
        assertThat(filter.excluded(metaBuilder.clear().a(33).b(33).build()), is(false));
    }

    @Test
    void shouldFilterByPresenceOfPropertyUsingGroovy() {
        MetaFilter filter = filter("groovy: d");
        assertThat(filter.excluded(metaBuilder.clear().a(11).build()), is(true));
        assertThat(filter.excluded(metaBuilder.clear().a(11).d("").build()), is(false));
        assertThat(filter.excluded(metaBuilder.clear().a(11).d("4nyth1ng").build()), is(false));
    }

    @Test
    void shouldFilterByNonPresenceOfPropertyUsingGroovy() {
        MetaFilter filter = filter("groovy: !d");
        assertThat(filter.excluded(metaBuilder.clear().a(11).build()), is(false));
        assertThat(filter.excluded(metaBuilder.clear().a(11).d("").build()), is(true));
    }

    @Test
    void shouldFilterByRegexUsingGroovy() {
        MetaFilter filter = filter("groovy: d ==~ /.*\\d+.*/");
        assertThat(filter.excluded(metaBuilder.clear().d("fr3ddie").build()), is(false));
        assertThat(filter.excluded(metaBuilder.clear().d("mercury").build()), is(true));
    }

    @Test
    @Ignore("Run on-demand depending when the env allows it")
    void shouldBeFastUsingGroovy() {
        MetaFilter filter = filter("groovy: a != '11' && b != '22'");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            boolean excluded = filter.excluded(metaBuilder.clear().a(11).b(33).build());
            if (excluded) {
                break;
            }
        }
        long delta = System.currentTimeMillis() - start;
        assertThat("1000 matches should take less than a second, but took " + delta + " ms.", delta, lessThan(1000L));
    }
        
    private MetaFilter filter(String filterAsString) {
        return new MetaFilter(filterAsString, new SilentEmbedderMonitor());
    }

    @Test
    void shouldFilterUsingCustomMetaMatcher() {
        String filterAsString = "custom: anything goes";
        Map<String, MetaMatcher> metaMatchers = new HashMap<>();
        metaMatchers.put("custom:", new AnythingGoesMetaMatcher());
        MetaFilter filter = new MetaFilter(filterAsString, new SilentEmbedderMonitor(), metaMatchers);
        assertThat(filter.metaMatcher(), instanceOf(AnythingGoesMetaMatcher.class));
        assertThat(filter.excluded(metaBuilder.clear().d("anything").build()), is(false));
    }

    public static class AnythingGoesMetaMatcher implements MetaMatcher {

        @Override
        public void parse(String filterAsString) {
        }

        @Override
        public boolean match(Meta meta) {
            return true;
        }

    }

    @SuppressWarnings("checkstyle:MethodName")
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
