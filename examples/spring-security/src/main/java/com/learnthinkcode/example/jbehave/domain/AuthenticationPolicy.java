package com.learnthinkcode.example.jbehave.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class AuthenticationPolicy implements Serializable {

  private static final long serialVersionUID = 8924363299528058665L;

  @Column(name = "IS_PWD_AUTO_EXPIRE")
  private boolean passwordAutoExpire;

  @Column(name = "PWD_EXPIRY_DAYS")
  private Integer passwordExpiryDays;

  @Column(name = "IS_PWD_LOCKOUT")
  private boolean lockoutEnabled;

  @Column(name = "PWD_LOCKOUT_COUNT")
  private Integer lockoutCount;

  public AuthenticationPolicy() {
    passwordAutoExpire = false;
    passwordExpiryDays = null;
    lockoutEnabled = false;
    lockoutCount = null;
  }

  public boolean isPasswordAutoExpire() {
    return passwordAutoExpire;
  }

  public void setPasswordAutoExpire(boolean passwordAutoExpire) {
    this.passwordAutoExpire = passwordAutoExpire;
  }

  public Integer getPasswordExpiryDays() {
    return passwordExpiryDays;
  }

  public void setPasswordExpiryDays(Integer passwordExpiryDays) {
    this.passwordExpiryDays = passwordExpiryDays;
  }

  public boolean isLockoutEnabled() {
    return lockoutEnabled;
  }

  public void setLockoutEnabled(boolean lockoutEnabled) {
    this.lockoutEnabled = lockoutEnabled;
  }

  public Integer getLockoutCount() {
    return lockoutCount;
  }

  public void setLockoutCount(Integer lockoutCount) {
    this.lockoutCount = lockoutCount;
  }
}
