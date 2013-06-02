package org.jbehave.example.spring.security.service;

import java.util.Collection;
import java.util.Date;

import org.jbehave.example.spring.security.domain.AuthenticationPolicy;
import org.jbehave.example.spring.security.domain.User;
import org.jbehave.example.spring.security.util.DateUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


public class UserDetailsImpl implements UserDetails {

  private static final long serialVersionUID = 1L;

  private String username;
  private String password;
  private boolean accountEnabled;
  private boolean accountExpired;
  private boolean accountLocked;
  private boolean passwordExpired;

  public UserDetailsImpl(User user, AuthenticationPolicy policy) {
    this.username = user.getUsername();
    this.password = user.getPassword();
    this.accountEnabled = determineAccountEnabled(user, policy);
    this.accountExpired = determineAccountExpired(user, policy);
    this.accountLocked = determineAccountLocked(user, policy);
    this.passwordExpired = determinePasswordExpired(user, policy);
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public boolean isEnabled() {
    return accountEnabled;
  }

  public boolean isAccountNonExpired() {
    return !accountExpired;
  }

  public boolean isAccountNonLocked() {
    return !accountLocked;
  }

  public boolean isCredentialsNonExpired() {
    return !passwordExpired;
  }

  protected boolean determineAccountEnabled(User user, AuthenticationPolicy policy) {
    return user.isEnabled();
  }

  protected boolean determineAccountExpired(User user, AuthenticationPolicy policy) {
    return user.isExpired();
  }

  protected boolean determineAccountLocked(User user, AuthenticationPolicy policy) {
    if (policy.isLockoutEnabled()) {
      // locked if login failure count is >= lockout count
      return user.getLoginFailureCount() >= policy.getLockoutCount();
    } else {
      // not locked
      return false;
    }
  }

  protected boolean determinePasswordExpired(User user, AuthenticationPolicy policy) {
    // if force-password-change then true
    if (user.isForcePasswordChange()) {
      return true;
    }

    // else look at policy
    if (policy.isPasswordAutoExpire()) {
      long elapsedDays = DateUtils.getElapsedDays(user.getLastPasswordResetDate(), new Date());
      return elapsedDays > policy.getPasswordExpiryDays();
    } else {
      // passwords only expire manually
      return false;
    }
  }

public Collection<? extends GrantedAuthority> getAuthorities() {
    // TODO Auto-generated method stub
    return null;
}

}
