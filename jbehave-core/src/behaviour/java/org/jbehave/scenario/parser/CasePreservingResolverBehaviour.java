package org.jbehave.scenario.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.jbehave.Ensure.ensureThat;

import org.jbehave.scenario.JUnitScenario;
import org.junit.Test;

public class CasePreservingResolverBehaviour {

    @Test
    public void shouldResolveClassNamePreservingCase() {
        CasePreservingResolver resolver = new CasePreservingResolver(".scenario");
        ensureThat(resolver.resolve(CamelCase.class),
                equalTo("org/jbehave/scenario/parser/CamelCase.scenario"));

    }

    @Test
    public void shouldResolveClassNamePreservingCaseWithNumbers() {
        CasePreservingResolver resolver = new CasePreservingResolver(".scenario");
        ensureThat(resolver.resolve(CamelCaseWith3Dates.class),
                equalTo("org/jbehave/scenario/parser/CamelCaseWith3Dates.scenario"));

    }

    static class CamelCase extends JUnitScenario {
        
    }
    
    static class CamelCaseWith3Dates extends JUnitScenario {
        
    }
}
