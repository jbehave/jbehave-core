package org.jbehave.scenario.steps;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import org.hamcrest.Matcher;
import org.hamcrest.Description;
import org.hamcrest.BaseMatcher;
import static org.jbehave.Ensure.ensureThat;

import java.util.List;

import org.junit.Test;

public class StepdocGeneratorBehaviour {
	
    @Test
    public void shouldGenerateStepdocsInPriorityOrder() {
        StepdocGenerator generator = new DefaultStepdocGenerator();
        MySteps steps = new MySteps();
        List<Stepdoc> stepdocs = generator.generate(steps);
        ensureThat(stepdocs.get(0).getPattern(), equalTo("a given"));
        ensureThat(stepdocs.get(0).getAliasPatterns(), equalTo(asList("a given alias", "another given alias")));
        ensureThat(stepdocs.get(0).getMethod().getName(), equalTo("given"));
        ensureThat(stepdocs.get(1).getPattern(), equalTo("a when"));
        ensureThat(stepdocs.get(1).getAliasPatterns(), equalTo(asList("a when alias", "another when alias")));
        ensureThat(stepdocs.get(1).getMethod().getName(), equalTo("when"));
        ensureThat(stepdocs.get(2).getPattern(), equalTo("a then"));
        ensureThat(stepdocs.get(2).getAliasPatterns(), equalTo(asList("a then alias", "another then alias")));
        ensureThat(stepdocs.get(2).getMethod().getName(), equalTo("then"));
    }    

    @Test
    public void shouldHaveTerseSignatures() {
        StepdocGenerator generator = new DefaultStepdocGenerator();
        MoreSteps steps = new MoreSteps();
        List<Stepdoc> stepdocs = generator.generate(steps);
        ensureThat(stepdocs.get(0).getMethodSignature(), equalTo("org.jbehave.scenario.steps.StepdocGeneratorBehaviour$MoreSteps.givenAbc(int,int)"));
        ensureThat(stepdocs.get(1).getMethodSignature(), equalTo("org.jbehave.scenario.steps.StepdocGeneratorBehaviour$MoreSteps.whenAbc(int,int)"));
        ensureThat(stepdocs.get(2).getMethodSignature(), equalTo("org.jbehave.scenario.steps.StepdocGeneratorBehaviour$MoreSteps.whenXyz(int,int)"));
        ensureThat(stepdocs.get(3).getMethodSignature(), equalTo("org.jbehave.scenario.steps.StepdocGeneratorBehaviour$MoreSteps.thenAbc(int,int)"));
    }

    @Test
    public void shouldHaveFinerGrainedComparablesThanJustPriority() {
        StepdocGenerator generator = new DefaultStepdocGenerator();
        MoreSteps steps = new MoreSteps();
        List<Stepdoc> stepdocs = generator.generate(steps);
        Stepdoc when1 = stepdocs.get(1);
        Stepdoc when2 = stepdocs.get(2);

        ensureThat(when1.compareTo(when2), lessThan(0));
        ensureThat(when2.compareTo(when1), greaterThan(0));
    }

    private Matcher<Integer> lessThan(final int i) {
        return new BaseMatcher<Integer>() {
            public boolean matches(Object o) {
                return ((Integer) o).compareTo(i) < 0;
            }

            public void describeTo(Description description) {
                description.appendText("not less than");
            }
        };
    }
    
    private Matcher<Integer> greaterThan(final int i) {
        return new BaseMatcher<Integer>() {
            public boolean matches(Object o) {
                return ((Integer) o).compareTo(i) > 0;
            }

            public void describeTo(Description description) {
                description.appendText("not greater than");
            }
        };
    }


    public static class MySteps extends Steps {
        
        private int givens;
        private int whens;
        private int thens;
        
        @org.jbehave.scenario.annotations.Given("a given")
        @org.jbehave.scenario.annotations.Aliases(values={"a given alias", "another given alias"})
        public void given() {
            givens++;
        }

        @org.jbehave.scenario.annotations.When("a when")
        @org.jbehave.scenario.annotations.Aliases(values={"a when alias", "another when alias"})
        public void when() {
            whens++;
        }
        
        @org.jbehave.scenario.annotations.Then("a then")
        @org.jbehave.scenario.annotations.Aliases(values={"a then alias", "another then alias"})
        public void then() {
            thens++;
        }
                
    }

    public static class MoreSteps extends Steps {

        @org.jbehave.scenario.annotations.Given("blah $xx blah $yy")
        public void givenAbc(int xx, int yy) {
        }

        @org.jbehave.scenario.annotations.When("blah $xx blah $yy")
        public void whenAbc(int xx, int yy) {
        }
        
        @org.jbehave.scenario.annotations.When("blah $xx blah $yy blah")
        public void whenXyz(int xx, int yy) {
        }

        @org.jbehave.scenario.annotations.Then("blah $xx blah $yy")
        public void thenAbc(int xx, int yy) {
        }

    }

}
