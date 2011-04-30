package com.learnthinkcode.example.jbehave.domain;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class UserBuilder {

  private BeanWrapper user;

  public UserBuilder(Organization org) {
    User target = new User();
    target.setOrganization(org);
    user = new BeanWrapperImpl(target);
  }

  public UserBuilder(Organization org, Map<String, String> row) {
    this(org);
    for (String propertyName : row.keySet()) {
      if (!StringUtils.isBlank(propertyName)) {
        String propertyValue = row.get(propertyName);
        if (propertyName.equals("lastPasswordResetDate") && propertyValue.startsWith("t-")) {
          // convert t-N into a proper date
          long daysBefore = new Long(propertyValue.substring(2));
          long nowMillis = System.currentTimeMillis();
          long thenMillis = nowMillis - (daysBefore * 1000 * 60 * 60 * 24);
          Date then = new Date(thenMillis);
          user.setPropertyValue(propertyName, then);
        } else {
          user.setPropertyValue(propertyName, propertyValue);
        }
      }
    }
  }

  public User build() {
    return (User) user.getWrappedInstance();
  }
}
