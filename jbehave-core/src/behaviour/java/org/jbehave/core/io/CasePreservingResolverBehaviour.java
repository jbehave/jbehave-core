package org.jbehave.core.io;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jbehave.core.JUnitStory;
import org.junit.Test;

public class CasePreservingResolverBehaviour {

    @Test
    public void shouldResolveClassNamePreservingCase() {
        CasePreservingResolver resolver = new CasePreservingResolver(".story");
        assertThat(resolver.resolve(CamelCase.class),
                equalTo("org/jbehave/core/io/CamelCase.story"));

    }

    @Test
    public void shouldResolveClassNamePreservingCaseWithNumbers() {
        CasePreservingResolver resolver = new CasePreservingResolver(".story");
        assertThat(resolver.resolve(CamelCaseWith3Dates.class),
                equalTo("org/jbehave/core/io/CamelCaseWith3Dates.story"));

    }

    static class CamelCase extends JUnitStory {
        
    }
    
    static class CamelCaseWith3Dates extends JUnitStory {
        
    }
}
