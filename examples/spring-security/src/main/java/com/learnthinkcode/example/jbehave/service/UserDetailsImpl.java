package com.learnthinkcode.example.jbehave.service;

import java.util.Date;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;

import com.learnthinkcode.example.jbehave.domain.AuthenticationPolicy;
import com.learnthinkcode.example.jbehave.domain.User;
import com.learnthinkcode.example.jbehave.util.DateUtils;

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

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public GrantedAuthority[] getAuthorities() {
    return null;
  }

  @Override
  public boolean isEnabled() {
    return accountEnabled;
  }

  @Override
  public boolean isAccountNonExpired() {
    return !accountExpired;
  }

  @Override
  public boolean isAccountNonLocked() {
    return !accountLocked;
  }

  @Override
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

}
