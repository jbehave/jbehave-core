package org.jbehave.core.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jbehave.core.JUnitStory;
import org.junit.Test;

public class CasePreservingResolverBehaviour {

    @Test
    public void shouldResolveClassNamePreservingCase() {
        CasePreservingResolver resolver = new CasePreservingResolver(".core");
        assertThat(resolver.resolve(CamelCase.class),
                equalTo("org/jbehave/core/parser/CamelCase.core"));

    }

    @Test
    public void shouldResolveClassNamePreservingCaseWithNumbers() {
        CasePreservingResolver resolver = new CasePreservingResolver(".core");
        assertThat(resolver.resolve(CamelCaseWith3Dates.class),
                equalTo("org/jbehave/core/parser/CamelCaseWith3Dates.core"));

    }

    static class CamelCase extends JUnitStory {
        
    }
    
    static class CamelCaseWith3Dates extends JUnitStory {
        
    }
}
