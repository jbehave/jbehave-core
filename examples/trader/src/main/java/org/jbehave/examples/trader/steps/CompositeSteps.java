package org.jbehave.examples.trader.steps;

import org.jbehave.core.annotations.Composite;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.When;

public class CompositeSteps {

    @Given("%customer has previously bought a %product")
    @Composite(steps = { "Given <customer> is logged in", 
                         "Given <customer> has a cart", 
                         "When a <product> is added to the cart" })
    public void aCompositeStep(@Named("customer") String customer, @Named("product") String product) {
    }

    @Given("<customer> is logged in")
    public void aCustomerIsLoggedIn(@Named("customer") String customer) {
    }

    @Given("<customer> has a cart")
    public void aCustomerHasACart(@Named("customer") String customer) {
    }

    @When("a <product> is added to the cart")
    public void aProductIsAddedToCart(@Named("product") String product) {
    }

}