package org.jbehave.example.spring.security.domain;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jbehave.core.steps.Parameters;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class UserBuilder {

  private BeanWrapper user;

  public UserBuilder(Organization org) {
    User target = new User();
    target.setOrganization(org);
    user = new BeanWrapperImpl(target);
  }

  public UserBuilder(Organization org, Parameters values, List<String> properties) {
    this(org);
    for (String propertyName : properties) {
      if (!StringUtils.isBlank(propertyName)) {
        String propertyValue = values.valueAs(propertyName,String.class);
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
