package org.jbehave.examples.core.steps;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Composite;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.When;

public class CompositeSteps {

    @Given("$customer has previously bought a $product") // used in normal parameter matching
    @Alias("<customer> has previously bought a <product>") // used in parameterised scenarios
    @Composite(steps = {
        "Given <customer> is logged in",
        "Given <customer> has a cart",
        "When a <product> is added to the cart"
    })
    public void compositeStep(@Named("customer") String customer, @Named("product") String product) {
        // composed steps use the named parameters
    }
    
    @Given("$customer returns to cart")
    @Composite(steps = {
        "Given step not found",
        "Given <customer> has a cart"
    })
    public void anotherCompositeStep(@Named("customer") String customer) { // composed steps use these named parameters
    }

    @Given("<customer> is logged in")
    public void customerIsLoggedIn(@Named("customer") String customer) {
    }

    @Given("<customer> has a cart")
    public void customerHasACart(@Named("customer") String customer) {
    }

    @When("a <product> is added to the cart")
    public void productIsAddedToCart(@Named("product") String product) {
    }

}