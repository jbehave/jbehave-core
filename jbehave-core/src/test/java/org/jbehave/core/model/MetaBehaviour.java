package org.jbehave.core.model;

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

}
