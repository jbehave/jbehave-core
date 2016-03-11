package org.jbehave.example.spring.security.steps;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
  public void userAuthenticates(@Named(value="username") String username, @Named(value="password") String password) {
    try {
      auth = manager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
      authException = null;
    } catch (AuthenticationException e) {
      auth = null;
      authException = e;
    }
  }

  @Then("user should be authenticated")
  public void assertAuthenticationIsValid() {
    assertNotNull(auth);
    assertNull(authException);
    assertTrue(auth.isAuthenticated());
  }

  @Then("user should not be authenticated")
  public void assertAuthenticationIsNotValid() {
    assertNull(auth);
    assertNotNull(authException);
  }

  @Then("authentication failure is <failure>")
  @Alias("authentication failure is $failure")
  public void assertAuthenticationClassIs(@Named("failure") String failure) {
    assertNotNull(authException);
    String expectedClassName = failure + "Exception";
    String actualClassName = ClassUtils.getShortClassName(authException.getClass());
    assertEquals(expectedClassName, actualClassName);
  }
}
