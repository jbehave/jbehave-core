package org.jbehave.example.spring.security.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ORGANIZATION")
public class Organization implements Serializable {

  private static final long serialVersionUID = -8480231196735951704L;

  @Id
  @GeneratedValue
  @Column(name = "ORGANIZATION_ID")
  private Long id;

  @Column(name = "NAME")
  private String name;

  private AuthenticationPolicy authPolicy;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AuthenticationPolicy getAuthenticationPolicy() {
    return authPolicy;
  }

  public void setAuthenticationPolicy(AuthenticationPolicy authPolicy) {
    this.authPolicy = authPolicy;
  }

}
