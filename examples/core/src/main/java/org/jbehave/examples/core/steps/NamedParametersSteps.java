package org.jbehave.examples.core.steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;

public class NamedParametersSteps {

    public String ith;
    public String nth;

    @Given("parameters matched by name in natural order $ith and $nth")
    public void parametersMatchedInNaturalOrder(@Named("ith") String ithName, @Named("nth") String nthName){
        this.ith = ithName;
        this.nth = nthName;
    }

    @Given("parameters matched by name in inverse order $ith and $nth")
    public void parametersMatchedInInverseOrder(@Named("nth") String nthName, @Named("ith") String ithName){
        this.ith = ithName;
        this.nth = nthName;
    }
    
   @Then("parameters values are $ith and $nth")
   public void parametersValuesAre(String ith, String nth){
        assertThat(this.ith, equalTo(ith));
        assertThat(this.nth, equalTo(nth));
    }


}