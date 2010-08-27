package com.learnthinkcode.example.jbehave.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.learnthinkcode.example.jbehave.util.SecurityUtils;

@Entity
@Table(name = "APPLICATION_USER")
public class User implements Serializable {

  private static final long serialVersionUID = 2216302982473702606L;

  @Id
  @GeneratedValue
  @Column(name = "USER_ID")
  private Long id;

  @ManyToOne(optional = false, cascade = CascadeType.ALL)
  private Organization organization;

  @Column(name = "USERNAME")
  private String username;

  @Column(name = "PASSWORD")
  private String password;

  @Column(name = "IS_ENABLED")
  private boolean enabled;

  @Column(name = "IS_EXPIRED")
  private boolean expired;

  @Column(name = "IS_FORCE_PWD_CHANGE")
  private boolean forcePasswordChange;

  @Column(name = "LOGIN_FAILURE_COUNT")
  private int loginFailureCount;

  @Column(name = "LAST_PWD_RESET_DATE")
  private Date lastPasswordResetDate;

  public User() {
    enabled = true;
    expired = false;
    forcePasswordChange = false;
    loginFailureCount = 0;
    lastPasswordResetDate = new Date();
  }
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Organization getOrganization() {
    return organization;
  }

  public void setOrganization(Organization organization) {
    this.organization = organization;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setPasswordCleartext(String passwordCleartext) {
    setPassword(SecurityUtils.encodePassword(passwordCleartext));
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isExpired() {
    return expired;
  }

  public void setExpired(boolean expired) {
    this.expired = expired;
  }

  public boolean isForcePasswordChange() {
    return forcePasswordChange;
  }

  public void setForcePasswordChange(boolean forcePasswordChange) {
    this.forcePasswordChange = forcePasswordChange;
  }

  public int getLoginFailureCount() {
    return loginFailureCount;
  }

  public void setLoginFailureCount(int loginFailureCount) {
    this.loginFailureCount = loginFailureCount;
  }

  public Date getLastPasswordResetDate() {
    return lastPasswordResetDate;
  }

  public void setLastPasswordResetDate(Date lastPasswordResetDate) {
    this.lastPasswordResetDate = lastPasswordResetDate;
  }
}
