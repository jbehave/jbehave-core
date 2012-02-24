package org.jbehave.examples.trader.steps;

import java.util.Set;
import java.util.TreeSet;

import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ParametrisationByDelimitedNameSteps {

  private String user;    
  private Set<String> products; // null means no cart...

  @BeforeScenario
  public void init() {
    user = null;
    products = null;
  }

  @Given("$user is logged in")
  public void loggedIn(String user) {
    this.user = user;
  }

  @Given("$customer has a cart")
  public void aCustomerHasACart(String customer) {
    assertThat(user, notNullValue());
    assertThat(customer, equalTo(user));
    if (products == null) products = new TreeSet<String>();
  }

  @When("a $product is added to the cart")
  public void aProductIsAddedToCart(String product) {
    assertThat(products, notNullValue());
    products.add(product);
  }

  @Then("cart contains $product")
  public void cartContains(String product) {
    assertThat(products, notNullValue());
    assertThat(products.contains(product), is(true));
  }
}