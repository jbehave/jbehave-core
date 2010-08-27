package com.learnthinkcode.example.jbehave.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.ClassUtils;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
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

  @When("user $username authenticates with password $password")
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

  @Then("authentication failure is $exceptionClass")
  public void assertAuthenticationClassIs(String className) {
    assertNotNull(authException);
    String expectedClassName = ClassUtils.getShortClassName(authException.getClass());
    assertEquals(expectedClassName, className+"Exception");
  }
}
