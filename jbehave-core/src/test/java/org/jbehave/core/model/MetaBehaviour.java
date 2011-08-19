package org.jbehave.core.model;

import java.util.Arrays;

import org.jbehave.core.embedder.MetaFilter;
import org.junit.Ignore;
import org.junit.Test;

import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class MetaBehaviour {

    @Test
    public void shouldInheritFromParentStartingFromEmpty() {
        // Given
        Meta parent = new Meta(asList("one One"));

        // When
        Meta child = new Meta();
        assertThat(child.isEmpty(), is(true));
        assertThat(child.getProperty("one"), equalTo(Meta.BLANK));
        Meta meta = child.inheritFrom(parent);
        
        // Then
        assertThat(meta.isEmpty(), is(false));
        assertThat(meta.getProperty("one"), equalTo("One"));
    }

    @Test
    public void shouldInheritFromParentStartingFromNonEmpty() {
        // Given
        Meta parent = new Meta(asList("one One"));

        // When
        Meta child = new Meta(asList("two Two"));
        assertThat(child.isEmpty(), is(false));
        assertThat(child.getProperty("two"), equalTo("Two"));
        Meta meta = child.inheritFrom(parent);
        
        // Then
        assertThat(meta.isEmpty(), is(false));
        assertThat(meta.getProperty("one"), equalTo("One"));
        assertThat(meta.getProperty("two"), equalTo("Two"));
    }
    
    @Test
    public void shouldAllowSingleExclusion() {
      Meta meta = new Meta(Arrays.asList("environment all", "skip"));
      MetaFilter filter = new MetaFilter("-skip");
      assertThat("should not be allowed", filter.allow(meta), is(false));
    }

    @Test
    @Ignore("FIXME JBEHAVE-583")
    public void shouldAllowMultipleExclusions() {
      Meta meta = new Meta(Arrays.asList("environment all", "skip"));
      MetaFilter filter = new MetaFilter("-environment preview -skip");
      assertThat("should not be allowed", filter.allow(meta), is(false));
    }

}
