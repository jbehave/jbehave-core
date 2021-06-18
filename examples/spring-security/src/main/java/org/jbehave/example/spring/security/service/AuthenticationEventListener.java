package org.jbehave.example.spring.security.service;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.example.spring.security.dao.UserDao;
import org.jbehave.example.spring.security.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticationEventListener implements ApplicationListener<AbstractAuthenticationEvent> {

    @Autowired
    private UserDao userDao;

    @Autowired
    private OrganizationManager organizationManager;

    @Override
    public void onApplicationEvent(AbstractAuthenticationEvent event) {
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
