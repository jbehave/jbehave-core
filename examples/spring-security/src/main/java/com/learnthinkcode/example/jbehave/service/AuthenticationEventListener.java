package com.learnthinkcode.example.jbehave.service;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.event.authentication.AbstractAuthenticationFailureEvent;
import org.springframework.security.event.authentication.AuthenticationSuccessEvent;
import org.springframework.security.userdetails.UserDetails;

import com.learnthinkcode.example.jbehave.dao.UserDao;
import com.learnthinkcode.example.jbehave.domain.User;

public class AuthenticationEventListener implements ApplicationListener {

  @Autowired
  private UserDao userDao;

  @Autowired
  private OrganizationManager organizationManager;

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    if (event instanceof AbstractAuthenticationFailureEvent) {
      onAuthenticationFailure((AbstractAuthenticationFailureEvent) event);
    }
    if (event instanceof AuthenticationSuccessEvent) {
      onAuthenticationSuccess((AuthenticationSuccessEvent) event);
    }
  }

  protected void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
    // on success - principal is a UserDetails
    UserDetails details = (UserDetails) event.getAuthentication().getPrincipal();
    String username = details.getUsername();
    if (!StringUtils.isBlank(username)) {
      Long orgId = organizationManager.getOrganization().getId();
      User user = userDao.findUserByOrganizationAndUsername(orgId, username);
      if (user != null) {
        user.setLoginFailureCount(0);
        userDao.persist(user);
      }
    }
  }

  protected void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
    // on failure - principal is a username
    String username = (String) event.getAuthentication().getPrincipal();
    if (!StringUtils.isBlank(username)) {
      Long orgId = organizationManager.getOrganization().getId();
      User user = userDao.findUserByOrganizationAndUsername(orgId, username);
      if (user != null) {
        int loginFailureCount = user.getLoginFailureCount();
        user.setLoginFailureCount(++loginFailureCount);
        userDao.persist(user);
      }
    }
  }

}
