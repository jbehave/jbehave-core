package com.learnthinkcode.example.jbehave.domain;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class AuthenticationPolicyBuilder {

  private BeanWrapper authPolicy;

  public AuthenticationPolicyBuilder() {
    AuthenticationPolicy target = new AuthenticationPolicy();
    authPolicy = new BeanWrapperImpl(target);
  }

  public AuthenticationPolicyBuilder(Map<String, String> row) {
    this();
    for (String propertyName : row.keySet()) {
      if (!StringUtils.isBlank(propertyName)) {
        String propertyValue = row.get(propertyName);
        authPolicy.setPropertyValue(propertyName, propertyValue);
      }
    }
  }

  public AuthenticationPolicy build() {
    return (AuthenticationPolicy) authPolicy.getWrappedInstance();
  }
}
