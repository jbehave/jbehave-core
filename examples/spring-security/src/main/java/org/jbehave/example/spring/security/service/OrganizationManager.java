package org.jbehave.example.spring.security.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jbehave.example.spring.security.domain.Organization;
import org.springframework.stereotype.Component;


@Component("organizationManager")
public class OrganizationManager {

  protected ThreadLocal<Organization> organizations;

  @PostConstruct
  public void init() {
    organizations = new ThreadLocal<>();
  }

  @PreDestroy
  public void destroy() {
    organizations = null;
  }

  public void setOrganization(Organization organization) {
    organizations.set(organization);
  }

  public Organization getOrganization() {
    return organizations.get();
  }

  public void removeOrganization() {
    organizations.remove();
  }

}
