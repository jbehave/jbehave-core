package com.learnthinkcode.example.jbehave.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;

import com.learnthinkcode.example.jbehave.domain.Organization;

@Component("organizationManager")
public class OrganizationManager {

  protected ThreadLocal<Organization> organizations;

  @PostConstruct
  public void init() {
    organizations = new ThreadLocal<Organization>();
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
