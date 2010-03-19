package org.jbehave.scenario.parser;

import org.jbehave.scenario.JUnitScenario;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.jbehave.Ensure.ensureThat;
import static org.jbehave.scenario.parser.UnderscoredCamelCaseResolver.NUMBERS_AS_UPPER_CASE_LETTERS_PATTERN;

public class UnderscoredCamelCaseResolverBehaviour {

    @Test
    public void shouldResolveCamelCasedClassNameToUnderscoredName() {
    	ScenarioNameResolver resolver = new UnderscoredCamelCaseResolver();
        ensureThat(resolver.resolve(CamelCaseScenario.class),
                equalTo("org/jbehave/scenario/parser/camel_case_scenario"));
    }
        
    @Test
    public void shouldResolveCamelCasedClassNameToUnderscoredNameWithExtension() {
    	ScenarioNameResolver resolver = new UnderscoredCamelCaseResolver(".scenario");
        ensureThat(resolver.resolve(CamelCase.class),
                equalTo("org/jbehave/scenario/parser/camel_case.scenario"));
    }
    
    @Test
    /**
     * Some teams are not going to have /scenarios/ directories,
     * they are going to co-mingle with tests and match in Maven land with *Scenario
     */
    public void shouldResolveCamelCasedClassNameToUnderscoredNameWithExtensionStrippingExtraneousWord() {
    	ScenarioNameResolver resolver = new UnderscoredCamelCaseResolver(".scenario").removeFromClassname("Scenario");
        ensureThat(resolver.resolve(CamelCaseScenario.class),
                equalTo("org/jbehave/scenario/parser/camel_case.scenario"));
    }

    @Test
    public void shouldResolveCamelCasedClassNameWithNumbersTreatedAsLowerCaseLetters() {
    	ScenarioNameResolver resolver = new UnderscoredCamelCaseResolver();
        ensureThat(resolver.resolve(CamelCaseWithA3Qualifier.class),
                equalTo("org/jbehave/scenario/parser/camel_case_with_a3_qualifier"));
        ensureThat(resolver.resolve(CamelCaseWithA33Qualifier.class),
                equalTo("org/jbehave/scenario/parser/camel_case_with_a33_qualifier"));
    }
        
    @Test
    public void shouldResolveCamelCasedClassNameWithNumbersTreatedAsUpperCaseLetters() {
    	ScenarioNameResolver resolver = new UnderscoredCamelCaseResolver("", NUMBERS_AS_UPPER_CASE_LETTERS_PATTERN);
        ensureThat(resolver.resolve(CamelCaseWithA3Qualifier.class),
                equalTo("org/jbehave/scenario/parser/camel_case_with_a_3_qualifier"));
        ensureThat(resolver.resolve(CamelCaseWithA33Qualifier.class),
                equalTo("org/jbehave/scenario/parser/camel_case_with_a_3_3_qualifier"));
    }
    
    static class CamelCaseScenario extends JUnitScenario {
        
    }

    static class CamelCase extends JUnitScenario {
        
    }
    
    static class CamelCaseWithA3Qualifier extends JUnitScenario {
        
    }
    
    static class CamelCaseWithA33Qualifier extends JUnitScenario {
        
    }
}
