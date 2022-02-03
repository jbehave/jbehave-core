package org.jbehave.examples.core.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Set;
import java.util.TreeSet;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

public class ParametrisationByDelimitedNameSteps {

    private ThreadLocal<DataWrapper> data = ThreadLocal.withInitial(DataWrapper::new);

    @AfterScenario
    public void tearDown() {
        data.remove();
    }

    @Given("$user is logged in")
    public void loggedIn(String user) {
        getCurrentData().user = user;
    }

    @Given("$customer has a cart")
    public void customerHasACart(String customer) {
        DataWrapper currentData = getCurrentData();
        assertThat(currentData.user, notNullValue());
        assertThat(customer, equalTo(currentData.user));
        if (currentData.products == null) {
            currentData.products = new TreeSet<>();
        }
    }

    @Given("a user $user has borrowed books $isbns")
    public void createListOfBorrowedBooks(String user, String isbns) {
        System.out.println(user + ", " + isbns);
    }

    @When("a $product is added to the cart")
    public void productIsAddedToCart(String product) {
        Set<String> products = getCurrentData().products;
        assertThat(products, notNullValue());
        products.add(product);
    }

    @Then("cart contains $product")
    public void cartContains(String product) {
        Set<String> products = getCurrentData().products;
        assertThat(products, notNullValue());
        assertThat(products.contains(product), is(true));
    }

    private DataWrapper getCurrentData() {
        return this.data.get();
    }

    private static final class DataWrapper {
        private String user;
        private Set<String> products; // null means no cart...
    }
}
