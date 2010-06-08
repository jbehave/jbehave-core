package org.jbehave.core.steps;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

public class StepdocGeneratorBehaviour {
	
    @Test
    public void shouldGenerateStepdocsInPriorityOrder() {
        StepdocGenerator generator = new DefaultStepdocGenerator();
        List<Stepdoc> stepdocs = generator.generate(new MySteps());
        assertThat(stepdocs.get(0).getPattern(), equalTo("a given"));
        assertThat(stepdocs.get(0).getAliasPatterns(), equalTo(asList("a given alias", "another given alias")));
        assertThat(stepdocs.get(0).getMethod().getName(), equalTo("given"));
        assertThat(stepdocs.get(1).getPattern(), equalTo("a when"));
        assertThat(stepdocs.get(1).getAliasPatterns(), equalTo(asList("a when alias", "another when alias")));
        assertThat(stepdocs.get(1).getMethod().getName(), equalTo("when"));
        assertThat(stepdocs.get(2).getPattern(), equalTo("a then"));
        assertThat(stepdocs.get(2).getAliasPatterns(), equalTo(asList("a then alias", "another then alias")));
        assertThat(stepdocs.get(2).getMethod().getName(), equalTo("then"));
    }    

    @Test
    public void shouldGenerateStepdocsForBeforeAndAfterSteps() {
        StepdocGenerator generator = new DefaultStepdocGenerator();
        List<Stepdoc> stepdocs = generator.generate(new BeforeAndAfterSteps());
        assertThat(stepdocs.size(), equalTo(4));
        List<String> methodNames = new ArrayList<String>();
        for (Stepdoc stepdoc : stepdocs) {
			methodNames.add(stepdoc.getMethod().getName());
		}
        assertThat(methodNames, hasItem("beforeScenario"));
        assertThat(methodNames, hasItem("afterScenario"));
        assertThat(methodNames, hasItem("beforeStory"));
        assertThat(methodNames, hasItem("afterStory"));
    }    

    @Test
    public void shouldHaveTerseSignatures() {
        StepdocGenerator generator = new DefaultStepdocGenerator();
        MoreSteps steps = new MoreSteps();
        List<Stepdoc> stepdocs = generator.generate(steps);
        assertThat(stepdocs.get(0).getMethodSignature(), equalTo("org.jbehave.core.steps.StepdocGeneratorBehaviour$MoreSteps.givenAbc(int,int)"));
        assertThat(stepdocs.get(1).getMethodSignature(), equalTo("org.jbehave.core.steps.StepdocGeneratorBehaviour$MoreSteps.whenAbc(int,int)"));
        assertThat(stepdocs.get(2).getMethodSignature(), equalTo("org.jbehave.core.steps.StepdocGeneratorBehaviour$MoreSteps.whenXyz(int,int)"));
        assertThat(stepdocs.get(3).getMethodSignature(), equalTo("org.jbehave.core.steps.StepdocGeneratorBehaviour$MoreSteps.thenAbc(int,int)"));
    }

    @Test
    public void shouldHaveFinerGrainedComparablesThanJustPriority() {
        StepdocGenerator generator = new DefaultStepdocGenerator();
        MoreSteps steps = new MoreSteps();
        List<Stepdoc> stepdocs = generator.generate(steps);
        Stepdoc when1 = stepdocs.get(1);
        Stepdoc when2 = stepdocs.get(2);

        assertThat(when1.compareTo(when2), lessThan(0));
        assertThat(when2.compareTo(when1), greaterThan(0));
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
        
        @org.jbehave.core.annotations.Given("a given")
        @org.jbehave.core.annotations.Aliases(values={"a given alias", "another given alias"})
        public void given() {
            givens++;
        }

        @org.jbehave.core.annotations.When("a when")
        @org.jbehave.core.annotations.Aliases(values={"a when alias", "another when alias"})
        public void when() {
            whens++;
        }
        
        @org.jbehave.core.annotations.Then("a then")
        @org.jbehave.core.annotations.Aliases(values={"a then alias", "another then alias"})
        public void then() {
            thens++;
        }
                
    }

    public static class MoreSteps extends Steps {

        @org.jbehave.core.annotations.Given("blah $xx blah $yy")
        public void givenAbc(int xx, int yy) {
        }

        @org.jbehave.core.annotations.When("blah $xx blah $yy")
        public void whenAbc(int xx, int yy) {
        }
        
        @org.jbehave.core.annotations.When("blah $xx blah $yy blah")
        public void whenXyz(int xx, int yy) {
        }

        @org.jbehave.core.annotations.Then("blah $xx blah $yy")
        public void thenAbc(int xx, int yy) {
        }

    }

    public static class BeforeAndAfterSteps extends Steps {

        @org.jbehave.core.annotations.BeforeScenario
        public void beforeScenario() {
        }

        @org.jbehave.core.annotations.AfterScenario
        public void afterScenario() {
        }

        @org.jbehave.core.annotations.BeforeStory
        public void beforeStory() {
        }

        @org.jbehave.core.annotations.AfterStory
        public void afterStory() {
        }
    }

}
