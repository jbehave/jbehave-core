package org.jbehave.core.embedder;

import org.jbehave.core.model.Meta;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPendingBehaviour;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GroovyMetaMatcherBehaviour {

    @Test
    public void shouldEvaluateAdditiveBooleanExpressions() {
        MetaBuilder metaBuilder = new MetaBuilder();
        MetaFilter.MetaMatcher mm = new GroovyMetaMatcher();
        mm.parse("groovy: (a == '11' | a == '22') && b == '33'");
        assertTrue(mm.match(metaBuilder.clear().a(11).b(33).build()));
        assertTrue(mm.match(metaBuilder.clear().a(22).b(33).build()));
        assertFalse(mm.match(metaBuilder.clear().a(44).b(33).build()));
        assertFalse(mm.match(metaBuilder.clear().a(11).b(44).build()));
        assertFalse(mm.match(metaBuilder.clear().a(11).build()));
        assertFalse(mm.match(metaBuilder.clear().b(33).build()));
        assertFalse(mm.match(metaBuilder.clear().c(99).build()));
    }

    @Test
    public void shouldEvaluateInEqualBooleanExpressions() {
        MetaBuilder metaBuilder = new MetaBuilder();
        MetaFilter.MetaMatcher mm = new GroovyMetaMatcher();
        mm.parse("groovy: a != '11' && b != '22'");
        assertFalse(mm.match(metaBuilder.clear().a(11).b(33).build()));
        assertTrue(mm.match(metaBuilder.clear().a(33).b(33).build()));
    }

    @Test
    public void shouldBeFast() {
        MetaBuilder metaBuilder = new MetaBuilder();
        MetaFilter.MetaMatcher mm = new GroovyMetaMatcher();
        long start = System.currentTimeMillis();
        mm.parse("groovy: a != '11' && b != '22'");
        for (int i = 0; i < 1000; i++) {
            assertFalse(mm.match(metaBuilder.clear().a(11).b(33).build()));
        }
        assertTrue("should be less than half a second for 1000 matches on a simple case", System.currentTimeMillis() - start < 500);
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

        public Meta build() {
            return new Meta(meta);
        }

        public MetaBuilder clear() {
            meta.remove("a");
            meta.remove("b");
            meta.remove("c");
            return this;
        }
    }


}
