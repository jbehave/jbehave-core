package org.jbehave.example.spring.security.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.apache.commons.lang3.ClassUtils;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component("authenticationSteps")
public class AuthenticationSteps {

    @Autowired
    private AuthenticationManager manager;

    private Authentication auth;
    private AuthenticationException authException;

    @BeforeScenario
    public void setupScenario() {
        auth = null;
        authException = null;
    }

    @When("user <username> authenticates with password <password>")
    @Alias("user $username authenticates with password $password")
    public void userAuthenticates(@Named(value = "username") String username,
            @Named(value = "password") String password) {
        try {
            auth = manager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            authException = null;
        }
        catch (AuthenticationException e) {
            auth = null;
            authException = e;
        }
    }

    @Then("user should be authenticated")
    public void assertAuthenticationIsValid() {
        assertThat(auth, is(notNullValue()));
        assertThat(authException, is(nullValue()));
        assertThat(auth.isAuthenticated(), is(true));
    }

    @Then("user should not be authenticated")
    public void assertAuthenticationIsNotValid() {
        assertThat(auth, is(nullValue()));
        assertThat(authException, is(notNullValue()));
    }

    @Then("authentication failure is <failure>")
    @Alias("authentication failure is $failure")
    public void assertAuthenticationClassIs(@Named("failure") String failure) {
        assertThat(authException, is(notNullValue()));
        String expectedClassName = failure + "Exception";
        String actualClassName = ClassUtils.getShortClassName(authException.getClass());
        assertThat(actualClassName, equalTo(expectedClassName));
    }
}
